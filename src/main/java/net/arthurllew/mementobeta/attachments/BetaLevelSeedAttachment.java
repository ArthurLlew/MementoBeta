package net.arthurllew.mementobeta.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arthurllew.mementobeta.world.levelgen.util.BetaSeedHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.WorldOptions;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Beta dimension seed data.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BetaLevelSeedAttachment {
    public static final String ID = "betaworld_seed";

    /**
     * Codec for serialization.
     */
    public static final Codec<BetaLevelSeedAttachment> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.fieldOf("was_absent").forGetter(BetaLevelSeedAttachment::wasAbsent),
                    Codec.LONG.fieldOf("seed").forGetter(BetaLevelSeedAttachment::getBetaSeed)
            ).apply(instance, BetaLevelSeedAttachment::new));

    /**
     * Whether this data was not read from disk.
     */
    private boolean wasAbsent = true;

    /**
     * Separate seed for beta dimension.
     */
    private long betaSeed = 0;

    /**
     * Codec constructor.
     */
    public BetaLevelSeedAttachment(boolean wasAbsent, long betaSeed) {
        this.wasAbsent = wasAbsent;
        this.betaSeed = betaSeed;
    }
    /**
     * Empty constructor.
     */
    public BetaLevelSeedAttachment() {}

    /**
     * @return whether this data was not read from disk
     */
    public boolean wasAbsent() {
        return this.wasAbsent;
    }

    /**
     * @return beta dimension seed
     */
    public long getBetaSeed() {
        return this.betaSeed;
    }
    /**
     * @param seed beta dimension seed
     */
    public void setBetaSeed(long seed) {
        this.betaSeed = seed;
    }

    /**
     * Is used to init seed with appropriate value on server load.
     */
    public void initSeed(MinecraftServer server) {
        if (wasAbsent()) {
            this.setBetaSeed(WorldOptions.parseSeed(BetaSeedHolder.getSeedString())
                    .orElse(server.getWorldData().worldGenOptions().seed()));
            this.wasAbsent = false;
        }
    }
}
