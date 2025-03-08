package net.arthurllew.mementobeta.world.biome;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Beta 1.7.3 climate map.
 */
public enum BetaClimateMap {
    RAINFOREST("Rainforest", 588342),
    SWAMPLAND("Swampland", 522674),
    SEASONAL_FOREST("Seasonal Forest", 10215459),
    FOREST("Forest", 353825),
    SAVANNA("Savanna", 14278691),
    SHRUBLAND("Shrubland", 10595616),
    TAIGA("Taiga", 3060051),
    DESERT("Desert", 16421912),
    PLAINS("Plains", 16767248),
    TUNDRA("Tundra", 5762041);

    /**
     * Climate table.
     */
    private static final BetaClimateMap[] biomeLookupTable = new BetaClimateMap[4096];

    /**
     * Biome name.
     */
    public final String biomeName;
    /**
     * Biome color.
     */
    public final int color;

    /**
     * Constructor.
     * @param name biome name.
     * @param color biome color
     */
    BetaClimateMap(String name, int color) {
        this.biomeName = name;
        this.color = color;
    }

    // Init climate table
    static {
        generateBiomeLookup();
    }

    /**
     * Generates climate table.
     */
    private static void generateBiomeLookup() {
        for(int t = 0; t < 64; ++t) {
            for(int h = 0; h < 64; ++h) {
                biomeLookupTable[t + h * 64] = getBiome((float)t / 63.0F, (float)h / 63.0F);
            }
        }
    }

    /**
     * @param temperature temperature.
     * @param humidity humidity.
     * @return climate value from given temperature and humidity.
     */
    private static BetaClimateMap getBiome(float temperature, float humidity) {
        humidity *= temperature;

        // In Vanilla Beta 1.7.3 here the ice desert should be picked, but Notch left a small bug :)
        if (temperature < 0.1F) {
            return TUNDRA;
        }

        if (humidity < 0.2F) {
            if (temperature < 0.5F) {
                return TUNDRA;
            }
            if (temperature < 0.95F) {
                return SAVANNA;
            } else {
                return DESERT;
            }
        }

        if (humidity > 0.5F && temperature < 0.7F) {
            return SWAMPLAND;
        }

        if (temperature < 0.5F) {
            return TAIGA;
        }

        if (temperature < 0.97F) {
            if (humidity < 0.35F) {
                return SHRUBLAND;
            } else {
                return FOREST;
            }
        }

        if (humidity < 0.45F) {
            return PLAINS;
        }

        if (humidity < 0.9F) {
            return SEASONAL_FOREST;
        } else {
            return RAINFOREST;
        }
    }

    /**
     * @param climate climate.
     * @return climate table value from given temperature and humidity.
     */
    public static BetaClimateMap getBiomeFromLookup(BetaClimate climate) {
        int t = (int)(climate.temperature() * 63.0D);
        int h = (int)(climate.humidity() * 63.0D);
        return biomeLookupTable[t + h * 64];
    }

    /**
     * @param climate climate.
     * @return top layer blocks.
     */
    public static BiomeTopLayerBlocks getFromClimate(BetaClimate climate) {
        switch (BetaClimateMap.getBiomeFromLookup(climate)) {
            case DESERT:
                return new BiomeTopLayerBlocks(Blocks.SAND, Blocks.SAND);
            default:
                return new BiomeTopLayerBlocks(Blocks.GRASS_BLOCK, Blocks.DIRT);
        }
    }

    /**
     * Record for storing top layer blocks.
     * @param topBlock top-most block.
     * @param fillerBlock blocks under top-most block.
     */
    public record BiomeTopLayerBlocks(Block topBlock, Block fillerBlock) {}
}
