package net.arthurllew.mementobeta.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * This mixin grants access to the "properties" field in {@link Level}.
 */
@Mixin(Level.class)
public interface LevelAccessor {
    /**
     * Getter.
     * @return world properties.
     */
    @Accessor("levelData")
    WritableLevelData getWorldProperties();

    /**
     * Setter.
     * @param levelData world properties.
     */
    @Mutable
    @Accessor("levelData")
    void setWorldProperties(WritableLevelData levelData);
}
