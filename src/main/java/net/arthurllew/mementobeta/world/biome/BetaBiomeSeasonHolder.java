package net.arthurllew.mementobeta.world.biome;

import net.arthurllew.mementobeta.attachments.BetaLevelSeasonAttachment;
import net.arthurllew.mementobeta.mixin.ServerLevelInjector;

/**
 * Is used to
 */
public class BetaBiomeSeasonHolder {
    /**
     * Saved {@link BetaLevelSeasonAttachment} instance for later use in {@link ServerLevelInjector}.
     */
    private static BetaLevelSeasonAttachment savedBetaSeasonInstance = null;

    /**
     * @param seasonData new Beta dimension season data.
     */
    public static synchronized void setSavedBetaSeasonInstance(BetaLevelSeasonAttachment seasonData) {
        savedBetaSeasonInstance = seasonData;
    }

    /**
     * Modifies provided biome temperature by season.
     */
    public static float seasonModifyTemperature(float temperature) {
        // Season temperature
        if (savedBetaSeasonInstance != null)
        {
            return BetaBiomeSeasons.seasonModifyTemperature(savedBetaSeasonInstance.getSeason(), temperature);
        }

        // Default behaviour
        return temperature;
    }
}
