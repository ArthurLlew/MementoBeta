package net.arthurllew.mementobeta.attachments.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Beta dimension seed data.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BetaSeedData extends SavedData {
    /**
     * Codec fo serialization.
     */
    public static final MapCodec<BetaSeedData> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.LONG.fieldOf("beta_seed").forGetter(BetaSeedData::getBetaSeed)
            ).apply(instance, BetaSeedData::new));

    /**
     * Factory.
     */
    public static final Factory<BetaSeedData> FACTORY = new SavedData.Factory<>(
            BetaSeedData::new, BetaSeedData::load);

    /**
     * Whether this data was not read from disk.
     */
    private boolean wasAbsent = true;

    /**
     * Separate seed for beta dimension.
     */
    private long betaSeed = 0;

    /**
     * Beta dimension seed data.
     */
    public BetaSeedData(long betaSeed) { this.betaSeed = betaSeed; }
    public BetaSeedData() {}

    /**
     * @return whether this data was not read from disk.
     */
    public boolean wasAbsent() {
        return this.wasAbsent;
    }

    /**
     * @return beta dimension seed.
     */
    public long getBetaSeed() {
        return this.betaSeed;
    }
    /**
     * @param seed beta dimension seed.
     */
    public void setBetaSeed(long seed) {
        this.betaSeed = seed;
        this.setDirty();
    }

    /**
     * Saves seed in the world save file.
     * @param compound NBT compound.
     * @param registries game registries.
     * @return modified NBT compound.
     */
    @Override
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider registries) {
        compound.putLong("betaSeed", betaSeed);
        return compound;
    }

    /**
     * Restores seed from the world save file.
     * @param compound NBT compound.
     * @param registries game registries.
     */
    public static BetaSeedData load(CompoundTag compound, HolderLookup.Provider registries) {
        BetaSeedData data = new BetaSeedData();
        data.wasAbsent = false;

        if (compound.contains("betaSeed")) {
            data.setBetaSeed(compound.getLong("betaSeed"));
        }

        return data;
    }
}
