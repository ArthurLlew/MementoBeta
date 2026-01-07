package net.arthurllew.mementobeta.client.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.attachments.data.BetaTimeData;
import net.arthurllew.mementobeta.mixin.LevelAccessor;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Handlers for dimension related client events.
 */
@EventBusSubscriber(modid = MementoBeta.MODID, value = Dist.CLIENT)
public class DimensionListenerClient {
    /**
     * Additional actions performed every client tick.
     * @param event client tick event.
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;

        // Client is not paused, level exists and belongs to correct dimension
        if (level != null && !Minecraft.getInstance().isPaused()
                && level.dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
            // Get access to level data
            LevelAccessor levelAccessor = (LevelAccessor) level;

            // Tick day time according to game rules
            if (levelAccessor.getWorldProperties().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                // Get or create beta level time data
                BetaTimeData timeData = level.getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT);
                // Even if server time is not ticking, client always increments time by 1 every tick.
                level.setDayTime(timeData.tickTime(level) - 1);
            }
        }
    }
}
