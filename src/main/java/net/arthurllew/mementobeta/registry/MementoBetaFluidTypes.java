package net.arthurllew.mementobeta.registry;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.fluid.BetaLavaFluidType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public abstract class MementoBetaFluidTypes {
    /**
     * Deferred Register for fluid types.
     */
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MementoBeta.MODID);

    /**
     * Beta 1.7.3 lava fluid type.
     */
    public static final Supplier<FluidType> BETA_LAVA_TYPE =
            FLUID_TYPES.register("beta_lava",
                    () -> new BetaLavaFluidType(
                            ResourceLocation
                                    .fromNamespaceAndPath(MementoBeta.MODID, "block/beta_lava"),
                            ResourceLocation
                                    .fromNamespaceAndPath(MementoBeta.MODID, "block/beta_lava_flow"),
                            FluidType.Properties.create()
                                    .descriptionId("fluid.mementobeta.beta_lava").canSwim(false).canDrown(false)
                                    .pathType(PathType.LAVA).adjacentPathType(null)
                                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                                    .lightLevel(15).density(3000).viscosity(6000).temperature(1300)));
}
