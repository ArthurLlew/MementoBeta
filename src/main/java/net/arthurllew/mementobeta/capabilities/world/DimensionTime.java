package net.arthurllew.mementobeta.capabilities.world;

import net.arthurllew.mementobeta.capabilities.MementoBetaCapabilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;

/**
 * Dimension time component.
 */
public interface DimensionTime extends INBTSerializable<CompoundTag> {
    /**
     * Tries to find this capability in level world.
     * @param world world.
     * @return {@link Optional} from this class.
     */
    static LazyOptional<DimensionTime> get(Level world) {
        return world.getCapability(MementoBetaCapabilities.BETA_TIME_COMPONENT);
    }

    /**
     * @return world to which this time data belongs
     */
    Level getLevel();

    /**
     * @return current day time.
     */
    long getDayTime();
    /**
     * @param time new day time.
     */
    void setDayTime(long time);

    /**
     * @return whether time is locked.
     */
    boolean isTimeLocked();
    /**
     * @param isTimeLocked new time lock value.
     */
    void setTimeLock(boolean isTimeLocked);
    /**
     * Synchronizes time lock value with client for all player that are in correct dimension.
     */
    void syncTimeLock();

    /**
     * @return fixed day cycle time in ticks.
     */
    long getFixedTime();
    /**
     * @param newFixedTime new fixed day cycle time in ticks.
     */
    void setFixedTime(long newFixedTime);
    /**
     * Synchronizes fixed time value with client for all player that are in correct dimension.
     */
    void syncFixedTime();

    /**
     * @param isTimeLocked new time lock value.
     * @param newFixedTime new fixed day cycle time in ticks.
     */
    void setTimeData(boolean isTimeLocked, long newFixedTime);
    /**
     * Synchronizes time data with client of given player.
     * @param player server player.
     */
    void syncTimeData(ServerPlayer player);

    /**
     * Ticks custom time in provided world.
     * @param world world.
     * @return new time.
     */
    long tickTime(Level world);
}
