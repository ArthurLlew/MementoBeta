package net.arthurllew.mementobeta.block;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.fluid.MementoBetaFluids;
import net.arthurllew.mementobeta.item.BlockItemWithTooltip;
import net.arthurllew.mementobeta.item.MementoBetaItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public abstract class MementoBetaBlocks {
    /**
     * Deferred Register for blocks.
     */
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MementoBeta.MODID);

    /**
     * Molten bedrock.
     */
    public static final RegistryObject<Block> MOLTEN_BEDROCK = registerBlock(
            "molten_bedrock",
            () -> new MoltenBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .requiresCorrectToolForDrops().strength(60.0F), Blocks.BEDROCK));
    /**
     * Molten reinforced deepslate.
     */
    public static final RegistryObject<Block> MOLTEN_REINFORCED_DEEPSLATE = registerBlock(
            "molten_reinforced_deepslate",
            () -> new MoltenBlock(BlockBehaviour.Properties.copy(Blocks.REINFORCED_DEEPSLATE)
                    .requiresCorrectToolForDrops().strength(60.0F), Blocks.REINFORCED_DEEPSLATE));

    /**
     * Reinforced bedrock.
     */
    public static final RegistryObject<RotatedPillarBlock> REINFORCED_BEDROCK = registerBlockWithTooltip(
            "reinforced_bedrock",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .requiresCorrectToolForDrops().strength(60.0F)),
            "tooltip." + MementoBeta.MODID + ".reinforced_bedrock");
    /**
     * Beta fire block.
     */
    public static final RegistryObject<FireBlock> BETA_FIRE = registerBlockWithTooltip("beta_fire",
            () -> new FireBlock(BlockBehaviour.Properties.copy(Blocks.FIRE)),
            "tooltip." + MementoBeta.MODID + ".beta_fire");

    /**
     * Beta portal block.
     */
    public static final RegistryObject<Block> BETA_PORTAL = registerBlock("beta_portal",
            () -> new BetaPortalBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_PORTAL)));

    /**
     * Beta 1.7.3 lava block.
     */
    public static final RegistryObject<LiquidBlock> BETA_lAVA = registerBlockWithTooltip("beta_lava",
            () -> new LiquidBlock(() -> MementoBetaFluids.BETA_lAVA_STILL.get(),
                            BlockBehaviour.Properties.copy(Blocks.LAVA)),
            "tooltip." + MementoBeta.MODID + ".beta_lava");

    /**
     * Registers block and its item.
     * @param name block id.
     * @param block block supplier.
     * @return registered block.
     * @param <T> block child.
     */
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> regBlock = BLOCKS.register(name, block);
        MementoBetaItems.ITEMS.register(name, () -> new BlockItem(regBlock.get(), new Item.Properties()));
        return regBlock;
    }

    /**
     * Registers block and its item with tooltip.
     * @param name block id.
     * @param block block supplier.
     * @param tooltipKey tooltip key.
     * @return registered block.
     * @param <T> block child.
     */
    private static <T extends Block> RegistryObject<T> registerBlockWithTooltip(String name, Supplier<T> block,
                                                                                String tooltipKey) {
        RegistryObject<T> regBlock = BLOCKS.register(name, block);
        MementoBetaItems.ITEMS.register(name,
                () -> new BlockItemWithTooltip(regBlock.get(), new Item.Properties(), tooltipKey));
        return regBlock;
    }
}
