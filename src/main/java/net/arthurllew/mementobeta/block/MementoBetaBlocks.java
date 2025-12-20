package net.arthurllew.mementobeta.block;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.fluid.MementoBetaFluids;
import net.arthurllew.mementobeta.item.BlockItemWithTooltip;
import net.arthurllew.mementobeta.item.MementoBetaItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public abstract class MementoBetaBlocks {
    /**
     * Deferred Register for blocks.
     */
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MementoBeta.MODID);

    /**
     * Molten bedrock.
     */
    public static final DeferredBlock<Block> MOLTEN_BEDROCK = registerBlock(
            "molten_bedrock",
            () -> new MoltenBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)
                    .requiresCorrectToolForDrops().strength(60.0F), Blocks.BEDROCK));
    /**
     * Molten reinforced deepslate.
     */
    public static final DeferredBlock<Block> MOLTEN_REINFORCED_DEEPSLATE = registerBlock(
            "molten_reinforced_deepslate",
            () -> new MoltenBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.REINFORCED_DEEPSLATE)
                    .requiresCorrectToolForDrops().strength(60.0F), Blocks.REINFORCED_DEEPSLATE));

    /**
     * Reinforced bedrock.
     */
    public static final DeferredBlock<RotatedPillarBlock> REINFORCED_BEDROCK = registerBlockWithTooltip(
            "reinforced_bedrock",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)
                    .requiresCorrectToolForDrops().strength(60.0F)),
            "tooltip." + MementoBeta.MODID + ".reinforced_bedrock");
    /**
     * Beta fire block.
     */
    public static final DeferredBlock<FireBlock> BETA_FIRE = registerBlockWithTooltip("beta_fire",
            () -> new FireBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FIRE)),
            "tooltip." + MementoBeta.MODID + ".beta_fire");

    /**
     * Beta portal block.
     */
    public static final DeferredBlock<Block> BETA_PORTAL = registerBlock("beta_portal",
            () -> new BetaPortalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL)));

    /**
     * Beta 1.7.3 lava block.
     */
    public static final DeferredBlock<LiquidBlock> BETA_lAVA = registerBlockWithTooltip("beta_lava",
            () -> new LiquidBlock(MementoBetaFluids.BETA_lAVA_STILL.get(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA)),
            "tooltip." + MementoBeta.MODID + ".beta_lava");

    /**
     * Registers block and its item.
     * @param name block id.
     * @param block block supplier.
     * @return registered block.
     * @param <T> block child.
     */
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> regBlock = BLOCKS.register(name, block);
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
    private static <T extends Block> DeferredBlock<T> registerBlockWithTooltip(String name, Supplier<T> block,
                                                                                String tooltipKey) {
        DeferredBlock<T> regBlock = BLOCKS.register(name, block);
        MementoBetaItems.ITEMS.register(name,
                () -> new BlockItemWithTooltip(regBlock.get(), new Item.Properties(), tooltipKey));
        return regBlock;
    }
}
