package net.arthurllew.mementobeta.block.entity;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MementoBetaBlockEntities {
    /**
     * Deferred Register for block entities.
     */
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MementoBeta.MODID);

    /**
     * Molten blocks.
     */
    public static final RegistryObject<BlockEntityType<MoltenBlockEntity>> MOLTEN_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("molten_block", () ->
                    BlockEntityType.Builder.of(MoltenBlockEntity::new,
                            MementoBetaBlocks.MOLTEN_BEDROCK.get(),
                            MementoBetaBlocks.MOLTEN_REINFORCED_DEEPSLATE.get()).build(null));
}
