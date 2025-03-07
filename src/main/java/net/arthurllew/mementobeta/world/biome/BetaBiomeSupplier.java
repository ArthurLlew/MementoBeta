package net.arthurllew.mementobeta.world.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arthurllew.mementobeta.world.BetaChunkGenerator;
import net.arthurllew.mementobeta.world.util.ChunkGenCache;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import javax.annotation.ParametersAreNonnullByDefault;
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

        // Select biome
        ClimateMap biome = ClimateMap.getBiomeFromLookup(
                genData.temperature[localX*16 + localZ],
                genData.humidity[localX*16 + localZ]);
        if (biome == ClimateMap.rainforest) {
            return allowedBiomes.get(0);
        } else if (biome == ClimateMap.swampland) {
            return allowedBiomes.get(1);
        } else if (biome == ClimateMap.seasonalForest) {
            return allowedBiomes.get(2);
        } else if (biome == ClimateMap.forest) {
            return allowedBiomes.get(3);
        } else if (biome == ClimateMap.savanna) {
            return allowedBiomes.get(4);
        } else if (biome == ClimateMap.shrubland) {
            return allowedBiomes.get(5);
        } else if (biome == ClimateMap.taiga) {
            return allowedBiomes.get(6);
        } else if (biome == ClimateMap.desert) {
            return allowedBiomes.get(7);
        } else if (biome == ClimateMap.plains) {
            return allowedBiomes.get(8);
        } else if (biome == ClimateMap.tundra) {
            return allowedBiomes.get(9);
        }

        // Is never reached
        return allowedBiomes.get(8);
    }
}
