package net.arthurllew.mementobeta.network.handlers;

import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.network.packet.TimeDataSyncPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TimeDataSyncPacketHandler implements IPayloadHandler<TimeDataSyncPacket> {
    /**
     * Handles {@link net.arthurllew.mementobeta.network.packet.TimeDataSyncPacket} on client.
     */
    @Override
    public void handle(TimeDataSyncPacket payload, IPayloadContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            // Update time data on client
            if (client.level.hasData(MementoBetaAttachments.BETA_TIME_ATTACHMENT)) {
                client.level.getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT)
                        .setTimeData(payload.isTimeLocked(), payload.fixedTime());
            }
        }
    }
}
