package net.arthurllew.mementobeta.world.biome;

/**
 * Beta 1.7.3 climate map.
 */
public enum ClimateMap {
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
    private static final ClimateMap[] biomeLookupTable = new ClimateMap[4096];

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
    ClimateMap(String name, int color) {
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
    private static ClimateMap getBiome(float temperature, float humidity) {
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
     * @param temperature temperature.
     * @param humidity humidity.
     * @return climate table value from given temperature and humidity.
     */
    public static ClimateMap getBiomeFromLookup(double temperature, double humidity) {
        int t = (int)(temperature * 63.0D);
        int h = (int)(humidity * 63.0D);
        return biomeLookupTable[t + h * 64];
    }
}
