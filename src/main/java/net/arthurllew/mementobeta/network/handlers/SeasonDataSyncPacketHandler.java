package net.arthurllew.mementobeta.network.handlers;

import net.arthurllew.mementobeta.attachments.data.BetaSeasonData;
import net.arthurllew.mementobeta.network.packet.SeasonDataSyncPacket;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.world.biome.BetaBiomeSeasonHolder;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SeasonDataSyncPacketHandler implements IPayloadHandler<SeasonDataSyncPacket> {
    /**
     * Handles {@link SeasonDataSyncPacket} on client.
     */
    @Override
    public void handle(SeasonDataSyncPacket payload, IPayloadContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.level != null) {
            // Update season data on client
            if (client.level.hasData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT)) {
                BetaSeasonData seasonData = client.level.getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);
                seasonData.setSeasonData(payload.season(), payload.isSeasonLocked(), payload.fixedSeason());
                BetaBiomeSeasonHolder.setSavedBetaSeasonInstance(seasonData);
            }
        }
    }
}
