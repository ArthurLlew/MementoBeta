package net.arthurllew.mementobeta.network.handlers;

import net.arthurllew.mementobeta.network.packet.FixedSeasonPacket;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FixedSeasonPacketHandler implements IPayloadHandler<FixedSeasonPacket> {
    /**
     * Handles {@link FixedSeasonPacket} on client.
     */
    @Override
    public void handle(FixedSeasonPacket payload, IPayloadContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            // Update fixed season on client
            if (client.level.hasData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT)) {
                client.level.getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT)
                        .setFixedSeason(payload.fixedSeason());
            }
            // Notify player
            client.player.sendSystemMessage(Component.literal("Beta level fixed season was changed to "
                    + payload.fixedSeason()));
        }
    }
}
