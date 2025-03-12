package net.arthurllew.mementobeta.capabilities;

import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
     * Custom dimension capability.
     */
    public static final Capability<BetaTimeCapability> BETA_TIME_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>(){});

    /**
     * Custom player capability.
     */
    public static final Capability<BetaPlayerCapability> BETA_PLAYER_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>(){});

    /**
     * Register capability classes.
     */
    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(BetaTimeCapability.class);
    }

    /**
     * Handlers for each capability registration events.
     */
    @Mod.EventBusSubscriber(modid = MementoBeta.MODID)
    public static class Registration {
        @SubscribeEvent
        public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof LivingEntity livingEntity) {
                if (livingEntity instanceof Player player) {
                    event.addCapability(new ResourceLocation(MementoBeta.MODID, "beta_player"),
                            new CapabilityProvider(BETA_PLAYER_CAPABILITY, new BetaPlayerCapability(player)));
                }
            }
        }

        /**
         * Attach dimension time capability to level on init.
         */
        @SubscribeEvent
        public static void attachWorldCapabilities(AttachCapabilitiesEvent<Level> event) {
            if (event.getObject().dimensionTypeId().location().getPath().equals("betaworld")) {
                event.addCapability(new ResourceLocation(MementoBeta.MODID, "betaworld_time"),
                        new CapabilityProvider(BETA_TIME_CAPABILITY, new BetaTimeCapability(event.getObject())));
            }
        }
    }
}
