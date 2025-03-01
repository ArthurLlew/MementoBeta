package net.arthurllew.mementobeta.capabilities;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.capabilities.world.BetaDimensionTime;
import net.arthurllew.mementobeta.capabilities.world.DimensionTime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers custom components linked to minecraft classes.
 */
@Mod.EventBusSubscriber(modid = MementoBeta.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MementoBetaCapabilities {
    /**
     * Custom time capability.
     */
    public static final Capability<DimensionTime> BETA_TIME_COMPONENT =
            CapabilityManager.get(new CapabilityToken<>(){});

    /**
     * Register capability classes.
     */
    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(DimensionTime.class);
    }

    /**
     * Handlers for each capability type related event.
     */
    @Mod.EventBusSubscriber(modid = MementoBeta.MODID)
    public static class Registration {
        /**
         * Attach dimension time capability to level on init.
         */
        @SubscribeEvent
        public static void attachWorldCapabilities(AttachCapabilitiesEvent<Level> event) {
            if (event.getObject().dimensionTypeId().location().getPath().equals("betaworld")) {
                event.addCapability(new ResourceLocation(MementoBeta.MODID, "betaworld_time"),
                        new CapabilityProvider(BETA_TIME_COMPONENT, new BetaDimensionTime(event.getObject())));
            }
        }
    }
}
