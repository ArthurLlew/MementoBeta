package net.arthurllew.mementobeta.world.biome;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arthurllew.mementobeta.world.BetaChunkGenerator;
import net.arthurllew.mementobeta.world.levelgen.BetaClimateSampler;
import net.arthurllew.mementobeta.world.util.ChunkGenCache;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BetaBiomeSupplier extends BiomeSource {
    /**
     * Codec.
     */
    public static final Codec<BetaBiomeSupplier> CODEC = RecordCodecBuilder.create((values) -> values.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(
                    (instance) -> instance.allowedBiomes),
            Codec.intRange(0, 62).fieldOf("scale").orElse(2).forGetter(
                    (instance) -> instance.size)).apply(values, BetaBiomeSupplier::new));

    private final HolderSet<Biome> allowedBiomes;
    private final int size;

    private BetaChunkGenerator generator;

    public BetaBiomeSupplier(HolderSet<Biome> allowedBiomes, int size) {
        this.allowedBiomes = allowedBiomes;
        this.size = size;
    }

    // Getters
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.allowedBiomes.stream();
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

        // Check deep water body condition (just slightly below sea level)
        if (genData.heightmap.getHeight(localX, localZ) <= 60) {
            return allowedBiomes.get(allowedBiomes.size() - 1);
        }

        // Get biome from climate
        return getBiomeFromClimate(genData.temperature[localX*16 + localZ], genData.humidity[localX*16 + localZ]);
    }

    /**
     * Super method cases a lot of lag on server startup, because the entire chunk cache is generated.
     * Here a more simplistic calculation of biome is used.
     * @return {@code null}.
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
                    BetaClimateSampler.Climate climate = this.generator.betaClimateSampler.sample(x + k2, z + j2);
                    Holder<Biome> holder = this.getBiomeFromClimate(climate.temperature(), climate.humidity());
                    if (biomePredicate.test(holder)) {
                        if (pair == null || random.nextInt(i1 + 1) == 0) {
                            BlockPos blockpos = new BlockPos(QuartPos.toBlock(k2), y, QuartPos.toBlock(j2));
                            if (findClosest) {
                                return Pair.of(blockpos, holder);
                            }

                            pair = Pair.of(blockpos, holder);
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
                BetaClimateSampler.Climate climate =
                        this.generator.betaClimateSampler.sample(x + localX, z + localZ);
                set.add(this.getBiomeFromClimate(climate.temperature(), climate.humidity()));
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
                int y = pos.getZ() + mutablePos.getZ() * horizontalStep;

                BetaClimateSampler.Climate climate = this.generator.betaClimateSampler.sample(x, y);
                Holder<Biome> holder = this.getBiomeFromClimate(climate.temperature(), climate.humidity());
                if (set.contains(holder)) {
                    return Pair.of(new BlockPos(x, 0, y), holder);
                }
            }
        }

        return null;
    }

    /**
     * Converts climate to biome.
     * @param temperature temperature.
     * @param humidity humidity.
     * @return biome.
     */
    private Holder<Biome> getBiomeFromClimate(double temperature, double humidity) {
        // Get climate map value
        ClimateMap climate = ClimateMap.getBiomeFromLookup(temperature, humidity);

        // Convert it to biome
        switch (climate) {
            case RAINFOREST:
                return allowedBiomes.get(0);
            case SWAMPLAND:
                return allowedBiomes.get(1);
            case SEASONAL_FOREST:
                return allowedBiomes.get(2);
            case FOREST:
                return allowedBiomes.get(3);
            case SAVANNA:
                return allowedBiomes.get(4);
            case SHRUBLAND:
                return allowedBiomes.get(5);
            case TAIGA:
                return allowedBiomes.get(6);
            case DESERT:
                return allowedBiomes.get(7);
            default:
            case PLAINS:
                return allowedBiomes.get(8);
            case TUNDRA:
                return allowedBiomes.get(9);
        }
    }
}