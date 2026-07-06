package net.arthurllew.mementobeta.world.biome;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;

public enum BetaBiomeSeasons {
    // Seasons
    SPRING,
    SUMMER,
    AUTUMN,
    WINTER;

    // Season static values
    public static final long SEASON_DURATION = MementoBetaDimension.DAY_CYCLE_TOTAL_TIME * 14;      // 14 days
    public static final long TRANSITION_DURATION = MementoBetaDimension.DAY_CYCLE_TOTAL_TIME * 3;   // 3 days
    public static final long WINTER_START = MementoBetaDimension.SEASON_CYCLE_TOTAL_TIME - SEASON_DURATION;
    public static final long WINTER_END = MementoBetaDimension.SEASON_CYCLE_TOTAL_TIME;
    public static final float AUTUMN_TEMPERATURE = 0.5f;
    public static final float WINTER_TEMPERATURE = -0.25f;
    public static final float SPRING_TEMPERATURE = 0.65f;

    /**
     * Biomes that have seasons.
     */
    public static final TagKey<Biome> BIOMES_WITH_SEASONS_TAG = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "seasonable"));

    /**
     * @return enum value from season
     */
    public static BetaBiomeSeasons mapSeason(long season, int shift) {
        return values()[((int)(season / SEASON_DURATION) + shift) % 4];
    }

    /**
     * @return season temperature
     */
    public static float mapSeasonTemperature(long season, float biomeTemperature, int shift) {
        return switch (mapSeason(season, shift)) {
            case SPRING -> SPRING_TEMPERATURE;
            case AUTUMN -> AUTUMN_TEMPERATURE;
            case WINTER -> WINTER_TEMPERATURE;
            default -> biomeTemperature;
        };
    }

    /**
     * @return current temperature of season
     */
    public static float seasonModifyTemperature(long season, float biomeTemperature) {
        // Current season temperature
        float seasonTemperature = mapSeasonTemperature(season, biomeTemperature, 0);

        // Ticks of season
        long seasonTicks = season % SEASON_DURATION;
        // Start of season transition
        long transitionStartTick = SEASON_DURATION - TRANSITION_DURATION;

        // If season entered transition
        if (seasonTicks >= transitionStartTick) {
            // Get transition progress
            float t = (float) (seasonTicks - transitionStartTick) / TRANSITION_DURATION;

            // Compute smooth temperature transition
            return Mth.lerp(0.5f * (1.0f - (float) Math.cos(t * Math.PI)),
                    seasonTemperature,
                    mapSeasonTemperature(season, biomeTemperature, 1));
        }

        // Pure season temperature
        return seasonTemperature;
    }

    /**
     * @return 1/probability of snow/ice to melt
     */
    public static int mapSeasonMelting(long season) {
        return switch (mapSeason(season, 0)) {
            case SPRING -> 3;
            case AUTUMN -> 10;
            default -> 5;
        };
    }

    /**
     * @param season current season
     * @return whether it is winter
     */
    public static boolean isWinter(long season)
    {
        return season >= WINTER_START && season <= WINTER_END;
    }
    /**
     * @param season current season
     * @return whether it is not winter
     */
    public static boolean notWinter(long season)
    {
        return !isWinter(season);
    }
}
