package net.arthurllew.mementobeta.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.world.biome.BetaBiomeSeasons;
import net.arthurllew.mementobeta.world.biome.BiomeInjectorInterface;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

/**
 * Handlers for registry related events.
 */
@EventBusSubscriber(modid = MementoBeta.MODID)
public class RegistryListener {
    /**
     * Allows season effects in Beta dimension biomes.
     */
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        // Get biome registry
        Registry<Biome> biomeRegistry = event.getRegistryAccess().registryOrThrow(Registries.BIOME);

        // Iterate over all biomes
        biomeRegistry.holders().forEach(holder -> {
            // Check tag and allow seasons
            if (holder.is(BetaBiomeSeasons.BIOMES_WITH_SEASONS_TAG)) {
                //noinspection ConstantValue
                if ((Object) holder.value() instanceof BiomeInjectorInterface biomeInjector) {
                    biomeInjector.allowSeasons();
                }
            }
        });
    }
}
