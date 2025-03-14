package net.arthurllew.mementobeta;

import net.arthurllew.mementobeta.block.BetaPortalBlock;
import net.arthurllew.mementobeta.item.MementoBetaTiers;
import net.arthurllew.mementobeta.item.MoltenMantle;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public abstract class MementoBetaContent {
    /**
     * Deferred Register for blocks.
     */
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MementoBeta.MODID);
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
     * Molten bedrock.
     */
    public static final RegistryObject<Block> MOLTEN_BEDROCK = registerBlock(
            "molten_bedrock",
            () -> new MagmaBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .requiresCorrectToolForDrops().strength(60.0F)));
    /**
     * Molten reinforced deepslate.
     */
    public static final RegistryObject<Block> MOLTEN_REINFORCED_DEEPSLATE = registerBlock(
            "molten_reinforced_deepslate",
            () -> new MagmaBlock(BlockBehaviour.Properties.copy(Blocks.REINFORCED_DEEPSLATE)
                    .requiresCorrectToolForDrops().strength(60.0F)));

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
     * Reinforced bedrock.
     */
    public static final RegistryObject<RotatedPillarBlock> REINFORCED_BEDROCK = registerBlock("reinforced_bedrock",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .requiresCorrectToolForDrops().strength(60.0F)));
    /**
     * Beta fire block.
     */
    public static final RegistryObject<FireBlock> BETA_FIRE = registerBlock("beta_fire",
            () -> new FireBlock(BlockBehaviour.Properties.copy(Blocks.FIRE)));

    /**
     * Beta portal block.
     */
    public static final RegistryObject<Block> BETA_PORTAL = registerBlock("beta_portal",
            () -> new BetaPortalBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_PORTAL)));

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
                        output.accept(MOLTEN_BEDROCK.get());
                        output.accept(MOLTEN_REINFORCED_DEEPSLATE.get());
                        output.accept(RESONANCE_STONE.get());
                        output.accept(RESONANCE_PICKAXE.get());
                        output.accept(REINFORCED_BEDROCK.get());
                        output.accept(BETA_FIRE.get());
                    }).build());

    /**
     * Registers block and its item.
     * @param name block id.
     * @param block block supplier.
     * @return registered block.
     * @param <T> block child.
     */
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    /**
     * Registers block item.
     * @param name block id.
     * @param block block supplier.
     * @return registered block item.
     * @param <T> block child.
     */
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
