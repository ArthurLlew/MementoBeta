package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = MementoBeta.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        net.minecraft.data.DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Block models provider
        generator.addProvider(event.includeServer(), new ModBlockStateProvider(packOutput, existingFileHelper));

        // Item models provider
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));

        // Recipe provider
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput));

        // Loot tables provider
        generator.addProvider(event.includeServer(), ModLootTableProvider.create(packOutput));

        // Block tags provider
        generator.addProvider(event.includeServer(),
                new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper));
    }
}
