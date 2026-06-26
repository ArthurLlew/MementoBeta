package net.arthurllew.mementobeta.world.levelgen;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import net.arthurllew.mementobeta.world.biome.BetaBiomeSource;
import net.arthurllew.mementobeta.world.biome.BetaClimateMap;
import net.arthurllew.mementobeta.world.biome.BetaClimateSampler;
import net.arthurllew.mementobeta.world.levelgen.carver.BetaCavesCarver;
import net.arthurllew.mementobeta.world.levelgen.noise.BetaTerrainNoiseSampler;
import net.arthurllew.mementobeta.world.levelgen.util.ChunkGenCache;
import net.arthurllew.mementobeta.world.levelgen.util.Consumer4;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
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
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BetaChunkGenerator extends NoiseBasedChunkGenerator {
    /**
     * Codec.
     */
    public static final MapCodec<BetaChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
        values -> values.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(
                    ChunkGenerator::getBiomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(
                    BetaChunkGenerator::generatorSettings),
            BetaChunkGeneratorSettings.CODEC.fieldOf("beta_settings")
                    .forGetter((generator) -> generator.betaSettings)
        ).apply(values, values.stable(BetaChunkGenerator::new)));

    /**
     * World seed.
     */
    protected long worldSeed;

    // Noises
    protected double[] sandNoise = new double[256];
    protected double[] gravelNoise = new double[256];
    protected double[] stoneNoise = new double[256];

    /**
     * Custom generator settings.
     */
    public Holder<BetaChunkGeneratorSettings> betaSettings;

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
    public BetaTerrainNoiseSampler betaTerrainNoiseSampler;

    /**
     * Sampler for additional terrain level 1.
     */
    NormalNoise subsurfaceSampler;

    /**
     * Beta 1.7.3 cave carver.
     */
    public final BetaCavesCarver betaCaveCarver;

    /**
     * Constructor.
     *
     * @param biomeSource biome provider.
     * @param settings generator settings.
     * @param betaSettings generator custom settings.
     */
    BetaChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings,
                       Holder<BetaChunkGeneratorSettings> betaSettings) {
        super(biomeSource, settings);

        // Inject reference to this generator into biome source (used to access generator cache)
        ((BetaBiomeSource)this.biomeSource).setGenerator(this);

        // Init chunk generator cache
        this.chunkGenCache = new ChunkGenCache(this);
        // and custom carver
        this.betaCaveCarver = new BetaCavesCarver(this);

        // Custom settings
        this.betaSettings = betaSettings;
    }

    /**
     * Set generator seed, init random class and noise generators.
     *
     * @param seed world seed.
     */
    public void setSeed(long seed) {
        // Init samplers
        this.betaClimateSampler = new BetaClimateSampler(seed);
        this.betaTerrainNoiseSampler = new BetaTerrainNoiseSampler(seed);
        double[] a = {1, 1, 0, 0, 1, 1};
        subsurfaceSampler = NormalNoise.create(new LegacyRandomSource(this.worldSeed),
                new NormalNoise.NoiseParameters(-1, new DoubleArrayList(a)));

        // Save world seed
        this.worldSeed = seed;
    }

    /**
     * @return codec
     */
    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    /**
     * @return world height
     */
    @Override
    public int getGenDepth() {
        return this.generatorSettings().value().noiseSettings().height();
    }

    /**
     *
     * @return sea level
     */
    @Override
    public int getSeaLevel() {
        return this.generatorSettings().value().seaLevel();
    }

    /**
     *
     * @return world minimum Y position
     */
    @Override
    public int getMinY() {
        return this.generatorSettings().value().noiseSettings().minY();
    }

    /**
     * Generate info in debug menu.
     *
     * @param info string list.
     * @param random noise config.
     * @param pos player position.
     */
    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {}

    /**
     * Creates structures. The 1rst step of terrain generation.
     *
     * @param chunk chunk to process.
     */
    @Override
    public void createStructures(RegistryAccess registryAccess, ChunkGeneratorStructureState structureState,
                                 StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager
                                 structureTemplateManager) {
        // For the future use in structure placement we need to fill in heightmaps
        Heightmap heightmapOceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmapSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        // Chunk position
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // Get cached generation data
        ChunkGenCache.GenData genData = chunkGenCache.get(chunkX, chunkZ);

        // Update heightmap
        sampleTerrain(genData.terrainNoise(), this.generatorSettings().value().seaLevel(),
                (x, y, z, blockState) -> {
                    heightmapOceanFloor.update(x, y, z, blockState);
                    heightmapSurface.update(x, y, z, blockState);
                });

        // Generate structures as normal
        super.createStructures(registryAccess, structureState, structureManager, chunk, structureTemplateManager);
    }

    /**
     * Creates basic terrain from noise. The 4th step of terrain generation.
     *
     * @param blender noise blender.
     * @param randomState generator random.
     * @param structureManager structure manager.
     * @param chunk chunk to process.
     */
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        // Occupy all chunk sections
        Set<LevelChunkSection> chunkSections = Sets.newHashSet();
        for(LevelChunkSection chunkSection : chunk.getSections()) {
            chunkSection.acquire();
            chunkSections.add(chunkSection);
        }

        // Schedule chunk terrain generation
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("wgen_fill_noise",
                        () -> this.generateTerrain(chunk)),
                Util.backgroundExecutor()).whenCompleteAsync((p_224309_, p_224310_) -> {
                    for(LevelChunkSection chunkSection : chunkSections) {
                        chunkSection.release();
                    }
                },
                Util.backgroundExecutor());
    }

    /**
     * Generates base (stone & water) terrain of Beta 1.7.3.
     *
     * @param chunk chunk to process.
     *
     * @return provided chunk.
     */
    public ChunkAccess generateTerrain(ChunkAccess chunk) {
        // Chunk position
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // Get cached generation data
        ChunkGenCache.GenData genData = chunkGenCache.get(chunkX, chunkZ);

        // Generate terrain
        sampleTerrain(genData.terrainNoise(), this.generatorSettings().value().seaLevel(),
                (x, y, z, blockState) -> {
                    // Set block
                    int localX = SectionPos.sectionRelative(x);
                    int localY = SectionPos.sectionRelative(y);
                    int localZ = SectionPos.sectionRelative(z);
                    chunk.getSection(chunk.getSectionIndex(y))
                            .setBlockState(localX, localY, localZ, blockState, false);
                });

        return chunk;
    }

    /**
     * Applies provided action inside Beta 1.7.3 terrain generation process. Is used to sample heightmaps and
     * generate surface.
     *
     * @param terrainNoise Beta 1.7.3 terrain noise.
     * @param seaLevel sea level.
     * @param genAction generation action.
     */
    @SuppressWarnings({"PointlessArithmeticExpression", "DuplicateExpressions"})
    public void sampleTerrain(double[] terrainNoise, int seaLevel,
                              Consumer4<Integer, Integer, Integer, BlockState> genAction) {
        // ================================================================================================
        // In Vanilla Beta 1.7.3 this section is done by ChunkProviderGenerate.generateTerrain(...) method.
        // ================================================================================================

        // Those are initialized at the beginning of ChunkProviderGenerate.generateTerrain(...) method.
        byte sizeHorizontal = 4;
        byte sizeVertical = 16;
        //int sizeX = sizeHorizontal + 1;
        int sizeY = sizeVertical + 1;
        int sizeZ = sizeHorizontal + 1;

        // Sea level is stored in generator settings
        //int seaLevel = this.generatorSettings().value().seaLevel();

        // Generate terrain
        for(int sectionX = 0; sectionX < sizeHorizontal; ++sectionX) {
            for(int sectionZ = 0; sectionZ < sizeHorizontal; ++sectionZ) {
                for(int sectionY = 0; sectionY < sizeVertical; ++sectionY) {
                    // Get noises
                    double noise1 = terrainNoise[((sectionX + 0) * sizeZ + sectionZ + 0) * sizeY + sectionY + 0];
                    double noise2 = terrainNoise[((sectionX + 0) * sizeZ + sectionZ + 1) * sizeY + sectionY + 0];
                    double noise3 = terrainNoise[((sectionX + 1) * sizeZ + sectionZ + 0) * sizeY + sectionY + 0];
                    double noise4 = terrainNoise[((sectionX + 1) * sizeZ + sectionZ + 1) * sizeY + sectionY + 0];
                    double noiseDelta1 =
                            (terrainNoise[((sectionX + 0) * sizeZ + sectionZ + 0) * sizeY + sectionY + 1] - noise1)
                                    * 0.125D;
                    double noiseDelta2 =
                            (terrainNoise[((sectionX + 0) * sizeZ + sectionZ + 1) * sizeY + sectionY + 1] - noise2)
                                    * 0.125D;
                    double noiseDelta3 =
                            (terrainNoise[((sectionX + 1) * sizeZ + sectionZ + 0) * sizeY + sectionY + 1] - noise3)
                                    * 0.125D;
                    double NoiseDelta4 =
                            (terrainNoise[((sectionX + 1) * sizeZ + sectionZ + 1) * sizeY + sectionY + 1] - noise4)
                                    * 0.125D;

                    for(int localY = 0; localY < 8; ++localY) {
                        // Pre-density values
                        double preDensity1 = noise1;
                        double preDensity2 = noise2;
                        double preDensityDelta1 = (noise3 - noise1) * 0.25D;
                        double preDensityDelta2 = (noise4 - noise2) * 0.25D;

                        for(int localX = 0; localX < 4; ++localX) {
                            // Density values
                            double density = preDensity1;
                            double densityDelta = (preDensity2 - preDensity1) * 0.25D;

                            for(int localZ = 0; localZ < 4; ++localZ) {

                                // Choose block
                                Block block;
                                if(density > 0.0D) {
                                    // Stone for any density > 0
                                    block = this.betaSettings.value().stoneBlock();
                                }
                                else
                                {
                                    // Set water if below sea level
                                    if(localY + sectionY * 8 < seaLevel) {
                                        block = Blocks.WATER;
                                    }
                                    // Air otherwise
                                    else {
                                        block = Blocks.AIR;
                                    }
                                }

                                // Generation action (e.g. set block or/and update heightmap)
                                genAction.accept(localX + sectionX * 4,
                                        localY + sectionY * 8,
                                        localZ + sectionZ * 4,
                                        block.defaultBlockState());

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
    }

    /**
     * Shapes surface, built on previous step. The 5th step of terrain generation.
     *
     * @param region chunk region
     * @param structures structures to place
     * @param noiseConfig noise config
     * @param chunk chunk to process
     */
    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState noiseConfig,
                             ChunkAccess chunk) {
        // Save chunk position
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // Get cached generation data
        ChunkGenCache.GenData genData = this.chunkGenCache.get(chunkX, chunkZ);

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

        double scale = 0.03125D; // Original code: double scale = 1.0D / 32.0D;

        // Noises for sand/gravel beaches and places, where there are no top blocks and stone can be seen
        this.sandNoise = this.betaTerrainNoiseSampler.beachOctaveNoise.sample(this.sandNoise,
                (chunkX * 16), (chunkZ * 16), 0.0D,
                16, 16, 1,
                scale, scale, 1.0D);
        this.gravelNoise = this.betaTerrainNoiseSampler.beachOctaveNoise.sample(this.gravelNoise,
                (chunkX * 16), 109.0134D, (chunkZ * 16),
                16, 1, 16,
                scale, 1.0D, scale);
        this.stoneNoise = this.betaTerrainNoiseSampler.surfaceOctaveNoise.sample(this.stoneNoise,
                (chunkX * 16), (chunkZ * 16), 0.0D,
                16, 16, 1,
                scale * 2.0D, scale * 2.0D, scale * 2.0D);

        // For some reason in Beta 1.7.3 code coordinates in this nested loop are iterated in reverse...
        // or noise is generated in reverse? Whatever the case, my testing shown, that it doesn't affect
        // block horizontal placement and only matters for the order of random class being called, which
        // is responsible for vertical block placement. For example, this effects sandstone vertical
        // distribution. The latter is very important for correct generation replication, so in order
        // to retain same look and don't mess up everything else, we will just reorder the loop.
        for(int localZ = 0; localZ < 16; localZ++) {
            for(int localX = 0; localX < 16; localX++) {
                // Get biome top block
                Block biomeBlock = BetaClimateMap.getBiomeFromTable(genData.climate()[localX * 16 + localZ]).topBlock;

                // Determine beach and stone patch noises
                boolean isGravel = this.gravelNoise[localX * 16 + localZ] + rand.nextDouble() * 0.2D > 3.0D;
                boolean isSand = this.sandNoise[localX * 16 + localZ] + rand.nextDouble() * 0.2D > 0.0D;
                int depth = (int)(this.stoneNoise[localX * 16 + localZ] / 3.0D + 3.0D + rand.nextDouble() * 0.25D);

                // Surface top block
                Block blockTop = biomeBlock;
                // Block bellow it
                Block blockBelow = blockTop;

                // Flag of previous block being air
                int airAbove = -1;

                // Loop over chunk-local Oy
                for(int localY = 127; localY >= minY; localY--) {
                    // Set block position
                    pos.set(localX, localY, localZ);

                    // Bedrock
                    if(localY <= minY + rand.nextInt(5)) {
                        chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), false);
                    }
                    // Basic upper terrain, beaches and stone patches
                    else {
                        // Get block at observed position
                        BlockState block3 = chunk.getBlockState(pos);

                        // Air flag
                        if(block3.isAir()) {
                            // Reset block state, if we hit air gap inside terrain
                            // (to ensure andesite is not being placed instead normal top blocks)
                            if (airAbove >= 0)
                                blockBelow = blockTop;

                            airAbove = -1;
                        }
                        // If block is stone
                        else if(block3.is(this.betaSettings.value().stoneBlock())) {
                            // Air block above
                            if(airAbove == -1) {
                                // Carve into terrain and reveal stone
                                if(depth <= 0) {
                                    blockTop = Blocks.AIR;
                                    blockBelow = this.betaSettings.value().stoneBlock();
                                }
                                // Basic terrain or beach
                                else if(localY >= seaLevel - 4 && localY <= seaLevel + 1) {
                                    // Biome related blocks
                                    blockTop = biomeBlock;
                                    blockBelow = blockTop;

                                    // (Beta 1.7.3 preferred sand to gravel in beach generation).
                                    // If there is sand beach
                                    if(isSand) {
                                        blockTop = Blocks.SAND;
                                        blockBelow = Blocks.SAND;
                                    }
                                    // Alternatively if there is gravel beach
                                    else if(isGravel) {
                                        blockTop = Blocks.AIR;
                                        blockBelow = Blocks.GRAVEL;
                                    }
                                }

                                // Avoid air at sea level
                                if(localY < seaLevel && blockTop == Blocks.AIR) {
                                    blockTop = Blocks.WATER;
                                }

                                airAbove = depth;

                                // Place blocks depending on sea level
                                if(localY >= seaLevel - 1) {
                                    chunk.setBlockState(pos, blockTop.defaultBlockState(), false);
                                } else {
                                    chunk.setBlockState(pos, blockBelow.defaultBlockState(), false);
                                }
                            }
                            else if(airAbove > 0) {
                                airAbove--;

                                // Place second top layer block (dirt/sand)
                                chunk.setBlockState(pos, blockBelow.defaultBlockState(), false);
                                // Place sandstone below sand
                                if((airAbove == 0) && (blockBelow == Blocks.SAND)) {
                                    airAbove = rand.nextInt(4);
                                    blockBelow = this.betaSettings.value().belowTopOneDesert();
                                }
                                // Extra blocks below dirt for smoothness of terrain
                                if((airAbove == 0) && (blockBelow == Blocks.CRYING_OBSIDIAN)) {
                                    airAbove = genSubsurfaceLayerDensity(chunk,
                                            localX, localY, localZ,
                                            2, 0.3, 0.3);
                                    blockBelow = this.betaSettings.value().belowTopOne();
                                }
                                // Extra blocks below sandstone and packed dirt for even more smoothness :)
                                if((airAbove == 0) && ((blockBelow == this.betaSettings.value().belowTopOne())
                                                       || (blockBelow == this.betaSettings.value().belowTopOneDesert()))) {
                                    airAbove = genSubsurfaceLayerDensity(chunk,
                                            localX + 8, localY, localZ + 8,
                                            1, 0.15, 0.35);
                                    blockBelow = this.betaSettings.value().belowTopTwo();
                                }
                            }
                        }
                    }
                }
            }
        }

        // Run modern surface building to include so-called "surface rule" (see noise_settings JSON files)
        super.buildSurface(region, structures, noiseConfig, chunk);
    }

    /**
     * @param chunk chunk.
     * @param x chunk local X
     * @param y chunk local Y
     * @param z chunk local Z
     * @param base base value of density
     * @param noiseThresholdMin controls noise clamping min
     * @param noiseThresholdMax controls noise clamping max
     *
     * @return sampled sub surface layer density (from base-1 to base+1)
     */
    protected int genSubsurfaceLayerDensity(ChunkAccess chunk, int x, int y, int z,
                                            int base, double noiseThresholdMin, double noiseThresholdMax)
    {
        // Sample noise at world positions
        double val = this.subsurfaceSampler.getValue(
                x + ((long)chunk.getPos().x) * 16,
                y,
                z + ((long)chunk.getPos().z) * 16);

        // Modify base value by clamped noise of sub-surface level density
        return Math.abs(base + ((val < -noiseThresholdMin) ? -1 : (val > noiseThresholdMax ? 1 : 0)));
    }

    /**
     * Generates caves. The 6th step of terrain generation.
     *
     * @param region chunk region
     * @param seed generation seed
     * @param noiseConfig cave noise
     * @param biomeAccess biomes
     * @param structureAccessor structures
     * @param chunk chunk
     * @param carverStep generation step
     */
    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState noiseConfig, BiomeManager biomeAccess,
                             StructureManager structureAccessor, ChunkAccess chunk,
                             GenerationStep.Carving carverStep) {
        // Apply beta cave carver
        this.betaCaveCarver.carve(chunk, this.worldSeed);
    }

    /**
     * Generates biome decorations like trees, flowers and so on. The 7th step of terrain generation.
     *
     * @param genRegion world region of 3x3 chunks
     * @param chunk chunk
     * @param structureManager structure manager
     */
    public void applyBiomeDecoration(WorldGenLevel genRegion, ChunkAccess chunk, StructureManager structureManager) {
        super.applyBiomeDecoration(genRegion, chunk, structureManager);
    }

    /**
     * Get terrain column at given X and Z coordinates in a form of a column.
     *
     * @param x X block coordinate
     * @param z Z block coordinate
     * @param heightView chunk, world or anything that implements HeightLimitView
     * @param noiseConfig noise config
     *
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
     *
     * @param x X block coordinate
     * @param z Z block coordinate
     * @param heightmap heightmap type
     * @param heightView chunk, world or anything that implements HeightLimitView
     * @param noiseConfig noise config
     *
     * @return height at given coordinates
     */
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor heightView,
                             @Nullable RandomState noiseConfig) {
        // Get height from proto-chunk heightmap
        if (heightView instanceof ProtoChunk chunk){
            // Chunk position
            int chunkX = chunk.getPos().x;
            int chunkZ = chunk.getPos().z;

            // Get cached generation data
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
