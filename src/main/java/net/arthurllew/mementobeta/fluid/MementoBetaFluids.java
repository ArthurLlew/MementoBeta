package net.arthurllew.mementobeta.fluid;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public abstract class MementoBetaFluids {
    /**
     * Deferred Register for fluids.
     */
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, MementoBeta.MODID);

    /**
     * Still Beta 1.7.3 lava (fluid source).
     */
    public static final RegistryObject<FlowingFluid> BETA_lAVA_STILL = FLUIDS.register("beta_lava",
            () -> new BetaLavaProperties.Source());
    /**
     * Flowing Beta 1.7.3 lava.
     */
    public static final RegistryObject<FlowingFluid> BETA_lAVA_FLOWING = FLUIDS.register("beta_lava_flow",
            () -> new BetaLavaProperties.Flowing());

    /**
     * Beta 1.7.3 lava block.
     */
    public static final RegistryObject<LiquidBlock> BETA_lAVA_BLOCK =
            MementoBetaBlocks.BLOCKS.register("beta_lava",
                    () -> new LiquidBlock(() -> BETA_lAVA_STILL.get(), BlockBehaviour.Properties.copy(Blocks.LAVA)));
}
