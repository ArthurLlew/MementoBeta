package net.arthurllew.mementobeta.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.world.levelgen.placement.MementoBetaPlacements;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = MementoBeta.MODID)
public class PlacementModifierTypesRegister {
    /**
     * Placement modifier types registering.
     * @param event common registering event.
     */
    @SubscribeEvent
    public static void registerPlacementModifierTypes(RegisterEvent event) {
        if (event.getRegistry().equals(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE)) {
            // Bootstrap custom placement modifiers
            MementoBetaPlacements.bootstrap();
        }
    }
}
