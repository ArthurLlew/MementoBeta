package net.arthurllew.mementobeta.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    /**
     * Getter.
     * @return portal entrance pos.
     */
    @Accessor("portalEntrancePos")
    BlockPos getPortalEntrancePos();

    /**
     * Setter.
     * @param portalEntrancePos portal entrance pos.
     */
    @Accessor("portalEntrancePos")
    void setPortalEntrancePos(BlockPos portalEntrancePos);
}
