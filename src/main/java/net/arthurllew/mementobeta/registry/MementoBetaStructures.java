package net.arthurllew.mementobeta.registry;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.world.structure.HeightCheckingPoolElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MementoBetaStructures {
    /**
     * Deferred Register structure pool elements.
     */
    public static final DeferredRegister<StructurePoolElementType<?>> POOL_ELEMENT_TYPES =
            DeferredRegister.create(BuiltInRegistries.STRUCTURE_POOL_ELEMENT, MementoBeta.MODID);

    /**
     * Height checking structure pool element.
     */
    public static final DeferredHolder<StructurePoolElementType<?>,
            StructurePoolElementType<HeightCheckingPoolElement>> HEIGHT_CHECKING_TYPE =
                    POOL_ELEMENT_TYPES.register("water_checking_single_pool_element",
                            () -> () -> HeightCheckingPoolElement.CODEC);
}
