package net.arthurllew.mementobeta.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalLong;

public class BetaSeedCapability implements INBTSerializable<CompoundTag> {
    /**
     * Tries to find this capability in level world.
     * @return {@link Optional} from this class.
     */
    public static LazyOptional<BetaSeedCapability> get(Level world) {
        return world.getCapability(MementoBetaCapabilities.BETA_SEED_CAPABILITY);
    }

    /**
     * Level to which this data is attached.
     */
    private final Level level;
    /**
     * Separate seed flag for beta dimension.
     */
    private boolean hasBetaSeed = false;
    /**
     * Separate seed for beta dimension.
     */
    private long betaSeed = 0;

    /**
     * Beta dimension seed capability.
     * @param level level to which this time data will be attached.
     */
    public BetaSeedCapability(Level level) {
        this.level = level;
    }

    /**
     * @return level to which this data is attached.
     */
    public Level getLevel() {
        return this.level;
    }

    /**
     * @return beta dimension seed.
     */
    public @NotNull OptionalLong getBetaSeed() {
        return this.hasBetaSeed ? OptionalLong.of(this.betaSeed) : OptionalLong.empty();
    }

    /**
     * @param seed beta dimension seed.
     */
    public void setBetaSeed(long seed) {
        this.hasBetaSeed = true;
        this.betaSeed = seed;
    }

    /**
     * Saves seed in the world save file.
     * @return NBT compound.
     */
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        compound.putBoolean("hasSeed", hasBetaSeed);
        compound.putLong("betaSeed", betaSeed);
        return compound;
    }

    /**
     * Restores seed from the world save file.
     * @param compound NBT compound.
     */
    public void deserializeNBT(CompoundTag compound) {
        if (compound.contains("hasSeed")) {
            hasBetaSeed = compound.getBoolean("hasSeed");
        }
        if (compound.contains("betaSeed")) {
            betaSeed = compound.getLong("betaSeed");
        }
    }
}
