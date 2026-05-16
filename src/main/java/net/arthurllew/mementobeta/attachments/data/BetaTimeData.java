package net.arthurllew.mementobeta.attachments.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arthurllew.mementobeta.network.MementoBetaNetwork;
import net.arthurllew.mementobeta.network.packet.FixedTimePacket;
import net.arthurllew.mementobeta.network.packet.TimeDataSyncPacket;
import net.arthurllew.mementobeta.network.packet.TimeLockPacket;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Beta dimension time data.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BetaTimeData extends SavedData {
    /**
     * Codec fo serialization.
     */
    public static final Codec<BetaTimeData> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.LONG.fieldOf("day_time").forGetter(BetaTimeData::getDayTime),
            Codec.BOOL.fieldOf("is_time_locked").forGetter(BetaTimeData::isTimeLocked),
            Codec.LONG.fieldOf("fixed_time").forGetter(BetaTimeData::getFixedTime)
        ).apply(instance, BetaTimeData::new));

    /**
     * Factory.
     */
    public static final Factory<BetaTimeData> FACTORY = new SavedData.Factory<>(
            BetaTimeData::new, BetaTimeData::load);

    /**
     * Level to which this data is attached.
     */
    private Level level;

    /**
     * Current day time.
     */
    private long dayTime = 0L;
    /**
     * Whether the time is locked.
     */
    private boolean isTimeLocked = false;
    /**
     * Fixed day time. Time will slowly adjust itself to this value or/and will not differ from it
     * when time is locked.
     */
    private long fixedTime = MementoBetaDimension.DAY_CYCLE_TOTAL_TIME / 4;

    /**
     * Beta dimension time attachment (codec constructor).
     */
    public BetaTimeData(long dayTime, boolean isTimeLocked, long fixedTime) {
        this.dayTime = dayTime;
        this.isTimeLocked = isTimeLocked;
        this.fixedTime = fixedTime % MementoBetaDimension.DAY_CYCLE_TOTAL_TIME;
    }
    public BetaTimeData() {}

    /**
     * @param level level to which this data must be attached to
     */
    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * @return current day time
     */
    public long getDayTime() {
        return this.dayTime;
    }
    /**
     * @param time new day time
     */
    public void setDayTime(long time) {
        this.dayTime = time;
        this.setDirty();
    }
    /**
     * @return whether time is locked
     */
    public boolean isTimeLocked() {
        return this.isTimeLocked;
    }
    /**
     * @param isTimeLocked new time lock value
     */
    public void setTimeLock(boolean isTimeLocked) {
        this.isTimeLocked = isTimeLocked;
        this.setDirty();
    }
    /**
     * @return fixed day cycle time in ticks
     */
    public long getFixedTime() {
        return this.fixedTime;
    }
    /**
     * @param newFixedTime new fixed day cycle time in ticks
     */
    public void setFixedTime(long newFixedTime) {
        this.fixedTime = newFixedTime % MementoBetaDimension.DAY_CYCLE_TOTAL_TIME;
        this.setDirty();
    }

    /**
     * Synchronizes time lock value with client for all player that are in correct dimension.
     *
     * @param level dimension level
     */
    public void syncTimeLock(Level level) {
        // Do this on server only
        if (level instanceof ServerLevel serverLevel) {
            // Send message to every player in this dimension
            MementoBetaNetwork.sendToPlayersInDimension(serverLevel, new TimeLockPacket(this.isTimeLocked));
        }
    }

    /**
     * Synchronizes fixed time value with client for all player that are in correct dimension.
     *
     * @param level dimension level
     */
    public void syncFixedTime(Level level) {
        // Do this on server only
        if (level instanceof ServerLevel serverLevel) {
            // Send message to every player in this dimension
            MementoBetaNetwork.sendToPlayersInDimension(serverLevel, new FixedTimePacket(this.fixedTime));
        }
    }

    /**
     * @param isTimeLocked new time lock value
     * @param newFixedTime new fixed day cycle time in ticks
     */
    public void setTimeData(boolean isTimeLocked, long newFixedTime) {
        setTimeLock(isTimeLocked);
        setFixedTime(newFixedTime);
        this.setDirty();
    }

    /**
     * Synchronizes time data with client of given player.
     *
     * @param player server player
     * @param level dimension level
     */
    public void syncTimeData(Level level, ServerPlayer player) {
        // Do this on server only
        if (level instanceof ServerLevel) {
            // Send message to player
            MementoBetaNetwork.sendToPlayer(player, new TimeDataSyncPacket(this.isTimeLocked, this.fixedTime));
        }
    }

    /**
     * Ticks custom time in provided level.
     *
     * @param level level
     *
     * @return new time
     */
    public long tickTime(Level level) {
        this.setDirty();

        long dayTime = level.getDayTime();
        if (this.isTimeLocked) {
            if (dayTime != this.fixedTime) {
                // This code will slowly shift time to required position, so it looks more natural
                long diff = this.fixedTime - (dayTime % MementoBetaDimension.DAY_CYCLE_TOTAL_TIME);
                if (diff > MementoBetaDimension.DAY_CYCLE_TOTAL_TIME / 2) {
                    diff -= MementoBetaDimension.DAY_CYCLE_TOTAL_TIME;
                }
                else if (diff < -MementoBetaDimension.DAY_CYCLE_TOTAL_TIME / 2) {
                    diff += MementoBetaDimension.DAY_CYCLE_TOTAL_TIME;
                }
                dayTime += Mth.clamp(diff, -10, 10);
            }
        } else {
            dayTime++;
        }
        return dayTime;
    }

    /**
     * Saves time data in the world save file.
     *
     * @param compound NBT compound
     * @param registries game registries
     *
     * @return modified NBT compound
     */
    @Override
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider registries) {
        if (level != null) {
            compound.putLong("DayTime", this.level.getDayTime());
        }
        else {
            compound.putLong("DayTime", this.dayTime);
        }
        compound.putBoolean("isTimeLocked", this.isTimeLocked);
        compound.putLong("FixedTime", this.fixedTime);
        return compound;
    }

    /**
     * Restores time data from the world save file.
     *
     * @param compound NBT compound
     * @param registries game registries
     */
    public static BetaTimeData load(CompoundTag compound, HolderLookup.Provider registries) {
        BetaTimeData data = new BetaTimeData();

        if (compound.contains("DayTime")) {
            data.setDayTime(compound.getLong("DayTime"));
        }
        if (compound.contains("isTimeLocked")) {
            data.setTimeLock(compound.getBoolean("isTimeLocked"));
        }
        if (compound.contains("FixedTime")) {
            data.setFixedTime(compound.getLong("FixedTime"));
        }

        return data;
    }
}
