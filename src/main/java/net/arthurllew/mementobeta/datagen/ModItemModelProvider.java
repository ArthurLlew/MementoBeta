package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.item.MementoBetaItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MementoBeta.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Simple (parents "generated") item model
        simpleItem(MementoBetaItems.HEATED_DRAGON_BREATH);
        simpleItem(MementoBetaItems.MOLTEN_MANTLE);
        simpleItem(MementoBetaItems.BETA_LAVA_BUCKET);
    }

    private ItemModelBuilder simpleItem(DeferredItem<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.withDefaultNamespace("item/generated"))
                        .texture("layer0",
                                ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID,
                                        "item/" + item.getId().getPath()));
    }
}
