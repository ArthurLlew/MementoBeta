package net.arthurllew.mementobeta.client.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.capabilities.world.DimensionTime;
import net.arthurllew.mementobeta.mixin.LevelAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handlers for dimension related client events.
 */
@Mod.EventBusSubscriber(modid = MementoBeta.MODID, value = Dist.CLIENT)
public class DimensionListenerClient {
    /**
     * Additional actions performed every client tick.
     * @param event client tick event.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        ClientLevel level = Minecraft.getInstance().level;

        // Client is not paused, level exists and belongs to correct dimension
        if (event.phase == TickEvent.Phase.START && !Minecraft.getInstance().isPaused() && level != null
                && level.dimensionTypeId().location().getPath().equals("betaworld")) {
            // Get access to level data
            LevelAccessor levelAccessor = (LevelAccessor) level;

            // Tick day time according to game rules
            if (levelAccessor.getWorldProperties().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                // Even if server time is not ticking, client always increments time by 1 every tick.
                DimensionTime.get(level).ifPresent(time -> level.setDayTime(time.tickTime(level) - 1));
            }
        }
    }
}
