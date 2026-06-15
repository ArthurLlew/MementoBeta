package net.arthurllew.mementobeta.world.levelgen.util;

import net.arthurllew.mementobeta.attachments.data.BetaSeedData;
import net.arthurllew.mementobeta.mixin.ChunkMapInjector;
import net.arthurllew.mementobeta.mixin.CreateWorldScreenWorldTabInjector;
import net.arthurllew.mementobeta.mixin.ServerLevelInjector;

/**
 * Used by
 * {@link CreateWorldScreenWorldTabInjector} to store beta dimension seed from editbox
 * and
 * {@link ChunkMapInjector} to avoid {@link NullPointerException} in {@link ServerLevelInjector}
 */
public class BetaSeedHolder {
    /**
     * Input string containing Beta dimension seed.
     */
    private static String betaSeedString = "";

    /**
     * Saved {@link BetaSeedData} instance for later use in {@link ServerLevelInjector}.
     */
    private static BetaSeedData savedBetaSeedInstance = null;

    /**
     * @param seed new Beta dimension seed string.
     */
    public static synchronized void setSeedString(String seed) {
        betaSeedString = seed;
    }

    /**
     * @return Beta dimension seed string.
     */
    public static synchronized String getSeedString() {
        return betaSeedString;
    }

    /**
     * @param seedData new Beta dimension seed data.
     */
    public static synchronized void setSavedBetaSeedInstance(BetaSeedData seedData) {
        savedBetaSeedInstance = seedData;
    }

    /**
     * @return Beta dimension seed data.
     */
    public static synchronized BetaSeedData getSavedBetaSeedInstance() {
        return savedBetaSeedInstance;
    }
}
