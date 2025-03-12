package net.arthurllew.mementobeta.capabilities;

import net.arthurllew.mementobeta.network.MementoBetaPacketHandler;
import net.arthurllew.mementobeta.network.packet.FixedTimePacket;
import net.arthurllew.mementobeta.network.packet.TimeDataSyncPacket;
import net.arthurllew.mementobeta.network.packet.TimeLockPacket;
import net.arthurllew.mementobeta.world.BetaDimension;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;

/**
 * Beta dimension time capability.
 */
public class BetaTimeCapability implements INBTSerializable<CompoundTag> {
    /**
     * Tries to find this capability in level world.
     * @return {@link Optional} from this class.
     */
    public static LazyOptional<BetaTimeCapability> get(Level world) {
        return world.getCapability(MementoBetaCapabilities.BETA_TIME_CAPABILITY);
    }

    /**
     * Level to which this data is attached.
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
    public BetaTimeCapability(Level level) {
        this.level = level;
    }

    /**
     * @return level to which this data is attached.
     */
    public Level getLevel() {
        return this.level;
    }

    /**
     * @return current day time.
     */
    public long getDayTime() {
        return this.dayTime;
    }

    /**
     * @param time new day time.
     */
    public void setDayTime(long time) {
        this.dayTime = time;
    }

    /**
     * @return whether time is locked.
     */
    public boolean isTimeLocked() {
        return this.isTimeLocked;
    }

    /**
     * @param isTimeLocked new time lock value.
     */
    public void setTimeLock(boolean isTimeLocked) {
        this.isTimeLocked = isTimeLocked;
    }

    /**
     * Synchronizes time lock value with client for all player that are in correct dimension.
     */
    public void syncTimeLock() {
        // Do this on server only
        if (this.level instanceof ServerLevel) {
            // Send message to every player in this dimension
            MementoBetaPacketHandler.sendToPlayersInDimension(new TimeLockPacket(this.isTimeLocked));
        }
    }

    /**
     * @return fixed day cycle time in ticks.
     */
    public long getFixedTime() {
        return this.fixedTime;
    }

    /**
     * @param newFixedTime new fixed day cycle time in ticks.
     */
    public void setFixedTime(long newFixedTime) {
        this.fixedTime = newFixedTime;
        this.fixedTimeDifference = BetaDimension.DAY_CYCLE_TOTAL_TIME - this.fixedTime;
    }

    /**
     * Synchronizes fixed time value with client for all player that are in correct dimension.
     */
    public void syncFixedTime() {
        // Do this on server only
        if (this.level instanceof ServerLevel) {
            // Send message to every player in this dimension
            MementoBetaPacketHandler.sendToPlayersInDimension(new FixedTimePacket(this.fixedTime));
        }
    }

    /**
     * @param isTimeLocked new time lock value.
     * @param newFixedTime new fixed day cycle time in ticks.
     */
    public void setTimeData(boolean isTimeLocked, long newFixedTime) {
        setTimeLock(isTimeLocked);
        setFixedTime(newFixedTime);
    }

    /**
     * Synchronizes time data with client of given player.
     * @param player server player.
     */
    public void syncTimeData(ServerPlayer player) {
        // Do this on server only
        if (this.level instanceof ServerLevel) {
            // Send message to player
            MementoBetaPacketHandler.sendToPlayer(player, new TimeDataSyncPacket(this.isTimeLocked, this.fixedTime));
        }
    }

    /**
     * Saves time data in the world save file.
     * @return NBT compound.
     */
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        compound.putLong("DayTime", this.level.getDayTime());
        compound.putBoolean("isTimeLocked", this.isTimeLocked);
        compound.putLong("FixedTime", this.fixedTime);
        return compound;
    }

    /**
     * Restores time data from the world save file.
     * @param compound NBT compound.
     */
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
