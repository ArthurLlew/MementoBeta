package net.arthurllew.mementobeta.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.capabilities.BetaPlayerCapability;
import net.arthurllew.mementobeta.capabilities.BetaTimeCapability;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handlers for player related server-side events.
 */
@Mod.EventBusSubscriber(modid = MementoBeta.MODID)
public class PlayerListener {
    /**
     * Tick player.
     * @param event living entity tick event.
     */
    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        // Tick player
        if (event.getEntity() instanceof Player player) {
            BetaPlayerCapability.get(player).ifPresent(BetaPlayerCapability::onTick);
        }
    }

    /**
     * Sync dimension time with the player on login.
     * @param event on player login event.
     */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncBetaBetaDimensionTime(event.getEntity());
    }

    /**
     * Sync dimension time with the player on dimension change.
     * @param event dimension change event.
     */
    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncBetaBetaDimensionTime(event.getEntity());
    }

    /**
     * Sync dimension time with the player on respawn.
     * @param event player respawn event.
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncBetaBetaDimensionTime(event.getEntity());
    }

    /**
     * Sync dimension time data with the player.
     * @param player player.
     */
    private static void syncBetaBetaDimensionTime(Player player) {
        // Player is server-side and he is in correct dimension
        if (player instanceof ServerPlayer serverPlayer &&
                serverPlayer.level().dimensionTypeId().location().getPath().equals("betaworld")) {
            // Synchronize dimension time data
            BetaTimeCapability.get(serverPlayer.level()).ifPresent((time) -> time.syncTimeData(serverPlayer));
        }
    }
}
