package net.arthurllew.mementobeta.network.packet;

import net.arthurllew.mementobeta.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FixedTimePacket {
    private final long fixedTime;

    /**
     * Packet constructor.
     * @param fixedTime fixed time in ticks.
     */
    public FixedTimePacket(long fixedTime) {
        this.fixedTime = fixedTime;
    }

    /**
     * Packet decoder.
     * @param buffer data buffer.
     */
    public FixedTimePacket(FriendlyByteBuf buffer) {
        this.fixedTime = buffer.readLong();
    }

    /**
     * Packet encoder.
     * @param buffer data buffer.
     */
    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeLong(this.fixedTime);
    }

    /**
     * Packet consumer.
     * @param supplier network context supplier.
     */
    public void consume(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
                // Execute code only on physical client
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> ClientPacketHandler.handleFixedTimePacket(this.fixedTime)));
        context.setPacketHandled(true);
    }
}
