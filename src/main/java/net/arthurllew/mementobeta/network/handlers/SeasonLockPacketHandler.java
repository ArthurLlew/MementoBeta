package net.arthurllew.mementobeta.network.handlers;

import net.arthurllew.mementobeta.network.packet.SeasonLockPacket;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SeasonLockPacketHandler implements IPayloadHandler<SeasonLockPacket> {
    /**
     * Handles {@link SeasonLockPacket} on client.
     */
    @Override
    public void handle(SeasonLockPacket payload, IPayloadContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            // Update season lock on client
            if (client.level.hasData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT)) {
                client.level.getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT)
                        .setSeasonLock(payload.isSeasonLocked());

                // Notify player
                client.player.sendSystemMessage(Component.literal("Beta level season lock is now "
                        + (payload.isSeasonLocked() ? "on" : "off")));
            }
        }
    }
}
