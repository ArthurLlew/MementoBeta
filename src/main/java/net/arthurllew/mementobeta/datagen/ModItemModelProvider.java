package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.item.MementoBetaItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MementoBeta.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Simple (parents "generated") item model
        simpleItem(MementoBetaItems.HEATED_DRAGON_BREATH);
        simpleItem(MementoBetaItems.MOLTEN_MANTLE);
        simpleItem(MementoBetaItems.RESONANCE_STONE);
        simpleItem(MementoBetaItems.BETA_LAVA_BUCKET);

        // Tool-like item model
        handheldItem(MementoBetaItems.RESONANCE_PICKAXE);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(MementoBeta.MODID,"item/" + item.getId().getPath()));
    }

    private ItemModelBuilder handheldItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/handheld")).texture("layer0",
                new ResourceLocation(MementoBeta.MODID,"item/" + item.getId().getPath()));
    }
}
