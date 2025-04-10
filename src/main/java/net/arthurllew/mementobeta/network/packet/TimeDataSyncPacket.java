package net.arthurllew.mementobeta.network.packet;

import net.arthurllew.mementobeta.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for sending entire dimension time datat to client.
 */
public class TimeDataSyncPacket {
    private final boolean isTimeLocked;
    private final long fixedTime;

    /**
     * Packet constructor.
     * @param isTimeLocked time lock.
     * @param fixedTime fixed time in ticks.
     */
    public TimeDataSyncPacket(boolean isTimeLocked, long fixedTime) {
        this.isTimeLocked = isTimeLocked;
        this.fixedTime = fixedTime;
    }

    /**
     * Packet decoder.
     * @param buffer data buffer.
     */
    public TimeDataSyncPacket(FriendlyByteBuf buffer) {
        this.isTimeLocked = buffer.readBoolean();
        this.fixedTime = buffer.readLong();
    }

    /**
     * Packet encoder.
     * @param buffer data buffer.
     */
    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.isTimeLocked);
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
                        () -> () -> ClientPacketHandler.handleTimeDataSyncPacket(this.isTimeLocked, this.fixedTime)));
        context.setPacketHandled(true);
    }
}
