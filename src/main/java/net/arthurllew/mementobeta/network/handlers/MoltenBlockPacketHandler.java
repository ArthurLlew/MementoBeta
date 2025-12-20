package net.arthurllew.mementobeta.network.handlers;

import net.arthurllew.mementobeta.network.packet.MoltenBlockPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public class MoltenBlockPacketHandler implements IPayloadHandler<MoltenBlockPacket> {
    /**
     * Handles {@link net.arthurllew.mementobeta.network.packet.MoltenBlockPacket} on client.
     */
    @Override
    public void handle(MoltenBlockPacket payload, IPayloadContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            RandomSource randomSource = client.level.getRandom();

            // Play lava sound
            client.level.playLocalSound(payload.pos(), SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                    2.6F + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8F, false);

            // Add smoke particles
            for(int i = 0; i < 8; ++i) {
                client.level.addParticle(ParticleTypes.LARGE_SMOKE,
                        (double)payload.pos().getX() + randomSource.nextDouble(),
                        (double)payload.pos().getY() + 1.1D,
                        (double)payload.pos().getZ() + randomSource.nextDouble(),
                        0.0D, 0.0D, 0.0D);
            }
        }
    }
}
