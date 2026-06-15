package net.arthurllew.mementobeta.network.handlers;

import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.network.packet.TimeLockPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TimeLockPacketHandler implements IPayloadHandler<TimeLockPacket> {
    /**
     * Handles {@link net.arthurllew.mementobeta.network.packet.TimeLockPacket} on client.
     */
    @Override
    public void handle(TimeLockPacket payload, IPayloadContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            // Update time lock on client
            if (client.level.hasData(MementoBetaAttachments.BETA_TIME_ATTACHMENT)) {
                client.level.getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT)
                        .setTimeLock(payload.isTimeLocked());
            }
            // Notify player
            client.player.sendSystemMessage(Component.literal("Beta level time lock is now "
                    + (payload.isTimeLocked() ? "on" : "off")));
        }
    }
}
