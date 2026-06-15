package net.arthurllew.mementobeta.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.attachments.data.BetaSeasonData;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.attachments.data.BetaTimeData;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Handlers for player related server-side events.
 */
@EventBusSubscriber(modid = MementoBeta.MODID)
public class PlayerListener {
    /**
     * Ticks player.
     *
     * @param event living entity tick event
     */
    @SubscribeEvent
    public static void onPlayerTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            player.getData(MementoBetaAttachments.BETA_PLAYER_ATTACHMENT).onTick(player);
        }
    }

    /**
     * Sync Beta dimension data with the player on login.
     */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncBetaBetaDimensionData(event.getEntity());
    }

    /**
     * Sync Beta dimension data with the player on dimension change.
     */
    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncBetaBetaDimensionData(event.getEntity());
    }

    /**
     * Sync Beta dimension data with the player on respawn.
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncBetaBetaDimensionData(event.getEntity());
    }

    /**
     * Sync Beta dimension data with provided player.
     */
    @SuppressWarnings("resource")
    private static void syncBetaBetaDimensionData(Player player) {
        // Player is server-side and is in correct dimension
        if (player instanceof ServerPlayer serverPlayer &&
                serverPlayer.level().dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
            // Synchronize Beta dimension time
            if (serverPlayer.level() instanceof ServerLevel level) {
                BetaTimeData timeData = level.getDataStorage().get(BetaTimeData.FACTORY, BetaTimeData.ID);
                if (timeData != null) {
                    timeData.syncTimeData(level, serverPlayer);
                }
                BetaSeasonData seasonData = level.getDataStorage().get(BetaSeasonData.FACTORY, BetaSeasonData.ID);
                if (seasonData != null) {
                    seasonData.syncSeasonData(level, serverPlayer);
                }
            }
        }
    }
}
