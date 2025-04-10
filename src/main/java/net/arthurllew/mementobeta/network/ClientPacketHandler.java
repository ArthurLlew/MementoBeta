package net.arthurllew.mementobeta.network;

import net.arthurllew.mementobeta.capabilities.BetaTimeCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Used as proxy to prevent server crashing from loading client world.
 */
public abstract class ClientPacketHandler {
    /**
     * Handles {@link net.arthurllew.mementobeta.network.packet.TimeLockPacket} on client.
     */
    public static void handleTimeLockPacket(boolean isTimeLocked) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            // Update time lock on client
            BetaTimeCapability.get(client.level).ifPresent(time -> time.setTimeLock(isTimeLocked));
            // Notify player
            client.player.sendSystemMessage(Component.literal("Beta world time lock is now "
                    + (isTimeLocked ? "on" : "off")));
        }
    }

    /**
     * Handles {@link net.arthurllew.mementobeta.network.packet.FixedTimePacket} on client.
     */
    public static void handleFixedTimePacket(long fixedTime) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            // Update fixed time on client
            BetaTimeCapability.get(client.level).ifPresent(time -> time.setFixedTime(fixedTime));
            // Notify player
            client.player.sendSystemMessage(Component.literal("Beta world fixed time was changed to "
                    + fixedTime));
        }
    }

    /**
     * Handles {@link net.arthurllew.mementobeta.network.packet.TimeDataSyncPacket} on client.
     */
    public static void handleTimeDataSyncPacket(boolean isTimeLocked, long fixedTime) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            // Update time data on client
            BetaTimeCapability.get(client.level).ifPresent(time -> time.setTimeData(isTimeLocked, fixedTime));
        }
    }

    /**
     * Handles {@link net.arthurllew.mementobeta.network.packet.BetaTravelSoundPacket} on client.
     */
    public static void handleBetaTravelSoundPacket() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            // Play travel sound to player
            client.getSoundManager().play(SimpleSoundInstance
                    .forLocalAmbience(SoundEvents.PORTAL_TRAVEL,
                            client.level.getRandom().nextFloat() * 0.4F + 0.8F, 0.25F));
        }
    }

    /**
     * Handles {@link net.arthurllew.mementobeta.network.packet.MoltenBlockPacket} on client.
     */
    public static void handleMoltenBlockPacket(BlockPos pos) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            RandomSource randomSource = client.level.getRandom();

            // Play lava sound
            client.level.playLocalSound(pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                    2.6F + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8F, false);

            // Add smoke particles
            for(int i = 0; i < 8; ++i) {
                client.level.addParticle(ParticleTypes.LARGE_SMOKE,
                        (double)pos.getX() + randomSource.nextDouble(),
                        (double)pos.getY() + 1.1D,
                        (double)pos.getZ() + randomSource.nextDouble(),
                        0.0D, 0.0D, 0.0D);
            }
        }
    }
}
