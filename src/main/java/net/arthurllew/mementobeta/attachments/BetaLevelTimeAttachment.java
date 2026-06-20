package net.arthurllew.mementobeta.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arthurllew.mementobeta.network.MementoBetaNetwork;
import net.arthurllew.mementobeta.network.packet.FixedTimePacket;
import net.arthurllew.mementobeta.network.packet.TimeDataSyncPacket;
import net.arthurllew.mementobeta.network.packet.TimeLockPacket;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Beta dimension time data.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BetaLevelTimeAttachment {
    public static final String ID = "betaworld_time";

    /**
     * Codec for serialization.
     */
    public static final Codec<BetaLevelTimeAttachment> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.LONG.fieldOf("day_time").forGetter(BetaLevelTimeAttachment::getDayTime),
            Codec.BOOL.fieldOf("is_time_locked").forGetter(BetaLevelTimeAttachment::isTimeLocked),
            Codec.LONG.fieldOf("fixed_time").forGetter(BetaLevelTimeAttachment::getFixedTime)
        ).apply(instance, BetaLevelTimeAttachment::new));

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
     * Codec constructor.
     */
    public BetaLevelTimeAttachment(long dayTime, boolean isTimeLocked, long fixedTime) {
        this.dayTime = dayTime;
        this.isTimeLocked = isTimeLocked;
        this.fixedTime = fixedTime % MementoBetaDimension.DAY_CYCLE_TOTAL_TIME;
    }
    /**
     * Empty constructor.
     */
    public BetaLevelTimeAttachment() {}

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
    }
    /**
     * Synchronizes time lock value with client for all players that are in correct dimension.
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
     * @return fixed day cycle time in ticks
     */
    public long getFixedTime() {
        return this.fixedTime;
    }
    /**
     * @param fixedTime new fixed day cycle time in ticks
     */
    public void setFixedTime(long fixedTime) {
        this.fixedTime = fixedTime % MementoBetaDimension.DAY_CYCLE_TOTAL_TIME;
    }
    /**
     * Synchronizes fixed time value with client for all players that are in correct dimension.
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
     * @param dayTime new day time value
     * @param isTimeLocked new time lock value
     * @param newFixedTime new fixed day cycle time in ticks
     */
    public void setTimeData(long dayTime, boolean isTimeLocked, long newFixedTime) {
        setDayTime(dayTime);
        setTimeLock(isTimeLocked);
        setFixedTime(newFixedTime);
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
            MementoBetaNetwork.sendToPlayer(player,
                    new TimeDataSyncPacket(this.dayTime, this.isTimeLocked, this.fixedTime));
        }
    }

    /**
     * Ticks time in provided level.
     *
     * @param level level
     *
     * @return new time
     */
    public long tickTime(Level level) {
        // Get current daytime in level
        long dayTime = level.getDayTime();

        // If time is locked and current time is not equal fixed time
        if (this.isTimeLocked && dayTime != this.fixedTime) {
            // This code will slowly shift time to required position, so it looks more natural
            long diff = this.fixedTime - (dayTime % MementoBetaDimension.DAY_CYCLE_TOTAL_TIME);
            if (diff > MementoBetaDimension.DAY_CYCLE_TOTAL_TIME / 2) {
                diff -= MementoBetaDimension.DAY_CYCLE_TOTAL_TIME;
            }
            else if (diff < -MementoBetaDimension.DAY_CYCLE_TOTAL_TIME / 2) {
                diff += MementoBetaDimension.DAY_CYCLE_TOTAL_TIME;
            }
            dayTime += Mth.clamp(diff, -10, 10);
        // Normal ticking
        } else {
            dayTime++;
        }

        // Save daytime
        this.dayTime = dayTime;

        // Provide result to the outside world
        return dayTime;
    }
}
