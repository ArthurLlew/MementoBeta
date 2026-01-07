package net.arthurllew.mementobeta.fluid;

import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.arthurllew.mementobeta.registry.MementoBetaFluidTypes;
import net.arthurllew.mementobeta.registry.MementoBetaFluids;
import net.arthurllew.mementobeta.registry.MementoBetaItems;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Beta lava properties.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class BetaLavaFluid extends LavaFluid {
    /**
     * @return beta lava source.
     */
    @Override
    public Fluid getSource() {
        return MementoBetaFluids.BETA_lAVA_STILL.get();
    }

    /**
     * @return beta lava flow.
     */
    @Override
    public Fluid getFlowing() {
        return MementoBetaFluids.BETA_lAVA_FLOWING.get();
    }

    /**
     * @return beta lava bucket.
     */
    @Override
    public Item getBucket() {
        return MementoBetaItems.BETA_LAVA_BUCKET.get();
    }

    /**
     * @return whether fluid is of beta lava.
     */
    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == MementoBetaFluids.BETA_lAVA_STILL.get()
                || fluid == MementoBetaFluids.BETA_lAVA_FLOWING.get();
    }

    /**
     * @return beta lava block.
     */
    @Override
    public BlockState createLegacyBlock(FluidState pState) {
        return MementoBetaBlocks.BETA_lAVA.get().defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(pState));
    }

    /**
     * @return beta lava fluid type.
     */
    @Override
    public FluidType getFluidType() {
        return MementoBetaFluidTypes.BETA_LAVA_TYPE.get();
    }

    /**
     * Copied from {@link LavaFluid}.
     */
    public static class Flowing extends BetaLavaFluid {
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> pBuilder) {
            super.createFluidStateDefinition(pBuilder);
            pBuilder.add(LEVEL);
        }

        public int getAmount(FluidState pState) {
            return pState.getValue(LEVEL);
        }

        public boolean isSource(FluidState pState) {
            return false;
        }
    }

    /**
     * Copied from {@link LavaFluid}.
     */
    public static class Source extends BetaLavaFluid {
        public int getAmount(FluidState pState) {
            return 8;
        }

        public boolean isSource(FluidState pState) {
            return true;
        }
    }
}
