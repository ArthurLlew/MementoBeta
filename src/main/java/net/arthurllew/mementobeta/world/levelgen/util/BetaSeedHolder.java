package net.arthurllew.mementobeta.world.levelgen.util;

import net.arthurllew.mementobeta.mixin.CreateWorldScreenWorldTabInjector;

/**
 * Used by {@link CreateWorldScreenWorldTabInjector} to store beta dimension seed from editbox.
 */
public class BetaSeedHolder {
    /**
     * Input string containing Beta dimension seed.
     */
    private static String betaSeedString = "";

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
}
