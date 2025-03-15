package net.arthurllew.mementobeta.item;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.arthurllew.mementobeta.fluid.MementoBetaFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public abstract class MementoBetaItems {
    /**
     * Deferred Register for items.
     */
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MementoBeta.MODID);
    /**
     * Deferred Register for creative tabs.
     */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MementoBeta.MODID);

    /**
     * Heated dragon's breath.
     */
    public static final RegistryObject<Item> HEATED_DRAGON_BREATH = ITEMS.register("heated_dragon_breath",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    /**
     * Molten mantle.
     */
    public static final RegistryObject<Item> MOLTEN_MANTLE = ITEMS.register("molten_mantle",
            () -> new MoltenMantle(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    /**
     * Resonance stone.
     */
    public static final RegistryObject<Item> RESONANCE_STONE = ITEMS.register("resonance_stone",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    /**
     * Resonance pickaxe.
     */
    public static final RegistryObject<Item> RESONANCE_PICKAXE = ITEMS.register("resonance_pickaxe",
            () -> new PickaxeItem(MementoBetaTiers.RESONANCE_TIER, 1, 1,
                    new Item.Properties().rarity(Rarity.EPIC)));

    /**
     * Beta lava bucket.
     */
    public static final RegistryObject<Item> BETA_LAVA_BUCKET = ITEMS.register("beta_lava_bucket",
            () -> new BucketItem(() -> MementoBetaFluids.BETA_lAVA_STILL.get(),
                    new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    /**
     * Memento Beta item group.
     */
    public static final RegistryObject<CreativeModeTab> BETA_DECO_ITEM_GROUP =
            CREATIVE_MODE_TABS.register("memento_beta", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemgroup." + MementoBeta.MODID + ".items"))
                    .icon(() -> RESONANCE_STONE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(HEATED_DRAGON_BREATH.get());
                        output.accept(MOLTEN_MANTLE.get());
                        output.accept(MementoBetaBlocks.MOLTEN_BEDROCK.get());
                        output.accept(MementoBetaBlocks.MOLTEN_REINFORCED_DEEPSLATE.get());
                        output.accept(RESONANCE_STONE.get());
                        output.accept(RESONANCE_PICKAXE.get());
                        output.accept(MementoBetaBlocks.REINFORCED_BEDROCK.get());
                        output.accept(MementoBetaBlocks.BETA_FIRE.get());
                        output.accept(BETA_LAVA_BUCKET.get());
                    }).build());
}
