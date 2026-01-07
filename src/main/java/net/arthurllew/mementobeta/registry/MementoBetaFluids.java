package net.arthurllew.mementobeta.registry;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.fluid.BetaLavaFluid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public abstract class MementoBetaFluids {
    /**
     * Deferred Register for fluids.
     */
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, MementoBeta.MODID);

    /**
     * Still Beta 1.7.3 lava (fluid source).
     */
    public static final Supplier<FlowingFluid> BETA_lAVA_STILL = FLUIDS.register("beta_lava",
            BetaLavaFluid.Source::new);
    /**
     * Flowing Beta 1.7.3 lava.
     */
    public static final Supplier<FlowingFluid> BETA_lAVA_FLOWING = FLUIDS.register("beta_lava_flow",
            BetaLavaFluid.Flowing::new);
}
