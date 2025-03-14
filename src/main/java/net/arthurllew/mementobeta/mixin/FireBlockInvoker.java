package net.arthurllew.mementobeta.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * This mixin grants access to the "setFlammable" method in {@link FireBlock}.
 */
@Mixin(FireBlock.class)
public interface FireBlockInvoker {
    /**
     * Invoker.
     */
    @Invoker("setFlammable")
    void invokeSetFlammable(Block block, int encouragement, int flammability);
}
