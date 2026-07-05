package net.arthurllew.mementobeta.registry;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.item.MoltenMantle;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public abstract class MementoBetaItems {
    /**
     * Deferred Register for items.
     */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MementoBeta.MODID);
    /**
     * Deferred Register for creative tabs.
     */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MementoBeta.MODID);

    /**
     * Molten mantle.
     */
    public static final DeferredItem<Item> MOLTEN_MANTLE = ITEMS.register("molten_mantle",
            () -> new MoltenMantle(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    /**
     * Beta lava bucket.
     */
    public static final DeferredItem<Item> BETA_LAVA_BUCKET = ITEMS.register("beta_lava_bucket",
            () -> new BucketItem(MementoBetaFluids.BETA_lAVA_STILL.get(),
            new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    /**
     * Memento Beta item group.
     */
    public static final Supplier<CreativeModeTab> MEMENTO_BETA_ITEM_GROUP =
            CREATIVE_MODE_TABS.register("memento_beta", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemgroup." + MementoBeta.MODID + ".items"))
                    .icon(() -> new ItemStack(MementoBetaBlocks.BETA_lAVA.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(MOLTEN_MANTLE.get());
                        output.accept(MementoBetaBlocks.MOLTEN_BEDROCK.get());
                        output.accept(MementoBetaBlocks.MOLTEN_REINFORCED_DEEPSLATE.get());
                        output.accept(MementoBetaBlocks.REINFORCED_BEDROCK.get());
                        output.accept(MementoBetaBlocks.BETA_FIRE.get());
                        output.accept(MementoBetaBlocks.BETA_lAVA.get());
                        output.accept(BETA_LAVA_BUCKET.get());
                        output.accept(MementoBetaBlocks.PACKED_DIRT.get());
                        output.accept(MementoBetaBlocks.BTA_BUSH_LEAVES.get());
                        output.accept(MementoBetaBlocks.BTA_BUSH_SAPLING.get());
                    }).build());
}
