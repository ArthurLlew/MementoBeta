package net.arthurllew.mementobeta.network.packet;

import net.arthurllew.mementobeta.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BetaTravelSoundPacket {
    /**
     * Packet constructor.
     */
    public BetaTravelSoundPacket() {}

    /**
     * Packet decoder.
     * @param buffer data buffer.
     */
    @SuppressWarnings("unused")
    public BetaTravelSoundPacket(FriendlyByteBuf buffer) {}

    /**
     * Packet encoder.
     * @param buffer data buffer.
     */
    @SuppressWarnings("unused")
    public void encoder(FriendlyByteBuf buffer) {}

    /**
     * Packet consumer.
     * @param supplier network context supplier.
     */
    public void consume(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
                // Execute code only on physical client
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> ClientPacketHandler.handleBetaTravelSoundPacket()));
        context.setPacketHandled(true);
    }
}
