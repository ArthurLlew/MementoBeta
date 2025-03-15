package net.arthurllew.mementobeta.block;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.block.BetaPortalBlock;
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
        return MementoBetaItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
