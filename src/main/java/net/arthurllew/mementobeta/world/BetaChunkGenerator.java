package net.arthurllew.mementobeta.world;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arthurllew.mementobeta.world.biome.BetaBiomeSupplier;
import net.arthurllew.mementobeta.world.biome.BetaClimateMap;
import net.arthurllew.mementobeta.world.levelgen.*;
import net.arthurllew.mementobeta.world.util.ChunkGenCache;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class BetaChunkGenerator extends NoiseBasedChunkGenerator {
    /**
     * Codec.
     */
    public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create((values) -> values.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(
                    ChunkGenerator::getBiomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(
                    NoiseBasedChunkGenerator::generatorSettings)
    ).apply(values, values.stable(BetaChunkGenerator::new)));

    /**
     * World seed.
     */
    private long worldSeed;

    /**
     * Random provider (like in Beta 1.7.3).
     */
    private final Random rand = new Random();

    // Noises
    private double[] sandNoise = new double[256];
    private double[] gravelNoise = new double[256];
    private double[] stoneNoise = new double[256];

    // Auxiliary temperatures
    private final double[] temperatures = new double[256];

    /**
     * Chunk generator cache.
     */
    public final ChunkGenCache chunkGenCache;

    /**
     * Beta 1.7.3 climate sampler.
     */
    public BetaClimateSampler betaClimateSampler;
    /**
     * Beta 1.7.3 terrain sampler.
     */
    public BetaTerrainSampler betaTerrainSampler;

    /**
     * Beta 1.7.3 cave carver.
     */
    public WorldGenCaves betaCaveCarver = new WorldGenCaves();

    /**
     * Constructor.
     * @param biomeSource biome provider.
     * @param settings generator settings.
     */
    BetaChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);

        // Inject reference to this generator into biome source (used to access generator cache)
        ((BetaBiomeSupplier)this.biomeSource).setGenerator(this);

        // Init chunk generator cache
        this.chunkGenCache = new ChunkGenCache(this);
    }

    /**
     * Set generator seed, init random class and noise generators.
     * @param seed world seed.
     */
    public void setSeed(long seed) {
        // Init samplers
        this.betaClimateSampler = new BetaClimateSampler(seed);
        this.betaTerrainSampler = new BetaTerrainSampler(seed);

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
        return this.generatorSettings().value().noiseSettings().height();
    }

    /**
     * Get world sea level from generator settings.
     * @return sea level
     */
    @Override
    public int getSeaLevel() {
        return this.generatorSettings().value().seaLevel();
    }

    /**
     * Get world minimum Y position from generator settings.
     * @return world minimum Y position
     */
    @Override
    public int getMinY() {
        return this.generatorSettings().value().noiseSettings().minY();
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
     * Creates basic terrain from noise.
     * @param executor method executor.
     * @param blender noise blender.
     * @param noiseConfig noise config.
     * @param structureAccessor structure accessor.
     * @param chunk chunk to process.
     */
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState noiseConfig,
                                                        StructureManager structureAccessor, ChunkAccess chunk) {
        // Occupy all chunk sections
        Set<LevelChunkSection> chunkSections = Sets.newHashSet();
        for(LevelChunkSection chunkSection : chunk.getSections()) {
            chunkSection.acquire();
            chunkSections.add(chunkSection);
        }

        // Schedule chung terrain generation
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("wgen_fill_noise",
                () -> this.generateTerrain(chunk)),
                Util.backgroundExecutor()).whenCompleteAsync((p_224309_, p_224310_) -> {
                    for(LevelChunkSection chunkSection : chunkSections) {
                        chunkSection.release();
                    }
                },
                executor);
    }

    /**
     * Generates base (stone & water) terrain of Beta 1.7.3.
     * @param chunk chunk to process.
     * @return provided chunk.
     */
    public ChunkAccess generateTerrain(ChunkAccess chunk) {
        // For the future use in structure placement we need to fill in heightmaps in a chunk
        Heightmap heightmapOceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmapSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        // Chunk position
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // Get generation cached data
        ChunkGenCache.GenData genData = chunkGenCache.get(chunkX, chunkZ);

        // Generate terrain
        betaTerrainSampler.sampleTerrain(genData.terrainNoise(), this.generatorSettings().value().seaLevel(),
                (x, y, z, blockState) -> {
                    // Set block and update heightmap
                    int localX = SectionPos.sectionRelative(x);
                    int localY = SectionPos.sectionRelative(y);
                    int localZ = SectionPos.sectionRelative(z);
                    chunk.getSection(chunk.getSectionIndex(y))
                            .setBlockState(localX, localY, localZ, blockState, false);
                    heightmapOceanFloor.update(x, y, z, blockState);
                    heightmapSurface.update(x, y, z, blockState);
                });

        return chunk;
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

        // Get generation cached data
        ChunkGenCache.GenData genData = chunkGenCache.get(chunkX, chunkZ);

        // Prepare block position
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Sea level is now stored in generator settings
        int seaLevel = this.generatorSettings().value().seaLevel();
        // We also need bottom Y
        int minY = this.getMinY();

        // We also need random class with seed derived from chunk coordinates
        Random rand = new Random((long)chunkX * 341873128712L + (long)chunkZ * 132897987541L);

        // ======================================================================================================
        // In Vanilla Beta 1.7.3 this section is done by ChunkProviderGenerate.replaceBlocksForBiome(...) method.
        // ======================================================================================================

        double scale = 0.03125; // Original code: double scale = 1.0D / 32.0D;

        // Noises for sand/gravel beaches and places, where there are no top blocks and stone can be seen
        this.sandNoise = this.betaTerrainSampler.beachOctaveNoise.sample(this.sandNoise,
                (chunkX * 16), (chunkZ * 16), 0.0D,
                16, 16, 1,
                scale, scale, 1.0D);
        this.gravelNoise = this.betaTerrainSampler.beachOctaveNoise.sample(this.gravelNoise,
                (chunkX * 16), 109.0134D, (chunkZ * 16),
                16, 1, 16,
                scale, 1.0D, scale);
        this.stoneNoise = this.betaTerrainSampler.surfaceOctaveNoise.sample(this.stoneNoise,
                (chunkX * 16), (chunkZ * 16), 0.0D,
                16, 16, 1,
                scale * 2.0D, scale * 2.0D, scale * 2.0D);

        // Loop over chunk-local Ox and Oz
        for(int localX = 0; localX < 16; localX++) {
            for(int localZ = 0; localZ < 16; localZ++) {
                pos.set(localX, 0, localZ);

                // Get biome specific top blocks
                BetaClimateMap.BiomeTopLayerBlocks biomeTopLayerBlocks =
                        BetaClimateMap.getFromClimate(genData.climate()[localX * 16 + localZ]);
                Block block1 = biomeTopLayerBlocks.topBlock();
                Block block2 = biomeTopLayerBlocks.fillerBlock();

                int surfaceDepth = (int)(this.stoneNoise[localX * 16 + localZ] / 3.0D + 3.0D + rand.nextDouble() * 0.25D);

                int airAbove = -1;

                // Loop over chunk-local Oy
                for(int surfaceTopY = 127; surfaceTopY >= 0; --surfaceTopY) {
                    pos.set(localX, surfaceTopY, localZ);

                    // Bedrock
                    if(surfaceTopY <= minY + rand.nextInt(5)) {
                        chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), false);
                    }
                    // Beaches and stone patches
                    else {
                        // get block
                        BlockState block3 = chunk.getBlockState(pos);

                        // Air flag
                        if(block3.isAir()) {
                            airAbove = -1;
                        }
                        // If block is stone
                        else if(block3.is(Blocks.STONE)) {
                            // Air block above
                            if(airAbove == -1) {
                                // Carve into terrain and reveal stone
                                if(surfaceDepth <= 0) {
                                    block1 = Blocks.AIR;
                                    block2 = Blocks.STONE;
                                }
                                // Beach
                                else if(surfaceTopY >= seaLevel - 4 && surfaceTopY <= seaLevel + 1) {
                                    // Get biome top layer blocks
                                    block1 = biomeTopLayerBlocks.topBlock();
                                    block2 = biomeTopLayerBlocks.fillerBlock();

                                    // Gravel beach?
                                    if(this.gravelNoise[localX * 16 + localZ] + rand.nextDouble() * 0.2D > 3.0D) {
                                        block1 = Blocks.AIR;
                                        block2 = Blocks.GRAVEL;
                                    }

                                    // Sand beach?
                                    if(this.sandNoise[localX * 16 + localZ] + rand.nextDouble() * 0.2D > 0.0D) {
                                        block1 = Blocks.SAND;
                                        block2 = Blocks.SAND;
                                    }
                                }

                                // Replace with water if below sea level and block is air
                                if(surfaceTopY < seaLevel && block1.defaultBlockState().isAir()) {
                                    block1 = Blocks.WATER;
                                }

                                // Place blocks
                                airAbove = surfaceDepth;
                                if(surfaceTopY >= seaLevel - 1) {
                                    chunk.setBlockState(pos, block1.defaultBlockState(), false);
                                } else {
                                    chunk.setBlockState(pos, block2.defaultBlockState(), false);
                                }
                            }
                            else if(airAbove > 0) {
                                --airAbove;

                                // Place second top layer block (dirt/sand)
                                chunk.setBlockState(pos, block2.defaultBlockState(), false);
                                // Place sandstone below sand
                                if((airAbove == 0) && (block2 == Blocks.SAND)) {
                                    airAbove = rand.nextInt(4);
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
        // Apply beta cave carver
        betaCaveCarver.generate(chunk, worldSeed);

        //super.applyCarvers(region, seed, noiseConfig, biomeAccess, structureAccessor, chunk, carverStep);
    }

    /**
     * Generates biome decorations like trees, flowers and so on.
     * @param genRegion world region of 3x3 chunks.
     * @param chunk chunk.
     * @param structureManager structure manager.
     */
    public void applyBiomeDecoration(WorldGenLevel genRegion, ChunkAccess chunk, StructureManager structureManager) {
        //=====================================================================================================
        // In Vanilla Beta 1.7.3 chunk decoration is done by ChunkProviderGenerate.populate(...) method.
        // Water/Lava lakes are generated first, followed by dungeons. Then mobs/trees/grass and other
        // decoration items are placed. At the end the snow layer is generated. We will only mimic water/lava
        // lakes, dungeons and snow. Other decorations will be provided by biome. Although it will not
        // reproduce Vanilla Beta 1.7.3 foliage setup, I believe modern biome decorations are better.
        //=====================================================================================================

        // Chunk position
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // World position
        int x = chunk.getPos().x * 16;
        int z = chunk.getPos().z * 16;

        // Set random seed
        this.rand.setSeed(this.worldSeed);
        long v1 = this.rand.nextLong() / 2L * 2L + 1L;
        long v2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed((long)chunkX * v1 + (long)chunkZ * v2 ^ this.worldSeed);

        int genX;
        int genY;
        int genZ;

        // Try to generate water lake
        if(this.rand.nextInt(4) == 0) {
            genX = x + this.rand.nextInt(16) + 8;
            genY = this.rand.nextInt(128);
            genZ = z + this.rand.nextInt(16) + 8;
            WorldGenLakes.generate(genRegion, this.rand, genX, genY, genZ, Blocks.WATER);
        }

        // Try to generate lava lake
        if(this.rand.nextInt(8) == 0) {
            genX = x + this.rand.nextInt(16) + 8;
            genY = this.rand.nextInt(this.rand.nextInt(120) + 8);
            genZ = z + this.rand.nextInt(16) + 8;
            if(genY < 64 || this.rand.nextInt(10) == 0) {
                WorldGenLakes.generate(genRegion, this.rand, genX, genY, genZ, Blocks.LAVA);
            }
        }

        // Try to generate dungeon
        for(int var16 = 0; var16 < 8; ++var16) {
            genX = x + this.rand.nextInt(16) + 8;
            genY = this.rand.nextInt(128);
            genZ = z + this.rand.nextInt(16) + 8;
            WorldGenDungeons.generate(genRegion, this.rand, genX, genY, genZ);
        }

        // Trees/grass and other biome decorations via modern methods
        super.applyBiomeDecoration(genRegion, chunk, structureManager);

        // Get additional temperatures
        betaClimateSampler.sampleTemperatures(this.temperatures, x, z, 16, 16);
        // Generate Beta 1.7.3 snow layer
        // In Vanilla Beta 1.7.3 X and Z coordinates are for some reason shifted by 8, but we would rather
        // safely calculate temperature and generate snow in current chunk.
        int idx = 0;
        for(int i = x; i < x + 16; i++) {
            for(int j = z; j < z + 16; j++) {
                // Find surface y (noise config is not used anyway so f it)
                int y = this.getBaseHeight(i, j, Heightmap.Types.WORLD_SURFACE_WG, chunk, null);

                // Calculate corresponding temperature
                double temperature = this.temperatures[idx] - (double)(y - 64) / 64.0D * 0.3D;

                BlockPos blockPos = new BlockPos(i, y, j);
                BlockPos blockPosBelow = new BlockPos(i, y - 1, j);

                // if temperature is below 0.5, this position is not occupied, block below is solid and not ice
                if(temperature < 0.5D
                        && chunk.getBlockState(blockPos).isAir()
                        && chunk.getBlockState(blockPosBelow).isSolid()
                        && chunk.getBlockState(blockPosBelow) != Blocks.ICE.defaultBlockState()) {
                    // Set snow
                    chunk.setBlockState(blockPos, Blocks.SNOW.defaultBlockState(), false);
                }

                idx++;
            }
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
                    column[i] = this.generatorSettings().value().defaultFluid();
                }
            // <= height on heightmap
            } else {
                // Set default block
                column[i] = this.generatorSettings().value().defaultBlock();
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
            // Chunk position
            int chunkX = chunk.getPos().x;
            int chunkZ = chunk.getPos().z;

            // Get generation cached data
            ChunkGenCache.GenData genData = chunkGenCache.get(chunkX, chunkZ);

            // Get height
            int height = genData.heightmap().getHeight(x & 15, z & 15);
            // Get sea level
            int seaLevel = getSeaLevel();

            // If heightmap is of "world surface" clamp height by sea level
            if (heightmap == Heightmap.Types.WORLD_SURFACE_WG && height <= seaLevel) {
                return seaLevel + 1;
            }
            // Else return height
            else {
                return height;
            }
        }
        // By default
        else {
            // Return something stupid to notice in game
            return 128;
        }
    }
}
