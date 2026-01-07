package net.arthurllew.mementobeta.mixin;

import net.arthurllew.mementobeta.registry.MementoBetaFluidTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Modifies {@link Entity} behaviour.
 */
@Mixin(Entity.class)
public abstract class EntityInjector implements IEntityExtension {
    @Shadow
    protected boolean firstTick;

    /**
     * Injects code into {@link Entity#isInLava}. Treats beta lava as Vanilla lava in {@link Entity} interactions.
     */
    @Inject(at = @At("RETURN"), method = "isInLava", cancellable = true)
    public void injectIsInLava(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() || (!firstTick
                && this.getFluidTypeHeight(MementoBetaFluidTypes.BETA_LAVA_TYPE.get()) > 0.0D));
    }

    /**
     * Inserts check to {@link Entity#getFluidTypeHeight} so
     * {@link net.minecraft.world.entity.LivingEntity#travel} will treat beta lava as Vanilla one.
     * This will cause similar entity movement because of the injection in {@link EntityInjector#injectIsInLava}.
     */
    public boolean isInFluidType(FluidType type)
    {
        return !(type == MementoBetaFluidTypes.BETA_LAVA_TYPE.get()) && this.getFluidTypeHeight(type) > 0.0D;
    }

    /**
     * Injects code into {@link Entity#getFluidHeight}, so entity speed calculation inside beta lava is correct.
     */
    @Inject(at = @At("RETURN"), method = "getFluidHeight", cancellable = true)
    public void injectGetFluidHeight(TagKey<Fluid> pFluidTag, CallbackInfoReturnable<Double> cir) {
        if (pFluidTag == FluidTags.LAVA && cir.getReturnValue() == 0.0D) {
            cir.setReturnValue(this.getFluidTypeHeight(MementoBetaFluidTypes.BETA_LAVA_TYPE.get()));
        }
    }
}
