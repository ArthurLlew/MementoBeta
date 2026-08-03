package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = MementoBeta.MODID)
public class DataGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        // Data generation context
        net.minecraft.data.DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Recipes provider
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));

        // Loot tables provider
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK)),
                lookupProvider));

        // Item models provider
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));

        // Block states provider
        generator.addProvider(event.includeServer(), new ModBlockStateProvider(packOutput, existingFileHelper));

        // Block tags provider
        generator.addProvider(event.includeServer(),
                new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper));
    }
}
