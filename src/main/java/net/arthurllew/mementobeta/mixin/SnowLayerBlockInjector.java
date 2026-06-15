package net.arthurllew.mementobeta.mixin;

import net.arthurllew.mementobeta.attachments.data.BetaSeasonData;
import net.arthurllew.mementobeta.world.biome.BetaBiomeSeasons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowLayerBlock.class)
public class SnowLayerBlockInjector {
    /**
     * Injects code into {@link SnowLayerBlock}. Allows melting under sun in Beta dimension.
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
                    && (level.getBrightness(LightLayer.SKY, pos) > 11)) {
                // Melt
                if (level.random.nextInt(BetaBiomeSeasons.mapSeasonMelting(seasonData.getSeason())) == 0) {
                    level.removeBlock(pos, false);
                }
            }
        }
    }
}
