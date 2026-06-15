package net.arthurllew.mementobeta.attachments.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arthurllew.mementobeta.network.MementoBetaNetwork;
import net.arthurllew.mementobeta.network.packet.*;
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
 * Beta dimension season data.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BetaSeasonData extends SavedData {
    public static final String ID = "betaworld_season";

    /**
     * Codec for serialization.
     */
    public static final Codec<BetaSeasonData> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.LONG.fieldOf("season").forGetter(BetaSeasonData::getSeason),
            Codec.BOOL.fieldOf("is_season_locked").forGetter(BetaSeasonData::isSeasonLocked),
            Codec.LONG.fieldOf("fixed_season").forGetter(BetaSeasonData::getFixedSeason)
        ).apply(instance, BetaSeasonData::new));

    /**
     * Factory.
     */
    public static final Factory<BetaSeasonData> FACTORY = new Factory<>(
            BetaSeasonData::new, BetaSeasonData::load);

    /**
     * Current season.
     */
    private long season = 0L;
    /**
     * Whether the season is locked.
     */
    private boolean isSeasonLocked = false;
    /**
     * Fixed season. Season will slowly adjust itself to this value or/and will not differ from it
     * when season is locked.
     */
    private long fixedSeason = MementoBetaDimension.SEASON_CYCLE_TOTAL_TIME / 4;

    /**
     * Beta dimension season attachment (codec constructor).
     */
    public BetaSeasonData(long season, boolean isSeasonLocked, long fixedSeason) {
        this.season = season;
        this.isSeasonLocked = isSeasonLocked;
        this.fixedSeason = fixedSeason % MementoBetaDimension.SEASON_CYCLE_TOTAL_TIME;
    }
    public BetaSeasonData() {}

    /**
     * @return current season
     */
    public long getSeason() {
        return this.season;
    }
    /**
     * @param season new season
     */
    public void setSeason(long season) {
        this.season = season % MementoBetaDimension.SEASON_CYCLE_TOTAL_TIME;
        this.setDirty();
    }
    /**
     * Synchronizes season value with client for all players that are in correct dimension.
     *
     * @param level dimension level
     */
    public void syncSeason(Level level) {
        // Do this on server only
        if (level instanceof ServerLevel serverLevel) {
            // Send message to every player in this dimension
            MementoBetaNetwork.sendToPlayersInDimension(serverLevel, new SeasonPacket(this.season));
        }
    }

    /**
     * @return whether season is locked
     */
    public boolean isSeasonLocked() {
        return this.isSeasonLocked;
    }
    /**
     * @param isTimeLocked new season lock value
     */
    public void setSeasonLock(boolean isTimeLocked) {
        this.isSeasonLocked = isTimeLocked;
        this.setDirty();
    }
    /**
     * Synchronizes season lock value with client for all players that are in correct dimension.
     *
     * @param level dimension level
     */
    public void syncSeasonLock(Level level) {
        // Do this on server only
        if (level instanceof ServerLevel serverLevel) {
            // Send message to every player in this dimension
            MementoBetaNetwork.sendToPlayersInDimension(serverLevel, new SeasonLockPacket(this.isSeasonLocked));
        }
    }

    /**
     * @return fixed season in ticks
     */
    public long getFixedSeason() {
        return this.fixedSeason;
    }
    /**
     * @param fixedSeason new fixed season in ticks
     */
    public void setFixedSeason(long fixedSeason) {
        this.fixedSeason = fixedSeason % MementoBetaDimension.SEASON_CYCLE_TOTAL_TIME;
        this.setDirty();
    }
    /**
     * Synchronizes fixed season value with client for all players that are in correct dimension.
     *
     * @param level dimension level
     */
    public void syncFixedSeason(Level level) {
        // Do this on server only
        if (level instanceof ServerLevel serverLevel) {
            // Send message to every player in this dimension
            MementoBetaNetwork.sendToPlayersInDimension(serverLevel, new FixedSeasonPacket(this.fixedSeason));
        }
    }

    /**
     * @param season new season value
     * @param isSeasonLocked new season lock value
     * @param fixedSeason new fixed season in ticks
     */
    public void setSeasonData(long season, boolean isSeasonLocked, long fixedSeason) {
        setSeason(season);
        setSeasonLock(isSeasonLocked);
        setFixedSeason(fixedSeason);
        this.setDirty();
    }
    /**
     * Synchronizes season data with client of given player.
     *
     * @param player server player
     * @param level dimension level
     */
    public void syncSeasonData(Level level, ServerPlayer player) {
        // Do this on server only
        if (level instanceof ServerLevel) {
            // Send message to player
            MementoBetaNetwork.sendToPlayer(player,
                    new SeasonDataSyncPacket(this.season, this.isSeasonLocked, this.fixedSeason));
        }
    }

    /**
     * Ticks season.
     */
    public void tick() {
        if (this.isSeasonLocked) {
            if (this.season != this.fixedSeason) {
                // This code will slowly shift time to required position, so it looks more natural
                long diff = this.fixedSeason - (this.season % MementoBetaDimension.SEASON_CYCLE_TOTAL_TIME);
                if (diff > MementoBetaDimension.SEASON_CYCLE_TOTAL_TIME / 2) {
                    diff -= MementoBetaDimension.SEASON_CYCLE_TOTAL_TIME;
                }
                else if (diff < -MementoBetaDimension.SEASON_CYCLE_TOTAL_TIME / 2) {
                    diff += MementoBetaDimension.SEASON_CYCLE_TOTAL_TIME;
                }
                setSeason(this.season + Mth.clamp(diff, -100, 100));
            }
        } else {
            setSeason(this.season + 1);
        }
    }

    /**
     * Saves season data in the world save file.
     *
     * @param compound NBT compound
     * @param registries game registries
     *
     * @return modified NBT compound
     */
    @Override
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider registries) {
        compound.putLong("Season", this.season);
        compound.putBoolean("IsSeasonLocked", this.isSeasonLocked);
        compound.putLong("FixedSeason", this.fixedSeason);
        return compound;
    }

    /**
     * Restores season data from the world save file.
     *
     * @param compound NBT compound
     * @param registries game registries
     */
    public static BetaSeasonData load(CompoundTag compound, HolderLookup.Provider registries) {
        BetaSeasonData data = new BetaSeasonData();

        if (compound.contains("Season")) {
            data.setSeason(compound.getLong("Season"));
        }
        if (compound.contains("IsSeasonLocked")) {
            data.setSeasonLock(compound.getBoolean("IsSeasonLocked"));
        }
        if (compound.contains("FixedSeason")) {
            data.setFixedSeason(compound.getLong("FixedSeason"));
        }

        return data;
    }
}
