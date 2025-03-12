package net.arthurllew.mementobeta.network.packet;

import net.arthurllew.mementobeta.capabilities.BetaTimeCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    Minecraft client = Minecraft.getInstance();
                    if (client.player != null && client.level != null) {
                        // Update fixed time on client
                        BetaTimeCapability.get(client.level).ifPresent(time -> time.setFixedTime(fixedTime));
                        // Notify player
                        client.player.sendSystemMessage(Component.literal("Beta world fixed time was changed to "
                                + fixedTime));
                    }
        }));
        context.setPacketHandled(true);
    }
}
