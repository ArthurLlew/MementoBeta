package net.arthurllew.mementobeta.block;

import net.arthurllew.mementobeta.mixin.FireBlockInvoker;
import net.minecraft.world.level.block.Blocks;

public class FireBlockBootstrap {
    /**
     * Adds blocks to flammable list allowing fire to spread.
     */
    public static void bootStrap() {
        FireBlockInvoker fireBlock = (FireBlockInvoker) MementoBetaBlocks.BETA_FIRE.get();
        fireBlock.invokeSetFlammable(Blocks.OAK_PLANKS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.SPRUCE_PLANKS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.BIRCH_PLANKS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.JUNGLE_PLANKS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.ACACIA_PLANKS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.CHERRY_PLANKS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.DARK_OAK_PLANKS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.MANGROVE_PLANKS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.BAMBOO_PLANKS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.BAMBOO_MOSAIC, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.OAK_SLAB, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.SPRUCE_SLAB, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.BIRCH_SLAB, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.JUNGLE_SLAB, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.ACACIA_SLAB, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.CHERRY_SLAB, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.DARK_OAK_SLAB, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.MANGROVE_SLAB, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.BAMBOO_SLAB, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.BAMBOO_MOSAIC_SLAB, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.OAK_FENCE_GATE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.SPRUCE_FENCE_GATE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.BIRCH_FENCE_GATE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.JUNGLE_FENCE_GATE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.ACACIA_FENCE_GATE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.CHERRY_FENCE_GATE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.DARK_OAK_FENCE_GATE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.MANGROVE_FENCE_GATE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.BAMBOO_FENCE_GATE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.OAK_FENCE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.SPRUCE_FENCE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.BIRCH_FENCE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.JUNGLE_FENCE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.ACACIA_FENCE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.CHERRY_FENCE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.DARK_OAK_FENCE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.MANGROVE_FENCE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.BAMBOO_FENCE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.OAK_STAIRS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.BIRCH_STAIRS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.SPRUCE_STAIRS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.JUNGLE_STAIRS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.ACACIA_STAIRS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.CHERRY_STAIRS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.DARK_OAK_STAIRS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.MANGROVE_STAIRS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.BAMBOO_STAIRS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.BAMBOO_MOSAIC_STAIRS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.OAK_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.SPRUCE_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.BIRCH_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.JUNGLE_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.ACACIA_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.CHERRY_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.DARK_OAK_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.MANGROVE_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.BAMBOO_BLOCK, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_OAK_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_SPRUCE_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_BIRCH_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_JUNGLE_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_ACACIA_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_CHERRY_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_DARK_OAK_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_MANGROVE_LOG, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_BAMBOO_BLOCK, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_OAK_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_SPRUCE_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_BIRCH_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_JUNGLE_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_ACACIA_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_CHERRY_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_DARK_OAK_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.STRIPPED_MANGROVE_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.OAK_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.SPRUCE_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.BIRCH_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.JUNGLE_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.ACACIA_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.CHERRY_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.DARK_OAK_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.MANGROVE_WOOD, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.MANGROVE_ROOTS, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.OAK_LEAVES, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.SPRUCE_LEAVES, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.BIRCH_LEAVES, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.JUNGLE_LEAVES, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.ACACIA_LEAVES, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.CHERRY_LEAVES, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.DARK_OAK_LEAVES, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.MANGROVE_LEAVES, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.BOOKSHELF, 30, 20);
        fireBlock.invokeSetFlammable(Blocks.TNT, 15, 100);
        fireBlock.invokeSetFlammable(Blocks.GRASS, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.FERN, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.DEAD_BUSH, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.SUNFLOWER, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.LILAC, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.ROSE_BUSH, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.PEONY, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.TALL_GRASS, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.LARGE_FERN, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.DANDELION, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.POPPY, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.BLUE_ORCHID, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.ALLIUM, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.AZURE_BLUET, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.RED_TULIP, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.ORANGE_TULIP, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.WHITE_TULIP, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.PINK_TULIP, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.OXEYE_DAISY, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.CORNFLOWER, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.LILY_OF_THE_VALLEY, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.TORCHFLOWER, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.PITCHER_PLANT, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.WITHER_ROSE, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.PINK_PETALS, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.WHITE_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.ORANGE_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.MAGENTA_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.LIGHT_BLUE_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.YELLOW_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.LIME_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.PINK_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.GRAY_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.LIGHT_GRAY_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.CYAN_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.PURPLE_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.BLUE_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.BROWN_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.GREEN_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.RED_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.BLACK_WOOL, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.VINE, 15, 100);
        fireBlock.invokeSetFlammable(Blocks.COAL_BLOCK, 5, 5);
        fireBlock.invokeSetFlammable(Blocks.HAY_BLOCK, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.TARGET, 15, 20);
        fireBlock.invokeSetFlammable(Blocks.WHITE_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.ORANGE_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.MAGENTA_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.LIGHT_BLUE_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.YELLOW_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.LIME_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.PINK_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.GRAY_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.LIGHT_GRAY_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.CYAN_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.PURPLE_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.BLUE_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.BROWN_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.GREEN_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.RED_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.BLACK_CARPET, 60, 20);
        fireBlock.invokeSetFlammable(Blocks.DRIED_KELP_BLOCK, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.BAMBOO, 60, 60);
        fireBlock.invokeSetFlammable(Blocks.SCAFFOLDING, 60, 60);
        fireBlock.invokeSetFlammable(Blocks.LECTERN, 30, 20);
        fireBlock.invokeSetFlammable(Blocks.COMPOSTER, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.SWEET_BERRY_BUSH, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.BEEHIVE, 5, 20);
        fireBlock.invokeSetFlammable(Blocks.BEE_NEST, 30, 20);
        fireBlock.invokeSetFlammable(Blocks.AZALEA_LEAVES, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.FLOWERING_AZALEA_LEAVES, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.CAVE_VINES, 15, 60);
        fireBlock.invokeSetFlammable(Blocks.CAVE_VINES_PLANT, 15, 60);
        fireBlock.invokeSetFlammable(Blocks.SPORE_BLOSSOM, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.AZALEA, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.FLOWERING_AZALEA, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.BIG_DRIPLEAF, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.BIG_DRIPLEAF_STEM, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.SMALL_DRIPLEAF, 60, 100);
        fireBlock.invokeSetFlammable(Blocks.HANGING_ROOTS, 30, 60);
        fireBlock.invokeSetFlammable(Blocks.GLOW_LICHEN, 15, 100);
    }
}
