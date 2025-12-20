package net.arthurllew.mementobeta.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.world.levelgen.BetaChunkGeneratorSettings;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

/**
 * Responsible for registering custom data resources.
 */
@EventBusSubscriber(modid = MementoBeta.MODID)
public class DataPackRegister {
    /**
     * Beta generator custom settings. Namespace in resource location actually defines nested directory
     * inside mod data directory.
     */
    public static final ResourceKey<Registry<BetaChunkGeneratorSettings>> BETA_SETTINGS =
            ResourceKey.createRegistryKey(ResourceLocation
                    .fromNamespaceAndPath("custom_data", "worldgen/settings"));

    /**
     * Registers custom data pack registry.
     */
    @SubscribeEvent
    public static void onNewDataRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(BETA_SETTINGS, BetaChunkGeneratorSettings.DIRECT_CODEC);
    }
}
