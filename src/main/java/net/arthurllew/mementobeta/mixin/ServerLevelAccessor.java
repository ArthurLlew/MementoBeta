package net.arthurllew.mementobeta.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * This mixin grants access to the "serverLevelData" field in {@link ServerLevel}.
 */
@Mixin(ServerLevel.class)
public interface ServerLevelAccessor {
    /**
     * Getter.
     * @return server world properties.
     */
    @Accessor("serverLevelData")
    ServerLevelData getServerWorldProperties();

    /**
     * Setter.
     * @param serverLevelData server world properties.
     */
    @Mutable
    @Accessor("serverLevelData")
    void setServerWorldProperties(ServerLevelData serverLevelData);
}
