package net.arthurllew.mementobeta.network.packet;

import net.arthurllew.mementobeta.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Handles time lock packet.
 */
public class TimeLockPacket {
    private final boolean isTimeLocked;

    /**
     * Packet constructor.
     * @param isTimeLocked time lock.
     */
    public TimeLockPacket(boolean isTimeLocked) {
        this.isTimeLocked = isTimeLocked;
    }

    /**
     * Packet decoder.
     * @param buffer data buffer.
     */
    public TimeLockPacket(FriendlyByteBuf buffer) {
        this.isTimeLocked = buffer.readBoolean();
    }

    /**
     * Packet encoder.
     * @param buffer data buffer.
     */
    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.isTimeLocked);
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
                        () -> () -> ClientPacketHandler.handleTimeLockPacket(this.isTimeLocked)));
        context.setPacketHandled(true);
    }
}
