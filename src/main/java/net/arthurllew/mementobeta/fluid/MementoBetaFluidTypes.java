package net.arthurllew.mementobeta.fluid;

import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public abstract class MementoBetaFluidTypes {
    /**
     * Deferred Register for fluid types.
     */
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MementoBeta.MODID);

    /**
     * Beta 1.7.3 lava fluid type.
     */
    public static final RegistryObject<FluidType> BETA_LAVA_TYPE =
            FLUID_TYPES.register("beta_lava",
                    () -> new LavaFluidType(new ResourceLocation(MementoBeta.MODID, "block/beta_lava"),
                            new ResourceLocation(MementoBeta.MODID, "block/beta_lava_flow"),
                            FluidType.Properties.create()
                                    .descriptionId("fluid.mementobeta.beta_lava").canSwim(false).canDrown(false)
                                    .pathType(BlockPathTypes.LAVA).adjacentPathType(null)
                                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                                    .lightLevel(15).density(3000).viscosity(6000).temperature(1300)));
}
