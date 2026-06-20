package net.arthurllew.mementobeta.network.handlers;

import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.network.packet.FixedTimePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FixedTimePacketHandler implements IPayloadHandler<FixedTimePacket> {
    /**
     * Handles {@link net.arthurllew.mementobeta.network.packet.FixedTimePacket} on client.
     */
    @Override
    public void handle(FixedTimePacket payload, IPayloadContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            // Update fixed time on client
            if (client.level.hasData(MementoBetaAttachments.BETA_TIME_ATTACHMENT)) {
                client.level.getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT)
                        .setFixedTime(payload.fixedTime());

                // Notify player
                client.player.sendSystemMessage(Component.literal("Beta level fixed time was changed to "
                        + payload.fixedTime()));
            }
        }
    }
}
