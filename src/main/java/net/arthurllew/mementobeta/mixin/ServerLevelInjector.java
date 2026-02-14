package net.arthurllew.mementobeta.mixin;

import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.arthurllew.mementobeta.world.levelgen.util.BetaSeedHolder;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Modifies {@link ServerLevel} behaviour.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelInjector {
    /**
     * Injects code into {@link ServerLevel#getSeed}. Returns appropriate seed for Beta dimension.
     */
    @Inject(at = @At("RETURN"), method = "getSeed", cancellable = true)
    public void injectGetSeed(CallbackInfoReturnable<Long> cir) {
        // If this instance is related to Beta dimension
        if (((ServerLevel)(Object)this).dimensionTypeRegistration()
                .is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
            // Try to return beta seed
            if (BetaSeedHolder.getSavedBetaSeedInstance() != null) {
                cir.setReturnValue(BetaSeedHolder.getSavedBetaSeedInstance().getBetaSeed());
            }
        }
    }
}
