package net.arthurllew.mementobeta.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MoltenBlockSolidifiedPacket {
    /**
     * Molten block position.
     */
    private final BlockPos pos;

    /**
     * Packet constructor.
     */
    public MoltenBlockSolidifiedPacket(BlockPos pos) {
        this.pos = pos;
    }

    /**
     * Packet decoder.
     * @param buffer data buffer.
     */
    @SuppressWarnings("unused")
    public MoltenBlockSolidifiedPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }

    /**
     * Packet encoder.
     * @param buffer data buffer.
     */
    @SuppressWarnings("unused")
    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
    }

    /**
     * Packet consumer.
     * @param supplier network context supplier.
     */
    public void consume(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
                // Execute code only on physical client
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    Minecraft client = Minecraft.getInstance();
                    if (client.player != null && client.level != null) {
                        RandomSource randomSource = client.level.getRandom();

                        // Play lava sound
                        client.level.playLocalSound(pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS,
                                0.5F,
                                2.6F + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8F,
                                false);

                        // Add smoke particles
                        for(int i = 0; i < 8; ++i) {
                            client.level.addParticle(ParticleTypes.LARGE_SMOKE,
                                    (double)pos.getX() + randomSource.nextDouble(),
                                    (double)pos.getY() + 1.1D,
                                    (double)pos.getZ() + randomSource.nextDouble(),
                                    0.0D, 0.0D, 0.0D);
                        }
                    }
                }));
        context.setPacketHandled(true);
    }
}
