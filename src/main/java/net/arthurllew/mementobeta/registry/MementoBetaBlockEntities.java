package net.arthurllew.mementobeta.registry;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.block.entity.MoltenBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public abstract class MementoBetaBlockEntities {
    /**
     * Deferred Register for block entities.
     */
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MementoBeta.MODID);

    /**
     * Molten blocks.
     */
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<MoltenBlockEntity>> MOLTEN_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("molten_block", () ->
                    BlockEntityType.Builder.of(MoltenBlockEntity::new,
                            MementoBetaBlocks.MOLTEN_BEDROCK.get(),
                            MementoBetaBlocks.MOLTEN_REINFORCED_DEEPSLATE.get()).build(null));
}
