package net.arthurllew.mementobeta.network.handlers;

import net.arthurllew.mementobeta.network.packet.SeasonPacket;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SeasonPacketHandler implements IPayloadHandler<SeasonPacket> {
    /**
     * Handles {@link SeasonPacket} on client.
     */
    @Override
    public void handle(SeasonPacket payload, IPayloadContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            // Update fixed season on client
            if (client.level.hasData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT)) {
                client.level.getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT)
                        .setSeason(payload.season());
            }
            // Notify player
            client.player.sendSystemMessage(Component.literal("Beta level season was changed to "
                    + payload.season()));
        }
    }
}
