package net.arthurllew.mementobeta.mixin;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.arthurllew.mementobeta.fluid.MementoBetaFluidTypes;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Modifies {@link Entity} behaviour.
 */
@Mixin(Entity.class)
public abstract class EntityInjector {
    /**
     * Texture atlas field.
     */
    @Shadow
    protected boolean firstTick;

    /**
     * Stitch preparations field.
     */
    @Shadow
    protected Object2DoubleMap<FluidType> forgeFluidTypeHeight;

    /**
     * Injects code into {@link Entity#isInLava()}.
     */
    @Inject(at = @At("RETURN"), method = "isInLava", cancellable = true)
    public void injectGetBlastResistance(CallbackInfoReturnable<Boolean> cir) {
        // Take beta lava into account
        cir.setReturnValue(cir.getReturnValue() || (!this.firstTick
                && this.forgeFluidTypeHeight.getDouble(MementoBetaFluidTypes.BETA_LAVA_TYPE.get()) > 0.0D));
    }
}
