package net.arthurllew.mementobeta.world.biome;

public class ClimateMap {
    public static final ClimateMap rainforest = new ClimateMap("Rainforest",588342);
    public static final ClimateMap swampland = new ClimateMap("Swampland",522674);
    public static final ClimateMap seasonalForest = new ClimateMap("Seasonal Forest",10215459);
    public static final ClimateMap forest = new ClimateMap("Forest",353825);
    public static final ClimateMap savanna = new ClimateMap("Savanna",14278691);
    public static final ClimateMap shrubland = new ClimateMap("Shrubland",10595616);
    public static final ClimateMap taiga = new ClimateMap("Taiga",3060051);
    public static final ClimateMap desert = new ClimateMap("Desert",16421912);
    public static final ClimateMap plains = new ClimateMap("Plains",16767248);
    public static final ClimateMap tundra = new ClimateMap("Tundra",5762041);

    private static final ClimateMap[] biomeLookupTable = new ClimateMap[4096];

    public String biomeName;
    public int color;

    private ClimateMap(String name, int color) {
        this.biomeName = name;
        this.color = color;
    }

    private static ClimateMap getBiome(float temperature, float humidity) {
        humidity *= temperature;

        // In Vanilla Beta 1.7.3 here the ice desert should be picked, but Notch left a small bug :)
        if (temperature < 0.1F) {
            return tundra;
        }

        if (humidity < 0.2F) {
            if (temperature < 0.5F) {
                return tundra;
            }
            if (temperature < 0.95F) {
                return savanna;
            } else {
                return desert;
            }
        }

        if (humidity > 0.5F && temperature < 0.7F) {
            return swampland;
        }

        if (temperature < 0.5F) {
            return taiga;
        }

        if (temperature < 0.97F) {
            if (humidity < 0.35F) {
                return shrubland;
            } else {
                return forest;
            }
        }

        if (humidity < 0.45F) {
            return plains;
        }

        if (humidity < 0.9F) {
            return seasonalForest;
        } else {
            return rainforest;
        }
    }

    public static void generateBiomeLookup() {
        for(int t = 0; t < 64; ++t) {
            for(int h = 0; h < 64; ++h) {
                biomeLookupTable[t + h * 64] = getBiome((float)t / 63.0F, (float)h / 63.0F);
            }
        }
    }

    static {
        generateBiomeLookup();
    }

    public static ClimateMap getBiomeFromLookup(double temperature, double humidity) {
        int t = (int)(temperature * 63.0D);
        int h = (int)(humidity * 63.0D);
        return biomeLookupTable[t + h * 64];
    }
}
