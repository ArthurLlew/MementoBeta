package net.arthurllew.mementobeta.mixin;

import net.arthurllew.mementobeta.attachments.data.BetaSeasonData;
import net.arthurllew.mementobeta.world.biome.BetaBiomeSeasons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IceBlock.class)
public abstract class IceBlockInjector {
    /**
     * Injects code into {@link IceBlock}. Allows melting under sun in Beta dimension.
     */
    @Inject(at = @At("RETURN"), method = "randomTick")
    public void injectRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
                                 CallbackInfo ci)
    {
        // Level has seasons
        BetaSeasonData seasonData = level.getDataStorage().get(BetaSeasonData.FACTORY, BetaSeasonData.ID);
        if (seasonData != null) {
            // Not winter + melting condition
            if (BetaBiomeSeasons.notWinter(seasonData.getSeason())
                    && (level.getBrightness(LightLayer.SKY, pos) > 11 - state.getLightBlock(level, pos))) {
                // Melt
                if (level.random.nextInt(BetaBiomeSeasons.mapSeasonMelting(seasonData.getSeason())) == 0) {
                    level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
                    level.neighborChanged(pos, Blocks.WATER.defaultBlockState().getBlock(), pos);
                }
            }
        }
    }
}
