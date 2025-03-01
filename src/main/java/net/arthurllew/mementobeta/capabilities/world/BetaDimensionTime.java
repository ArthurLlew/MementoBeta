package net.arthurllew.mementobeta.capabilities.world;

import net.arthurllew.mementobeta.network.DimensionPacketHandler;
import net.arthurllew.mementobeta.network.packet.FixedTimePacket;
import net.arthurllew.mementobeta.network.packet.TimeDataSyncPacket;
import net.arthurllew.mementobeta.network.packet.TimeLockPacket;
import net.arthurllew.mementobeta.world.BetaDimension;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * Beta dimension time component.
 */
public class BetaDimensionTime implements DimensionTime {
    /**
     * Level to which this time data is attached.
     */
    private final Level level;
    /**
     * Current day time.
     */
    private long dayTime = 6000L;
    /**
     * Whether the time is locked.
     */
    private boolean isTimeLocked = false;
    /**
     * Fixed day time. Time will slowly adjust itself to this value or/and will not differ from it
     * when time is locked.
     */
    private long fixedTime = 6000L;
    /**
     * Difference between total day cycle time and fixed time.
     */
    private long fixedTimeDifference = BetaDimension.DAY_CYCLE_TOTAL_TIME - this.fixedTime;

    /**
     * Constructor.
     * @param level level to which this time data will be attached.
     */
    public BetaDimensionTime(Level level) {
        this.level = level;
    }

    /**
     * @return level to which this time data is attached.
     */
    @Override
    public Level getLevel() {
        return this.level;
    }

    /**
     * @return current day time.
     */
    @Override
    public long getDayTime() {
        return this.dayTime;
    }

    /**
     * @param time new day time.
     */
    @Override
    public void setDayTime(long time) {
        this.dayTime = time;
    }

    /**
     * @return whether time is locked.
     */
    @Override
    public boolean isTimeLocked() {
        return this.isTimeLocked;
    }

    /**
     * @param isTimeLocked new time lock value.
     */
    @Override
    public void setTimeLock(boolean isTimeLocked) {
        this.isTimeLocked = isTimeLocked;
    }

    /**
     * Synchronizes time lock value with client for all player that are in correct dimension.
     */
    @Override
    public void syncTimeLock() {
        // Do this on server only
        if (this.level instanceof ServerLevel) {
            // Send message to every player in this dimension
            DimensionPacketHandler.sendToPlayersInDimension(new TimeLockPacket(this.isTimeLocked));
        }
    }

    /**
     * @return fixed day cycle time in ticks.
     */
    @Override
    public long getFixedTime() {
        return this.fixedTime;
    }

    /**
     * @param newFixedTime new fixed day cycle time in ticks.
     */
    @Override
    public void setFixedTime(long newFixedTime) {
        this.fixedTime = newFixedTime;
        this.fixedTimeDifference = BetaDimension.DAY_CYCLE_TOTAL_TIME - this.fixedTime;
    }

    /**
     * Synchronizes fixed time value with client for all player that are in correct dimension.
     */
    @Override
    public void syncFixedTime() {
        // Do this on server only
        if (this.level instanceof ServerLevel) {
            // Send message to every player in this dimension
            DimensionPacketHandler.sendToPlayersInDimension(new FixedTimePacket(this.fixedTime));
        }
    }

    /**
     * @param isTimeLocked new time lock value.
     * @param newFixedTime new fixed day cycle time in ticks.
     */
    @Override
    public void setTimeData(boolean isTimeLocked, long newFixedTime) {
        setTimeLock(isTimeLocked);
        setFixedTime(newFixedTime);
    }

    /**
     * Synchronizes time data with client of given player.
     * @param player server player.
     */
    @Override
    public void syncTimeData(ServerPlayer player) {
        // Do this on server only
        if (this.level instanceof ServerLevel) {
            // Send message to player
            DimensionPacketHandler.sendToPlayer(player, new TimeDataSyncPacket(this.isTimeLocked, this.fixedTime));
        }
    }

    /**
     * Saves time data in the world save file.
     * @return nbt compound.
     */
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        compound.putLong("DayTime", this.level.getDayTime());
        compound.putBoolean("isTimeLocked", this.isTimeLocked);
        compound.putLong("FixedTime", this.fixedTime);
        return compound;
    }

    /**
     * Restores time data from the world save file.
     * @param compound nbt compound.
     */
    @Override
    public void deserializeNBT(CompoundTag compound) {
        if (compound.contains("DayTime")) {
            this.setDayTime(compound.getLong("DayTime"));
        }
        if (compound.contains("isTimeLocked")) {
            this.setTimeLock(compound.getBoolean("isTimeLocked"));
        }
        if (compound.contains("FixedTime")) {
            this.setFixedTime(compound.getLong("FixedTime"));
        }
    }

    /**
     * Ticks custom time in provided level.
     * @param level level.
     * @return new time.
     */
    @Override
    public long tickTime(Level level) {
        long dayTime = level.getDayTime();
        if (this.isTimeLocked) {
            if (dayTime != this.fixedTime) {
                // This code will slowly shift time to required position, so it looks more natural
                long timeDistance = dayTime % BetaDimension.DAY_CYCLE_TOTAL_TIME;
                if (timeDistance > this.fixedTimeDifference) {
                    timeDistance -= BetaDimension.DAY_CYCLE_TOTAL_TIME;
                }
                long timeShift = (long) Mth.clamp(this.fixedTime - timeDistance, -10, 10);
                dayTime += timeShift;
            }
        } else {
            dayTime++;
        }
        return dayTime;
    }
}
