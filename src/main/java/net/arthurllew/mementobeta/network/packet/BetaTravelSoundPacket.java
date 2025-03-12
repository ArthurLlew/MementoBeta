package net.arthurllew.mementobeta.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
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
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    Minecraft client = Minecraft.getInstance();
                    if (client.player != null && client.level != null) {
                        // Play travel sound
                        client.getSoundManager().play(SimpleSoundInstance
                                .forLocalAmbience(SoundEvents.PORTAL_TRAVEL,
                                        client.level.getRandom().nextFloat() * 0.4F + 0.8F, 0.25F));
                    }
                }));
        context.setPacketHandled(true);
    }
}
