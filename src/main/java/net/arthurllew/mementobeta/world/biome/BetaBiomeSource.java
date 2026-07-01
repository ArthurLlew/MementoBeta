package net.arthurllew.mementobeta.world.biome;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arthurllew.mementobeta.world.levelgen.BetaChunkGenerator;
import net.arthurllew.mementobeta.world.levelgen.noise.BetaTerrainDensitySampler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("DefaultNotLastCaseInSwitch")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BetaBiomeSource extends BiomeSource {
    /**
     * Codec (reads list of biome {@link HolderSet} from "dimension/betaworld.json"; one {@link HolderSet}
     * for each Beta 1.7.3 biome variations set).
     */
    public static final MapCodec<BetaBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
        values -> values.group(
            ExtraCodecs.nonEmptyList(Biome.LIST_CODEC.listOf()).fieldOf("biomes")
                    .forGetter((betaBiomeSource) -> betaBiomeSource.biomes)
        ).apply(values, BetaBiomeSource::new));

    /**
     * Biomes list.
     */
    private final List<HolderSet<Biome>> biomes;

    /**
     * All biomes.
     */
    private final Stream<Holder<Biome>> collectedBiomes;

    /**
     * Allows access to the chunk generator cache.
     */
    private BetaChunkGenerator generator;

    /**
     * Constructor.
     */
    public BetaBiomeSource(List<HolderSet<Biome>> biomes) {
        this.biomes = biomes;

        // Gather all biomes into a stream
        List<Holder<Biome>> collectedBiomes = new ArrayList<>();
        for (HolderSet<Biome> biomeSet : this.biomes) {
            collectedBiomes.addAll(biomeSet.stream().toList());
        }
        this.collectedBiomes = collectedBiomes.stream();
    }

    /**
     * @return biome source codec.
     */
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    /**
     * @return all biome source biomes.
     */
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.collectedBiomes;
    }

    /**
     * Configures related chunk generator.
     * @param generator chunk generator
     */
    public void setGenerator(BetaChunkGenerator generator) {
        this.generator = generator;
    }

    /**
     * @param quarterX chunk quarter X
     * @param quarterY chunk quarter Y
     * @param quarterZ chunk quarter Z
     * @param sampler climate sampler
     * @return biome at given quarter coordinates
     */
    @Override
    public Holder<Biome> getNoiseBiome(int quarterX, int quarterY, int quarterZ, Climate.Sampler sampler) {
        // Global coordinates
        int x = QuartPos.toBlock(quarterX);
        int y = QuartPos.toBlock(quarterY);
        int z = QuartPos.toBlock(quarterZ);

        // Chunk relative coordinates
        int localX = SectionPos.sectionRelative(x);
        int localZ = SectionPos.sectionRelative(z);

        // Get local climate
        BetaClimate climate = this.generator.climateCache
                .get(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)).get(localX, localZ);

        // Get biome from climate
        return getBiomeFromClimate(x, y, z,
                isBiomeCold(climate, this.generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG)));
    }

    private int isBiomeCold(BetaClimate climate, int height) {
        int seaLevel = this.generator.getSeaLevel();

        //=====================================================================================================
        // In Beta 1.7.3 ChunkProviderGenerate.populate(...) method (among other things) was responsible for
        // generating snowy regions using an adjusted temperature. Because that also affected weather (snow
        // instead of rain), a set of auxiliary cold biomes is used. This also helps to place snow and ice.
        //=====================================================================================================

        // Calculate biome variant index depending on adjusted temperature
        double adjustedTemperature = climate.temperature() -
                ((double)((height <= seaLevel ? seaLevel + 1 : height) - 64) / 64.0D * 0.3D);
        return adjustedTemperature < 0.5D ? 1 : 0;
    }

    /**
     * Optimized super method.
     */
    @Override
    @Nullable
    public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(int x, int y, int z, int radius,
                                                             int increment, Predicate<Holder<Biome>> biomePredicate,
                                                             RandomSource random, boolean findClosest,
                                                             Climate.Sampler sampler) {
        int i = QuartPos.fromBlock(x);
        int j = QuartPos.fromBlock(z);
        int k = QuartPos.fromBlock(radius);
        Pair<BlockPos, Holder<Biome>> pair = null;
        int i1 = 0;
        int j1 = findClosest ? 0 : k;

        for(int k1 = j1; k1 <= k; k1 += increment) {
            for(int l1 = SharedConstants.debugGenerateSquareTerrainWithoutNoise ? 0 : -k1; l1 <= k1; l1 += increment) {
                boolean flag = Math.abs(l1) == k1;

                for(int i2 = -k1; i2 <= k1; i2 += increment) {
                    if (findClosest) {
                        boolean flag1 = Math.abs(i2) == k1;
                        if (!flag1 && !flag) {
                            continue;
                        }
                    }

                    int k2 = i + i2;
                    int j2 = j + l1;
                    Holder<Biome> biome = this.getBiomeFromClimate(x + k2, y, z + j2, 0);
                    if (biomePredicate.test(biome)) {
                        if (pair == null || random.nextInt(i1 + 1) == 0) {
                            BlockPos blockpos = new BlockPos(QuartPos.toBlock(k2), y, QuartPos.toBlock(j2));
                            if (findClosest) {
                                return Pair.of(blockpos, biome);
                            }

                            pair = Pair.of(blockpos, biome);
                        }

                        ++i1;
                    }
                }
            }
        }

        return pair;
    }

    /**
     * Optimized super method.
     */
    @Override
    public Set<Holder<Biome>> getBiomesWithin(int x, int y, int z, int radius, Climate.Sampler sampler) {
        int minLocalX = QuartPos.fromBlock(x - radius);
        int maxLocalX = QuartPos.fromBlock(x + radius);
        int minLocalZ = QuartPos.fromBlock(z - radius);
        int maxLocalZ = QuartPos.fromBlock(z + radius);
        int totalX = maxLocalX - minLocalX + 1;
        int totalZ = maxLocalZ - minLocalZ + 1;
        Set<Holder<Biome>> set = Sets.newHashSet();

        for(int iX = 0; iX < totalX; ++iX) {
            for(int iZ = 0; iZ < totalZ; ++iZ) {
                int localX = minLocalX + iX;
                int localZ = minLocalZ + iZ;
                set.add(this.getBiomeFromClimate(x + localX, y, z + localZ, 0));
            }
        }

        return set;
    }

    /**
     * Optimized super method.
     */
    @Override
    @Nullable
    public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(BlockPos pos, int radius, int horizontalStep,
                                                            int verticalStep,
                                                            Predicate<Holder<Biome>> biomePredicate,
                                                            Climate.Sampler sampler, LevelReader level) {
        Set<Holder<Biome>> set = this.possibleBiomes()
                .stream().filter(biomePredicate).collect(Collectors.toUnmodifiableSet());

        if (!set.isEmpty()) {
            int searchRadius = Math.floorDiv(radius, horizontalStep);

            for (BlockPos.MutableBlockPos mutablePos : BlockPos.spiralAround(BlockPos.ZERO, searchRadius,
                    Direction.EAST, Direction.SOUTH)) {
                int x = pos.getX() + mutablePos.getX() * horizontalStep;
                int z = pos.getZ() + mutablePos.getZ() * horizontalStep;

                Holder<Biome> biome = this.getBiomeFromClimate(pos.getX(), pos.getY(), pos.getZ(), 0);
                if (set.contains(biome)) {
                    return Pair.of(new BlockPos(x, 0, z), biome);
                }
            }
        }

        return null;
    }

    /**
     * Maps climate to biome.
     * @param x X block coordinate
     * @param y Y block coordinate
     * @param z Z block coordinate
     * @param biomeVariantID biome variant index
     * @return biome
     */
    private Holder<Biome> getBiomeFromClimate(int x, int y, int z, int biomeVariantID) {
        // Global to chunk coordinates
        int chunkX = SectionPos.blockToSectionCoord(x);
        int chunkZ = SectionPos.blockToSectionCoord(z);

        // If Y is out of Beta 1.7.3 world bounds sample biomes from Beta 1.7.3 world top/bottom coordinates
        if (y < this.generator.getBetaMinY())
            return this.getBiomeFromClimate(x, this.generator.getBetaMinY(), z, biomeVariantID);
        if (y > this.generator.getBetaMaxY())
            return this.getBiomeFromClimate(x, this.generator.getBetaMaxY(), z, biomeVariantID);

        // Global to chunk local coordinates
        int localX = SectionPos.sectionRelative(x);
        int localZ = SectionPos.sectionRelative(z);

        // Get terrain noise
        double[] terrainNoise = this.generator.terrainNoiseCache.get(chunkX, chunkZ).getTerrainNoise();

        // Get beta biome
        BetaClimateMap betaBiome = BetaClimateMap
                .getBiomeFromTable(this.generator.climateCache.get(chunkX, chunkZ).get(localX, localZ));

        // Sampled density is not > 0
        if (BetaTerrainDensitySampler
                .sampleDensity(SectionPos.sectionRelative(x), y, SectionPos.sectionRelative(z),
                        terrainNoise, 17, 5) <= 0) {
            boolean isLake = true;

            // Above water
            if (y >= this.generator.getSeaLevel()) {
                // Density column
                double[] density = this.generator.densityCache.get(chunkX, chunkZ).get(localX, localZ);

                // Has lake below it
                if (density[this.generator.getSeaLevel()-1] <= 0) {
                    // Check for air gap between this point and lake (if y > max y then use max y)
                    for (int i = this.generator.getSeaLevel(); i < (y < 128 ? y : this.generator.getBetaMaxY()); i++) {
                        if (density[i] > 0) {
                            isLake = false;
                            break;
                        }
                    }
                }
                else {
                    isLake = false;
                }
            }

            // Check deep water body condition (just slightly below sea level)
            if (isLake) {
                // Select lake biome depending on beta biome (normal biomes correspond to normal lake, warm to warm and
                // cold to cold)
                return switch (betaBiome) {
                    default -> this.biomes.get(10).get(0);
                    case RAINFOREST, SAVANNA, DESERT -> this.biomes.get(10).get(1);
                    case TAIGA, TUNDRA -> this.biomes.get(10).get(2);
                };
            }
        }

        // Select modern version of old biome
        return switch (betaBiome) {
            case RAINFOREST -> biomes.get(0).get(biomeVariantID);
            case SWAMPLAND -> biomes.get(1).get(biomeVariantID);
            case SEASONAL_FOREST -> biomes.get(2).get(biomeVariantID);
            case FOREST -> biomes.get(3).get(biomeVariantID);
            case SAVANNA -> biomes.get(4).get(biomeVariantID);
            case SHRUBLAND -> biomes.get(5).get(biomeVariantID);
            case TAIGA -> biomes.get(6).get(0);
            case DESERT -> biomes.get(7).get(0);
            default -> biomes.get(8).get(biomeVariantID);
            case TUNDRA -> biomes.get(9).get(0);
        };
    }
}