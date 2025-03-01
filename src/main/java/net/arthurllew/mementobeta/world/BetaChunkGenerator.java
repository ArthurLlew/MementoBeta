package net.arthurllew.mementobeta.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arthurllew.mementobeta.world.noise.NoiseGeneratorPerlinOctaves;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class BetaChunkGenerator extends NoiseBasedChunkGenerator {
    /**
     * Generator settings.
     */
    private final Holder<NoiseGeneratorSettings> settings;

    /**
     * World seed.
     */
    private long worldSeed;

    // Noises
    private NoiseGeneratorPerlinOctaves minLimitOctaveNoise;
    private NoiseGeneratorPerlinOctaves maxLimitOctaveNoise;
    private NoiseGeneratorPerlinOctaves mainOctaveNoise;
    private NoiseGeneratorPerlinOctaves beachOctaveNoise;
    private NoiseGeneratorPerlinOctaves surfaceOctaveNoise;
    private NoiseGeneratorPerlinOctaves scaleOctaveNoise;
    private NoiseGeneratorPerlinOctaves depthOctaveNoise;
    private NoiseGeneratorPerlinOctaves forestOctaveNoise;
    private double[] sandNoise = new double[256];
    private double[] gravelNoise = new double[256];
    private double[] stoneNoise = new double[256];
    private double[] mainNoise;
    private double[] minLimitNoise;
    private double[] maxLimitNoise;
    private double[] scaleNoise;
    private double[] depthNoise;
    private double[] noise;

    // Biome stuff
    BiomeGenBase[] biomes;
    private BetaChunkManager betaChunkManager;

    /**
     * Codec reads data from .json files in data folder.
     */
    public static final Codec<BetaChunkGenerator> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source")
                                    .forGetter(generator -> generator.biomeSource),
                            NoiseGeneratorSettings.CODEC.fieldOf("settings")
                                    .forGetter(generator -> generator.settings)
                    )
                    .apply(instance, instance.stable(BetaChunkGenerator::new))
    );

    /**
     * Constructor.
     * @param biomeSource biome provider.
     * @param settings generator settings.
     */
    BetaChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
        this.settings = settings;
    }

    /**
     * Set generator seed, init random class and noise generators.
     * @param seed world seed.
     */
    public void setSeed(long seed) {
        // Init chunk manager
        this.betaChunkManager = new BetaChunkManager(seed);

        // Init octave noises
        Random rand = new Random(seed);
        this.minLimitOctaveNoise = new NoiseGeneratorPerlinOctaves(rand, 16);
        this.maxLimitOctaveNoise = new NoiseGeneratorPerlinOctaves(rand, 16);
        this.mainOctaveNoise = new NoiseGeneratorPerlinOctaves(rand, 8);
        this.beachOctaveNoise = new NoiseGeneratorPerlinOctaves(rand, 4);
        this.surfaceOctaveNoise = new NoiseGeneratorPerlinOctaves(rand, 4);
        this.scaleOctaveNoise = new NoiseGeneratorPerlinOctaves(rand, 10);
        this.depthOctaveNoise = new NoiseGeneratorPerlinOctaves(rand, 16);
        this.forestOctaveNoise = new NoiseGeneratorPerlinOctaves(rand, 8);

        // Save world seed
        this.worldSeed = seed;
    }

    /**
     * Returns stored codec.
     * @return codec
     */
    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    /**
     * Get world height from generator settings.
     * @return world height
     */
    @Override
    public int getGenDepth() {
        return this.settings.value().noiseSettings().height();
    }

    /**
     * Get world sea level from generator settings.
     * @return sea level
     */
    @Override
    public int getSeaLevel() {
        return this.settings.value().seaLevel();
    }

    /**
     * Get world minimum Y position from generator settings.
     * @return world minimum Y position
     */
    @Override
    public int getMinY() {
        return this.settings.value().noiseSettings().minY();
    }

    /**
     * Generate info in debug menu.
     * @param info string list.
     * @param random noise config.
     * @param pos player position.
     */
    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {}

    /**
     * In Vanilla generators this method is invoked at the beginning of the chunk generation. Here only an
     * approximation of terrain heightmap is used to determine structure positions.
     * @param registryManager manager for registries.
     * @param placementCalculator placement calculator.
     * @param structureAccessor structure accessor.
     * @param chunk chunk to process.
     * @param structureTemplateManager manager for structure templates.
     */
    @Override
    public void createStructures(RegistryAccess registryManager, ChunkGeneratorStructureState placementCalculator,
                                 StructureManager structureAccessor, ChunkAccess chunk,
                                 StructureTemplateManager structureTemplateManager
    ) {
        // In Vanilla generators this method is invoked at the beginning of the chunk generation.
        // Here only an approximation of terrain heightmap is used to determine structure positions.
        // However, we can only give an exact terrain (since beta generator had different logic).
        // So here the base terrain is generated.

        // For the future use in structure placement we need to fill in heightmaps in a chunk
        Heightmap heightmapOceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmapSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        // Prepare block position
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Save chunk position
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // In Vanilla Beta 1.7.3 temperature and humidity both are used right of the bat,
        // so biomes are generated first by WorldChunkManager.generateBiomeInfo(...) method.
        // TODO: generate only temperature and humidity here (it effects erosion in noise)
        this.biomes = betaChunkManager.generateBiomeInfo(biomes,
                chunkX * 16, chunkZ * 16,
                16, 16);

        // ================================================================================================
        // In Vanilla Beta 1.7.3 this section is done by ChunkProviderGenerate.generateTerrain(...) method.
        // ================================================================================================

        // Sea level is stored in generator settings
        int seaLevel = this.settings.value().seaLevel();

        // Those are initialized at the beginning of ChunkProviderGenerate.generateTerrain(...) method.
        byte sizeHorizontal = 4;
        byte sizeVertical = 16;
        int sizeX = sizeHorizontal + 1;
        int sizeY = sizeVertical + 1;
        int sizeZ = sizeHorizontal + 1;

        // Generate terrain noise
        this.noise = this.noiseGeneration(this.noise,
                chunkX * sizeHorizontal, 0, chunkZ * sizeHorizontal,
                sizeX, sizeY, sizeZ);

        // Generate terrain
        for(int subChunkX = 0; subChunkX < sizeHorizontal; ++subChunkX) {
            for(int subChunkZ = 0; subChunkZ < sizeHorizontal; ++subChunkZ) {
                for(int subChunkY = 0; subChunkY < sizeVertical; ++subChunkY) {
                    // Get noises
                    double noise1 = this.noise[((subChunkX + 0) * sizeZ + subChunkZ + 0) * sizeY + subChunkY + 0];
                    double noise2 = this.noise[((subChunkX + 0) * sizeZ + subChunkZ + 1) * sizeY + subChunkY + 0];
                    double noise3 = this.noise[((subChunkX + 1) * sizeZ + subChunkZ + 0) * sizeY + subChunkY + 0];
                    double noise4 = this.noise[((subChunkX + 1) * sizeZ + subChunkZ + 1) * sizeY + subChunkY + 0];
                    double noiseDelta1 = (this.noise[((subChunkX + 0) * sizeZ + subChunkZ + 0) * sizeY + subChunkY + 1] - noise1) * 0.125D;
                    double noiseDelta2 = (this.noise[((subChunkX + 0) * sizeZ + subChunkZ + 1) * sizeY + subChunkY + 1] - noise2) * 0.125D;
                    double noiseDelta3 = (this.noise[((subChunkX + 1) * sizeZ + subChunkZ + 0) * sizeY + subChunkY + 1] - noise3) * 0.125D;
                    double NoiseDelta4 = (this.noise[((subChunkX + 1) * sizeZ + subChunkZ + 1) * sizeY + subChunkY + 1] - noise4) * 0.125D;

                    for(int subY = 0; subY < 8; ++subY) {
                        // Pre-density values
                        double preDensity1 = noise1;
                        double preDensity2 = noise2;
                        double preDensityDelta1 = (noise3 - noise1) * 0.25D;
                        double preDensityDelta2 = (noise4 - noise2) * 0.25D;

                        for(int subX = 0; subX < 4; ++subX) {
                            // Density values
                            double density = preDensity1;
                            double densityDelta = (preDensity2 - preDensity1) * 0.25D;

                            for(int subZ = 0; subZ < 4; ++subZ) {
                                // Set block position
                                pos.set(subX + subChunkX * 4, subY + subChunkY * 8, subZ + subChunkZ * 4);

                                // Choose block
                                Block block;
                                if(density > 0.0D) {
                                    // Stone for any density > 0
                                    block = Blocks.STONE;
                                }
                                else
                                {
                                    // Set water if below sea level
                                    if(subY + subChunkY * 8 < seaLevel) {
                                        block = Blocks.WATER;
                                    }
                                    // Air otherwise
                                    else {
                                        block = Blocks.AIR;
                                    }
                                }

                                // Set block and update heightmap
                                chunk.setBlockState(pos, block.defaultBlockState(), false);
                                heightmapOceanFloor.update(pos.getX(), pos.getY(), pos.getZ(), block.defaultBlockState());
                                heightmapSurface.update(pos.getX(), pos.getY(), pos.getZ(), block.defaultBlockState());

                                // Update density
                                density += densityDelta;
                            }

                            // Update pre-density
                            preDensity1 += preDensityDelta1;
                            preDensity2 += preDensityDelta2;
                        }

                        // Update noise
                        noise1 += noiseDelta1;
                        noise2 += noiseDelta2;
                        noise3 += noiseDelta3;
                        noise4 += NoiseDelta4;
                    }
                }
            }
        }

        // Call parent method so structures can actually generate
        super.createStructures(registryManager,
                placementCalculator, structureAccessor, chunk, structureTemplateManager);
    }

    /**
     * Generates Beta 1.7.3 terrain noise.
     * @param buffer noise buffer.
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param z Z coordinate.
     * @param sizeX noise X size.
     * @param sizeY noise Y size.
     * @param sizeZ noise Z size.
     * @return filled buffer.
     */
    private double[] noiseGeneration(@Nullable double[] buffer, int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
        if (buffer == null)  {
            buffer = new double[sizeX * sizeY * sizeZ];
        }

        double scaleX = 684.412D;
        double scaleY = 684.412D;
        double[] temperature = this.betaChunkManager.temperature;
        double[] humidity = this.betaChunkManager.humidity;
        this.scaleNoise = this.scaleOctaveNoise.generateNoiseOctavesFlat(this.scaleNoise, x, z, sizeX, sizeZ,
                1.121D, 1.121D, 0.5D);
        this.depthNoise = this.depthOctaveNoise.generateNoiseOctavesFlat(this.depthNoise, x, z, sizeX, sizeZ,
                200.0D, 200.0D, 0.5D);
        this.mainNoise = this.mainOctaveNoise.generateNoiseOctaves(this.mainNoise,
                x, y, z,
                sizeX, sizeY, sizeZ,
                scaleX / 80.0D,
                scaleY / 160.0D,
                scaleX / 80.0D);
        this.minLimitNoise = this.minLimitOctaveNoise.generateNoiseOctaves(this.minLimitNoise,
                x, y, z,
                sizeX, sizeY, sizeZ,
                scaleX, scaleY, scaleX);
        this.maxLimitNoise = this.maxLimitOctaveNoise.generateNoiseOctaves(this.maxLimitNoise,
                x, y, z,
                sizeX, sizeY, sizeZ,
                scaleX, scaleY, scaleX);

        int noiseIndex1 = 0;
        int noiseIndex2 = 0;
        int anotherSize = 16 / sizeX;

        for(int noiseX = 0; noiseX < sizeX; ++noiseX) {
            int noiseXIndex = noiseX * anotherSize + anotherSize / 2;

            for(int noiseZ = 0; noiseZ < sizeZ; ++noiseZ) {
                int noiseZIndex = noiseZ * anotherSize + anotherSize / 2;
                double temperatureValue = temperature[noiseXIndex * 16 + noiseZIndex];
                double rain = humidity[noiseXIndex * 16 + noiseZIndex] * temperatureValue;

                rain = 1.0D - rain;
                rain *= rain;
                rain *= rain;
                rain = 1.0D - rain;

                double scale = (this.scaleNoise[noiseIndex2] + 256.0D) / 512.0D;
                scale *= rain;
                if(scale > 1.0D) {
                    scale = 1.0D;
                }

                double depth = this.depthNoise[noiseIndex2] / 8000.0D;
                if(depth < 0.0D) {
                    depth = -depth * 0.3D;
                }

                depth = depth * 3.0D - 2.0D;
                if(depth < 0.0D) {
                    depth /= 2.0D;
                    if(depth < -1.0D) {
                        depth = -1.0D;
                    }

                    depth /= 1.4D;
                    depth /= 2.0D;
                    scale = 0.0D;
                } else {
                    if(depth > 1.0D) {
                        depth = 1.0D;
                    }

                    depth /= 8.0D;
                }

                if(scale < 0.0D) {
                    scale = 0.0D;
                }

                scale += 0.5D;
                depth = depth * (double)sizeY / 16.0D;
                double var31 = (double)sizeY / 2.0D + depth * 4.0D;
                ++noiseIndex2;

                for(int NoiseY = 0; NoiseY < sizeY; ++NoiseY) {
                    double densityOffset = ((double)NoiseY - var31) * 12.0D / scale;
                    if(densityOffset < 0.0D) {
                        densityOffset *= 4.0D;
                    }

                    double minLimitNoise = this.minLimitNoise[noiseIndex1] / 512.0D;
                    double maxLimitNoise = this.maxLimitNoise[noiseIndex1] / 512.0D;
                    double mainNoise = (this.mainNoise[noiseIndex1] / 10.0D + 1.0D) / 2.0D;

                    double density;
                    if(mainNoise < 0.0D) {
                        density = minLimitNoise;
                    } else if(mainNoise > 1.0D) {
                        density = maxLimitNoise;
                    } else {
                        density = minLimitNoise + (maxLimitNoise - minLimitNoise) * mainNoise;
                    }

                    density -= densityOffset;
                    if(NoiseY > sizeY - 4) {
                        double var44 = (float)(NoiseY - (sizeY - 4)) / 3.0F;
                        density = density * (1.0D - var44) + -10.0D * var44;
                    }

                    buffer[noiseIndex1] = density;
                    ++noiseIndex1;
                }
            }
        }

        return buffer;
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Executor executor, RandomState noiseConfig, Blender blender,
                                                       StructureManager structureAccessor, ChunkAccess chunk) {
        //return super.populateBiomes(executor, noiseConfig, blender, structureAccessor, chunk);
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            chunk.fillBiomesFromNoise(new TestBiomeSupplier(chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG),
                    Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.BIOME)), noiseConfig.sampler());
            return chunk;
        }), Util.backgroundExecutor());
    }

    /**
     * Creates basic terrain.
     * @param executor method executor.
     * @param blender noise blender.
     * @param noiseConfig noise config.
     * @param structureAccessor structure accessor.
     * @param chunk chunk to process.
     */
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState noiseConfig,
                                                        StructureManager structureAccessor, ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    /**
     * Shapes surface, built on previous step.
     * @param region chunk region.
     * @param structures structures to place.
     * @param noiseConfig noise config.
     * @param chunk chunk to process.
     */
    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState noiseConfig,
                             ChunkAccess chunk) {
        // Save chunk position
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // Prepare block position
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Sea level is now stored in generator settings
        int seaLevel = this.settings.value().seaLevel();
        // We also need bottom Y
        int minY = this.getMinY();

        // We need biomes the second time (now for real, I swear)
        // TODO: remove this call and choose blocks according to biome at given location
        this.biomes = betaChunkManager.generateBiomeInfo(biomes,
                chunkX * 16, chunkZ * 16,
                16, 16);

        // We also need random class with seed derived from chunk coordinates
        Random rand = new Random((long)chunkX * 341873128712L + (long)chunkZ * 132897987541L);

        // ======================================================================================================
        // In Vanilla Beta 1.7.3 this section is done by ChunkProviderGenerate.replaceBlocksForBiome(...) method.
        // ======================================================================================================

        double scale = 0.03125; // Original code: double scale = 1.0D / 32.0D;

        // Different terrain block noises
        this.sandNoise = this.beachOctaveNoise.generateNoiseOctaves(this.sandNoise,
                (chunkX * 16), (chunkZ * 16), 0.0D,
                16, 16, 1,
                scale, scale, 1.0D);
        this.gravelNoise = this.beachOctaveNoise.generateNoiseOctaves(this.gravelNoise,
                (chunkX * 16), 109.0134D, (chunkZ * 16),
                16, 1, 16,
                scale, 1.0D, scale);
        this.stoneNoise = this.surfaceOctaveNoise.generateNoiseOctaves(this.stoneNoise,
                (chunkX * 16), (chunkZ * 16), 0.0D,
                16, 16, 1,
                scale * 2.0D, scale * 2.0D, scale * 2.0D);

        // Loop over Ox and Oz
        for(int localZ = 0; localZ < 16; ++localZ) {
            for(int localX = 0; localX < 16; ++localX) {
                pos.set(localX, 0, localZ);

                // TODO: get valid biome here from chunk itself
                BiomeGenBase var10 = this.biomes[localZ + localX * 16];
                boolean var11 = this.sandNoise[localZ + localX * 16] + rand.nextDouble() * 0.2D > 0.0D;
                boolean var12 = this.gravelNoise[localZ + localX * 16] + rand.nextDouble() * 0.2D > 3.0D;
                int surfaceDepth = (int)(this.stoneNoise[localZ + localX * 16] / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
                int y = -1;
                Block block1 = var10.topBlock;
                Block block2 = var10.fillerBlock;

                // Loop over Oy
                for(int surfaceTopY = 127; surfaceTopY >= 0; --surfaceTopY) {
                    pos.set(localX, surfaceTopY, localZ);

                    if(surfaceTopY <= minY + rand.nextInt(5)) {
                        chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), false);
                    } else {
                        BlockState block3 = chunk.getBlockState(pos);
                        if(block3.isAir()) {
                            y = -1;
                        } else if(block3.is(Blocks.STONE)) {
                            if(y == -1) {
                                if(surfaceDepth <= 0) {
                                    block1 = Blocks.AIR;
                                    block2 = Blocks.STONE;
                                } else if(surfaceTopY >= seaLevel - 4 && surfaceTopY <= seaLevel + 1) {
                                    block1 = var10.topBlock;
                                    block2 = var10.fillerBlock;

                                    if(var12) {
                                        block1 = Blocks.AIR;
                                        block2 = Blocks.GRAVEL;
                                    }

                                    if(var11) {
                                        block1 = Blocks.SAND;
                                        block2 = Blocks.SAND;
                                    }
                                }

                                if(surfaceTopY < seaLevel && block1.defaultBlockState().isAir()) {
                                    block1 = Blocks.WATER;
                                }

                                y = surfaceDepth;
                                if(surfaceTopY >= seaLevel - 1) {
                                    chunk.setBlockState(pos, block1.defaultBlockState(), false);
                                } else {
                                    chunk.setBlockState(pos, block2.defaultBlockState(), false);
                                }
                            } else if(y > 0) {
                                --y;
                                chunk.setBlockState(pos, block2.defaultBlockState(), false);
                                if((y == 0) && (block2 == Blocks.SAND)) {
                                    y = rand.nextInt(4);
                                    block2 = Blocks.SANDSTONE;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates caves.
     * @param region chunk region.
     * @param seed generation seed.
     * @param noiseConfig cave noise.
     * @param biomeAccess biomes.
     * @param structureAccessor structures.
     * @param chunk chunk.
     * @param carverStep generation step.
     */
    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState noiseConfig, BiomeManager biomeAccess,
                             StructureManager structureAccessor, ChunkAccess chunk,
                             GenerationStep.Carving carverStep) {
        super.applyCarvers(region, seed, noiseConfig, biomeAccess, structureAccessor, chunk, carverStep);
    }

    /**
     * Populates chunk with creatures.
     * @param region chunk region.
     */
    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        if (!this.settings.value().disableMobGeneration()) {
            ChunkPos chunkpos = region.getCenter();
            Holder<Biome> holder = region.getBiome(chunkpos.getWorldPosition().atY(region.getMaxBuildHeight() - 1));
            WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
            worldgenrandom.setDecorationSeed(region.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
            NaturalSpawner.spawnMobsForChunkGeneration(region, holder, chunkpos, worldgenrandom);
        }
    }

    /**
     * Get terrain column at given X and Z coordinates in a form of a column.
     * @param x X block coordinate.
     * @param z Z block coordinate.
     * @param heightView chunk, world or anything that implements HeightLimitView.
     * @param noiseConfig noise config.
     * @return column sample at given coordinates
     */
    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightView, RandomState noiseConfig) {
        // Get height at this point from heightmap
        int height = this.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, heightView, noiseConfig);

        // Get world properties
        int worldHeight = this.getGenDepth();
        int minY = this.getMinY();

        // Init block column
        BlockState[] column = new BlockState[worldHeight];

        // Iterate over column
        for (int i = worldHeight - 1; i >= 0; --i) {
            // Column index to Y
            int y = i + minY;

            // > height on heightmap
            if (y > height) {
                // Air or water
                if (y > this.getSeaLevel()) {
                    column[i] = Blocks.AIR.defaultBlockState();
                }
                else {
                    column[i] = this.settings.value().defaultFluid();
                }
            // <= height on heightmap
            } else {
                // Set default block
                column[i] = this.settings.value().defaultBlock();
            }
        }

        return new NoiseColumn(minY, column);
    }

    /**
     * Get terrain height at given X and Z coordinates.
     * @param x X block coordinate.
     * @param z Z block coordinate.
     * @param heightmap heightmap type.
     * @param heightView chunk, world or anything that implements HeightLimitView.
     * @param noiseConfig noise config.
     * @return height at given coordinates
     */
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor heightView,
                             RandomState noiseConfig) {
        // Get height from proto-chunk heightmap
        if (heightView instanceof ProtoChunk chunk){
            Heightmap chunkHeightmap = chunk.getOrCreateHeightmapUnprimed(heightmap);
            int sum = 0;
            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    sum += chunkHeightmap.getFirstAvailable(
                            SectionPos.sectionRelative(x+i),
                            SectionPos.sectionRelative(z+j));
                }
            }
            return sum/25;
        }
        // By default
        else {
            // Return something stupid to notice in game
            return 128;
        }
    }
}
