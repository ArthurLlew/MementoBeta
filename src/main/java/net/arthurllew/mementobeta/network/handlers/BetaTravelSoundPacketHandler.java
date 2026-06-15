package net.arthurllew.mementobeta.network.handlers;

import net.arthurllew.mementobeta.network.packet.BetaTravelSoundPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BetaTravelSoundPacketHandler implements IPayloadHandler<BetaTravelSoundPacket> {
    /**
     * Handles {@link net.arthurllew.mementobeta.network.packet.BetaTravelSoundPacket} on client.
     */
    @Override
    public void handle(BetaTravelSoundPacket payload, IPayloadContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            // Play travel sound to player
            client.getSoundManager().play(SimpleSoundInstance
                    .forLocalAmbience(SoundEvents.PORTAL_TRAVEL,
                            client.level.getRandom().nextFloat() * 0.4F + 0.8F, 0.25F));
        }
    }
}
