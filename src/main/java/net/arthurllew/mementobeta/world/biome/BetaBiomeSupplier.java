package net.arthurllew.mementobeta.world.biome;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arthurllew.mementobeta.world.levelgen.BetaChunkGenerator;
import net.arthurllew.mementobeta.world.util.ChunkGenCache;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BetaBiomeSupplier extends BiomeSource {
    /**
     * Codec (reads list of biome {@link HolderSet} from "dimension/betaworld.json"; one {@link HolderSet}
     * for each Beta 1.7.3 biome variations set).
     */
    public static final Codec<BetaBiomeSupplier> CODEC = RecordCodecBuilder.create((values) ->
            values.group(ExtraCodecs.nonEmptyList(Biome.LIST_CODEC.listOf()).fieldOf("biomes")
                    .forGetter((supplier) -> supplier.biomes)
            ).apply(values, BetaBiomeSupplier::new)
    );

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
    public BetaBiomeSupplier(List<HolderSet<Biome>> biomes) {
        this.biomes = biomes;

        // Gather all biomes into a stream
        List<Holder<Biome>> collectedBiomes = new ArrayList<>();
        for (HolderSet<Biome> biome : this.biomes) {
            collectedBiomes.addAll(biome.stream().toList());
        }
        this.collectedBiomes = collectedBiomes.stream();
    }

    // Getters
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.collectedBiomes;
    }

    /**
     * Configures related chunk generator.
     * @param generator chunk generator.
     */
    public void setGenerator(BetaChunkGenerator generator) {
        this.generator = generator;
    }

    /**
     * @param x chunk quarter X.
     * @param y chunk quarter Y.
     * @param z chunk quarter Z.
     * @param sampler climate sampler.
     * @return biome at given coordinates.
     */
    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        // Global coordinates
        x = QuartPos.toBlock(x);
        z = QuartPos.toBlock(z);

        // Get generation cached data
        ChunkGenCache.GenData genData =
                this.generator.chunkGenCache.get(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));

        // Chunk relative coordinates
        int localX = SectionPos.sectionRelative(x);
        int localZ = SectionPos.sectionRelative(z);

        // Get local climate
        BetaClimate climate = genData.climate()[localX * 16 + localZ];

        // Get surface Y and sea level
        int height = genData.heightmap().getHeight(localX, localZ);
        int seaLevel = this.generator.getSeaLevel();

        //=====================================================================================================
        // In Beta 1.7.3 ChunkProviderGenerate.populate(...) method (among other things) was responsible for
        // generating snowy regions using an adjusted temperature. Because that also affected weather (snow
        // instead of rain), a set of auxiliary cold biomes is used. This also helps to place snow and ice.
        //=====================================================================================================

        // Calculate biome variant index depending on adjusted temperature
        double adjustedTemperature = climate.temperature() -
                ((double)((height <= seaLevel ? seaLevel + 1 : height) - 64) / 64.0D * 0.3D);
        int biomeVariantID = adjustedTemperature < 0.5D ? 1 : 0;

        // Check deep water body condition (just slightly below sea level)
        if (height <= 60) {
            // Select lake biome depending on temperature (cold remains cold, normal can become warm if
            // the temperature is high enough)
            return this.biomes.get(10).get(biomeVariantID == 1 ? 1 : climate.temperature() > 0.97D ? 2 : 0);
        }

        // Get biome from climate
        return getBiomeFromClimate(climate, biomeVariantID);
    }

    /**
     * Super method cases a lot of lag on server startup, because the entire chunk cache is generated.
     * Here a more simplistic calculation of biome is used.
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
                    BetaClimate climate = this.generator.betaClimateSampler.sample(x + k2, z + j2);
                    Holder<Biome> biome = this.getBiomeFromClimate(climate, 0);
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
     * Optimized super method. Is used to generate ocean monument.
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
                BetaClimate climate = this.generator.betaClimateSampler.sample(x + localX, z + localZ);
                set.add(this.getBiomeFromClimate(climate, 0));
            }
        }

        return set;
    }

    /**
     * Optimized super method. Is used to find structure via command.
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

                BetaClimate climate = this.generator.betaClimateSampler.sample(x, z);
                Holder<Biome> biome = this.getBiomeFromClimate(climate, 0);
                if (set.contains(biome)) {
                    return Pair.of(new BlockPos(x, 0, z), biome);
                }
            }
        }

        return null;
    }

    /**
     * Maps climate to biome.
     * @param climate climate.
     * @param biomeVariantID biome variant index.
     * @return biome.
     */
    private Holder<Biome> getBiomeFromClimate(BetaClimate climate, int biomeVariantID) {
        // Get beta biome
        BetaClimateMap betaBiome = BetaClimateMap.getBiomeFromLookup(climate);
        // Convert it to modern biome
        switch (betaBiome) {
            case RAINFOREST:
                return biomes.get(0).get(biomeVariantID);
            case SWAMPLAND:
                return biomes.get(1).get(biomeVariantID);
            case SEASONAL_FOREST:
                return biomes.get(2).get(biomeVariantID);
            case FOREST:
                return biomes.get(3).get(biomeVariantID);
            case SAVANNA:
                return biomes.get(4).get(biomeVariantID);
            case SHRUBLAND:
                return biomes.get(5).get(biomeVariantID);
            case TAIGA:
                return biomes.get(6).get(0);
            case DESERT:
                return biomes.get(7).get(0);
            default:
            case PLAINS:
                return biomes.get(8).get(biomeVariantID);
            case TUNDRA:
                return biomes.get(9).get(0);
        }
    }
}