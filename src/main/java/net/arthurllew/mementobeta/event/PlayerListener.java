package net.arthurllew.mementobeta.event;

import net.arthurllew.mementobeta.MementoBeta;
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
     * Sync dimension time with the player on login.
     *
     * @param event player login event
     */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncBetaBetaDimensionTime(event.getEntity());
    }

    /**
     * Sync dimension time with the player on dimension change.
     *
     * @param event dimension change event
     */
    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncBetaBetaDimensionTime(event.getEntity());
    }

    /**
     * Sync dimension time with the player on respawn.
     *
     * @param event player respawn event
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncBetaBetaDimensionTime(event.getEntity());
    }

    /**
     * Sync dimension time data with the player.
     *
     * @param player player
     */
    @SuppressWarnings("resource")
    private static void syncBetaBetaDimensionTime(Player player) {
        // Player is server-side and he is in correct dimension
        if (player instanceof ServerPlayer serverPlayer &&
                serverPlayer.level().dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
            // Synchronize dimension time data
            if (serverPlayer.level() instanceof ServerLevel level) {
                BetaTimeData timeData = level.getDataStorage().get(BetaTimeData.FACTORY, "betaworld_time");
                if (timeData != null) {
                    timeData.syncTimeData(level, serverPlayer);
                }
            }
        }
    }
}
