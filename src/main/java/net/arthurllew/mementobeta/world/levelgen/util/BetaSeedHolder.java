package net.arthurllew.mementobeta.world.levelgen.util;

import net.arthurllew.mementobeta.mixin.CreateWorldScreenWorldTabInjector;

/**
 * Used by editbox in {@link CreateWorldScreenWorldTabInjector} to store beta dimension seed. Uses {@code synchronized}
 * get/set methods.
 */
public class BetaSeedHolder {
    private static String betaSeed = "";

    public static synchronized void setSeed(String seed) {
        betaSeed = seed;
    }

    public static synchronized String getSeed() {
        return betaSeed;
    }
}
