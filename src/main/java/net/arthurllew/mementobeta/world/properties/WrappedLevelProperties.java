package net.arthurllew.mementobeta.world.properties;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper for level properties. Allows to use custom dimension time.
 */
public class WrappedLevelProperties extends DerivedLevelData {
    /**
     * Wrapped server level data.
     */
    private final ServerLevelData wrappedLevelData;
    /**
     * Wrapped game rules.
     */
    private final WrappedGameRules wrappedGameRules;
    /**
     * Custom daytime.
     */
    private long dayTime;

    public WrappedLevelProperties(WorldData worldData, ServerLevelData overworldData, long dayTime) {
        super(worldData, overworldData);
        this.wrappedLevelData = overworldData;
        // Currently, no rules are blacklisted
        this.wrappedGameRules = new WrappedGameRules(worldData.getGameRules(), ImmutableSet.of());
        this.dayTime = dayTime;
    }

    /**
     * @return world day time in ticks.
     */
    @Override
    public long getDayTime() {
        return this.dayTime;
    }

    /**
     * @param time new world day time.
     */
    @Override
    public void setDayTime(long time) {
        this.dayTime = time;
    }

    /**
     * @param time number of ticks the weather will be clear.
     */
    @Override
    public void setClearWeatherTime(int time) {
        this.wrappedLevelData.setClearWeatherTime(time);
    }

    /**
     * @param raining whether it is raining.
     */
    @Override
    public void setRaining(boolean raining) {
        this.wrappedLevelData.setRaining(raining);
    }

    /**
     * @param time number of ticks until rain.
     */
    @Override
    public void setRainTime(int time) {
        this.wrappedLevelData.setRainTime(time);
    }

    /**
     * @param thundering whether it is thundering.
     */
    @Override
    public void setThundering(boolean thundering) {
        this.wrappedLevelData.setThundering(thundering);
    }

    /**
     * @param time number of ticks until next lightning bolt.
     */
    @Override
    public void setThunderTime(int time) {
        this.wrappedLevelData.setThunderTime(time);
    }

    /**
     * @return game rules.
     */
    @Override
    public @NotNull WrappedGameRules getGameRules() {
        return this.wrappedGameRules;
    }
}
