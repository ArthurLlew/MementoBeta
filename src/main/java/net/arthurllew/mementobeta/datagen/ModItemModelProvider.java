package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.arthurllew.mementobeta.registry.MementoBetaItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MementoBeta.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Simple item models
        simpleItem(MementoBetaItems.LAVA_CREAM.getId().getPath());
        simpleItem(MementoBetaItems.MOLTEN_MANTLE.getId().getPath());
        simpleItem(MementoBetaItems.BETA_LAVA_BUCKET.getId().getPath());

        // Simple block item models
        simpleBlockItem(MementoBetaBlocks.BTA_BUSH_SAPLING.getId().getPath());
    }

    private void simpleItem(String itemId) {
        withExistingParent(itemId, ResourceLocation.withDefaultNamespace("item/generated"))
                        .texture("layer0",
                                ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "item/" + itemId));
    }

    private void simpleBlockItem(String itemId) {
        withExistingParent(itemId, ResourceLocation.withDefaultNamespace("item/generated"))
                .texture("layer0",
                        ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "block/" + itemId));
    }
}
