package net.arthurllew.mementobeta.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.world.levelgen.BetaChunkGeneratorSettings;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DataPackRegistryEvent;

/**
 * Responsible for registering custom data resources.
 */
@Mod.EventBusSubscriber(modid = MementoBeta.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomDataRegister {
    /**
     * Beta generator custom settings. Namespace in resource location actually defines nested directory
     * inside mod data directory.
     */
    public static final ResourceKey<Registry<BetaChunkGeneratorSettings>> BETA_SETTINGS =
            ResourceKey.createRegistryKey(new ResourceLocation("custom_data", "worldgen/settings"));

    /**
     * Registers custom data pack registry.
     */
    @SubscribeEvent
    public static void onNewDataRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(BETA_SETTINGS, BetaChunkGeneratorSettings.DIRECT_CODEC);
    }
}
