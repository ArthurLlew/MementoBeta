package net.arthurllew.mementobeta.mixin;

import net.arthurllew.mementobeta.attachments.BetaLevelSeasonAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.world.biome.BetaBiomeSeasons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
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
    @Inject(at = @At("HEAD"), method = "randomTick")
    public void injectRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
                                 CallbackInfo ci) {
        // Level belongs to correct dimension
        if (level.hasData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT)) {
            BetaLevelSeasonAttachment betLevelSeason = level
                    .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);
            // Not winter + melting condition
            if (BetaBiomeSeasons.notWinter(betLevelSeason.getSeason())
                    && (level.getBrightness(LightLayer.SKY, pos) > 11)) {
                // If biome permits
                if (!level.getBiome(pos).value().shouldSnow(level, pos)) {
                    // Melt
                    if (random.nextInt(BetaBiomeSeasons.mapSeasonMelting(betLevelSeason.getSeason())) == 0) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }
}
