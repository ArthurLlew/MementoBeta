package net.arthurllew.mementobeta.world.levelgen;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import net.arthurllew.mementobeta.world.biome.BetaBiomeSource;
import net.arthurllew.mementobeta.world.biome.BetaBiomes;
import net.arthurllew.mementobeta.world.levelgen.noise.BetaClimateSampler;
import net.arthurllew.mementobeta.world.levelgen.cache.ChunkCache;
import net.arthurllew.mementobeta.world.levelgen.cache.ChunkCachedClimateMap;
import net.arthurllew.mementobeta.world.levelgen.cache.ChunkCachedDensityMap;
import net.arthurllew.mementobeta.world.levelgen.cache.ChunkCachedNoise;
import net.arthurllew.mementobeta.world.levelgen.carver.BetaCavesCarver;
import net.arthurllew.mementobeta.world.levelgen.noise.BetaTerrainNoiseSampler;
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

    /**
     * Custom generator settings.
     */
    public Holder<BetaChunkGeneratorSettings> betaSettings;

    /**
     * Chunk climate cache.
     */
    public final ChunkCache<ChunkCachedClimateMap> climateCache;
    /**
     * Chunk density cache.
     */
    public final ChunkCache<ChunkCachedDensityMap> densityCache;
    /**
     * Chunk terrain noise cache.
     */
    public final ChunkCache<ChunkCachedNoise> terrainNoiseCache;

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
    private NormalNoise subsurfaceSampler;

    /**
     * Beta 1.7.3 cave carver.
     */
    private final BetaCavesCarver betaCaveCarver;

    /**
     * Constructor.
     * @param biomeSource biome provider.
     * @param settings generator settings.
     * @param betaSettings generator custom settings.
     */
    BetaChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings,
                       Holder<BetaChunkGeneratorSettings> betaSettings) {
        super(biomeSource, settings);

        // Inject reference to this generator into biome source (used to access generator cache)
        ((BetaBiomeSource)this.biomeSource).setGenerator(this);

        // Init climate and density cache
        this.climateCache = new ChunkCache<>(ChunkCachedClimateMap::new, this);
        this.densityCache = new ChunkCache<>(ChunkCachedDensityMap::new, this);
        this.terrainNoiseCache = new ChunkCache<>(ChunkCachedNoise::new, this);

        // Beta carver
        this.betaCaveCarver = new BetaCavesCarver(this);

        // Custom settings
        this.betaSettings = betaSettings;
    }

    /**
     * Set generator seed, init random class and noise generators.
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
     * @return Beta 1.7.3 minimum Y block coordinate.
     */
    public int getBetaMinY() {
        return 0;
    }
    /**
     * @return Beta 1.7.3 maximum Y block coordinate.
     */
    public int getBetaMaxY() {
        return 127;
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
     * Creates structures. The 1rst step of terrain generation.
     * @param chunk chunk to process.
     */
    @Override
    public void createStructures(RegistryAccess registryAccess, ChunkGeneratorStructureState structureState,
                                 StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager
                                 structureTemplateManager) {
        // Generate structures as normal
        super.createStructures(registryAccess, structureState, structureManager, chunk, structureTemplateManager);
    }

    /**
     * Creates basic terrain from noise. The 4th step of terrain generation.
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
     * @param chunk chunk to process.
     * @return provided chunk.
     */
    public ChunkAccess generateTerrain(ChunkAccess chunk) {
        // Get cached density
        ChunkCachedDensityMap density = this.densityCache.get(chunk.getPos().x, chunk.getPos().z);

        // Chunk heightmaps
        Heightmap heightmapOceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmapSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        // ================================================================================================
        // In Vanilla Beta 1.7.3 this section is done by ChunkProviderGenerate.generateTerrain(...) method.
        // ================================================================================================

        // Iterate over chunk local coordinates
        BlockState blockState;
        for(int localX = 0; localX < 16; localX++) {
            for(int localZ = 0; localZ < 16; localZ++) {
                // Iterate over height
                for(int localY = 0; localY < 128; localY++) {
                    // Stone for density > 0
                    if(density.get(localX, localZ)[localY] > 0) {
                        blockState = this.betaSettings.value().stoneBlock().defaultBlockState();
                    }
                    // Air or water otherwise
                    else
                    {
                        // Water below sea level
                        if(localY < this.getSeaLevel()) {
                            blockState = Blocks.WATER.defaultBlockState();
                        }
                        // Air otherwise
                        else {
                            blockState = Blocks.AIR.defaultBlockState();
                        }
                    }

                    // Set block
                    int sectionX = SectionPos.sectionRelative(localX);
                    int sectionY = SectionPos.sectionRelative(localY);
                    int sectionZ = SectionPos.sectionRelative(localZ);
                    chunk.getSection(chunk.getSectionIndex(localY))
                            .setBlockState(sectionX, sectionY, sectionZ, blockState, false);
                    // Update heightmaps
                    heightmapOceanFloor.update(localX, localY, localZ, blockState);
                    heightmapSurface.update(localX, localY, localZ, blockState);
                }
            }
        }

        return chunk;
    }

    /**
     * Shapes surface, built on previous step. The 5th step of terrain generation.
     * @param region chunk region
     * @param structures structures to place
     * @param noiseConfig noise config
     * @param chunk chunk to process
     */
    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState noiseConfig,
                             ChunkAccess chunk) {
        // Chunk position
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // ======================================================================================================
        // In Vanilla Beta 1.7.3 this section is done by ChunkProviderGenerate.replaceBlocksForBiome(...) method.
        // ======================================================================================================

        // We also need random class with seed derived from chunk coordinates
        Random rand = new Random((long)chunkX * 341873128712L + (long)chunkZ * 132897987541L);

        double scale = 0.03125D; // Original code: double scale = 1.0D / 32.0D;

        // Noises for sand/gravel beaches and places, where there are no top blocks and stone can be seen
        double[] sandNoise = this.betaTerrainNoiseSampler.sampleBeachNoise(
                (chunkX * 16), (chunkZ * 16), 0.0D,
                16, 16, 1,
                scale, scale, 1.0D);
        double[] gravelNoise = this.betaTerrainNoiseSampler.sampleBeachNoise(
                (chunkX * 16), 109.0134D, (chunkZ * 16),
                16, 1, 16,
                scale, 1.0D, scale);
        double[] stoneNoise = this.betaTerrainNoiseSampler.sampleSurfaceNoise(
                (chunkX * 16), (chunkZ * 16), 0.0D,
                16, 16, 1,
                scale * 2.0D, scale * 2.0D, scale * 2.0D);

        // Prepare block position
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // For some reason in Beta 1.7.3 code coordinates in this nested loop are iterated in reverse...
        // or noise is generated in reverse? Whatever the case, my testing shown, that it doesn't affect
        // block horizontal placement and only matters for the order of random class being called, which
        // is responsible for vertical block placement. For example, this effects sandstone vertical
        // distribution. The latter is very important for correct generation replication, so in order
        // to retain same look and don't mess up everything else, we will retain the loop order.
        for(int localZ = 0; localZ < 16; localZ++) {
            for(int localX = 0; localX < 16; localX++) {
                // Get biome top block
                Block biomeBlock = BetaBiomes
                        .getBiomeFromTable(this.climateCache.get(chunkX, chunkZ).get(localX, localZ)).topBlock;

                // Determine beach and stone patch noises
                boolean isGravel = gravelNoise[localX * 16 + localZ] + rand.nextDouble() * 0.2D > 3.0D;
                boolean isSand = sandNoise[localX * 16 + localZ] + rand.nextDouble() * 0.2D > 0.0D;
                int depth = (int)(stoneNoise[localX * 16 + localZ] / 3.0D + 3.0D + rand.nextDouble() * 0.25D);

                // Surface top block
                Block blockTop = biomeBlock;
                // Block bellow it
                Block blockBelow = blockTop;

                // Flag of previous block being air
                int airAbove = -1;

                // Loop over chunk-local Oy
                for(int localY = 127; localY >= this.getMinY(); localY--) {
                    // Set block position
                    pos.set(localX, localY, localZ);

                    // Bedrock
                    if(localY <= this.getMinY() + rand.nextInt(5)) {
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
                                else if(localY >= this.generatorSettings().value().seaLevel() - 4
                                        && localY <= this.generatorSettings().value().seaLevel() + 1) {
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
                                if(localY < this.generatorSettings().value().seaLevel() && blockTop == Blocks.AIR) {
                                    blockTop = Blocks.WATER;
                                }

                                airAbove = depth;

                                // Place blocks depending on sea level
                                if(localY >= this.generatorSettings().value().seaLevel() - 1) {
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
     * @param genRegion world region of 3x3 chunks
     * @param chunk chunk
     * @param structureManager structure manager
     */
    public void applyBiomeDecoration(WorldGenLevel genRegion, ChunkAccess chunk, StructureManager structureManager) {
        super.applyBiomeDecoration(genRegion, chunk, structureManager);
    }

    /**
     * Get terrain blocks column at given X and Z coordinates in a form of a column.
     * @param x X block coordinate
     * @param z Z block coordinate
     * @param heightView chunk, world or anything that implements HeightLimitView
     * @param noiseConfig noise config
     * @return blocks column
     */
    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightView, RandomState noiseConfig) {
        // Density column
        double[] density = this.densityCache
                .get(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z))
                .get(SectionPos.sectionRelative(x), SectionPos.sectionRelative(z));

        // Init block column
        BlockState[] column = new BlockState[this.getGenDepth()];

        // Iterate over column
        for (int i = this.getGenDepth() - 1; i >= 0; --i) {
            // Global Y coordinate
            int y = i + this.getMinY();

            // Y > Beta height
            if (y > this.getBetaMaxY()) {
                column[i] = Blocks.AIR.defaultBlockState();
            }
            // Y < Beta min height
            else if (y < this.getBetaMinY()) {
                column[i] = this.betaSettings.value().stoneBlock().defaultBlockState();
            }
            // Inside density column
            else {
                // Stone for density > 0
                if(density[y] > 0) {
                    column[i] = this.betaSettings.value().stoneBlock().defaultBlockState();
                }
                // Air or water otherwise
                else
                {
                    // Water below sea level
                    if(y < this.getSeaLevel()) {
                        column[i] = this.generatorSettings().value().defaultFluid();
                    }
                    // Air otherwise
                    else {
                        column[i] = Blocks.AIR.defaultBlockState();
                    }
                }
            }
        }

        return new NoiseColumn(this.getMinY(), column);
    }

    /**
     * Calculates first empty block Y coordinate at given position from given heightmap.
     * @param x X block coordinate
     * @param z Z block coordinate
     * @param heightmap heightmap type
     * @param heightView chunk, world or anything that implements HeightLimitView
     * @param noiseConfig noise config
     * @return first empty block Y coordinate
     */
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor heightView,
                             @Nullable RandomState noiseConfig) {
        // Chunk in process of generation
        if (heightView instanceof ProtoChunk){
            return this.getBaseHeight(x, z, heightmap);
        }
        // Otherwise return something stupid to notice in game
        else {
            return 128;
        }
    }

    /**
     * Calculates first empty block Y coordinate at given position from given heightmap.
     * @param x X block coordinate
     * @param z Z block coordinate
     * @param heightmap heightmap type
     * @return first empty block Y coordinate
     */
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap) {
        // Density column
        double[] density = this.densityCache
                .get(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z))
                .get(SectionPos.sectionRelative(x), SectionPos.sectionRelative(z));

        // Iterate over column downwards (column index is also a global Y coordinate here)
        for (int y = this.getBetaMaxY(); y >= this.getBetaMinY(); --y) {
            // At or below sea level, has liquid and liquid is not opaque for provided heightmap
            // or
            // Density > 0
            if (y < this.getSeaLevel() && density[y] <= 0
                    && heightmap.isOpaque().test(this.generatorSettings().value().defaultFluid())
                    || density[y] > 0) {
                return y + 1;
            }
        }

        // Lowest Beta 1.7.3 point
        return this.getBetaMinY();
    }
}
