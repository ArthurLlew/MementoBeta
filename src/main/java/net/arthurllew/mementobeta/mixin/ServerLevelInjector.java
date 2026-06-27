package net.arthurllew.mementobeta.mixin;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.world.biome.BetaBiomeSeasons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ServerLevel.class)
public abstract class ServerLevelInjector {
    /**
     * Injects code into {@link ServerLevel#getSeed}. Returns appropriate seed for Beta dimension.
     */
    @Inject(at = @At("RETURN"), method = "getSeed", cancellable = true)
    public void injectGetSeed(CallbackInfoReturnable<Long> cir) {
        // Convert this class to its mixin target
        ServerLevel serverLevel = (ServerLevel) (Object) this;

        // If this instance is related to Beta dimension
        if (serverLevel.hasData(MementoBetaAttachments.BETA_SEED_ATTACHMENT)) {
            // Return beta dimension seed
            cir.setReturnValue(serverLevel.getData(MementoBetaAttachments.BETA_SEED_ATTACHMENT).getBetaSeed());
        }
    }

    /**
     * See {@link ServerLevel#tickPrecipitation}.
     */
    @Unique
    private void tickPrecipitation(ServerLevel level, BlockPos blockPos1, BlockPos blockPos2) {
        Biome biome = level.getBiome(blockPos1).value();
        if (level.isAreaLoaded(blockPos2, 1))
            if (biome.shouldFreeze(level, blockPos2)) {
                level.setBlockAndUpdate(blockPos2, Blocks.ICE.defaultBlockState());
            }

        if (level.isRaining()) {
            int i = level.getGameRules().getInt(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT);
            if (i > 0 && biome.shouldSnow(level, blockPos1)) {
                BlockState tempBlockState = level.getBlockState(blockPos1);
                if (tempBlockState.is(Blocks.SNOW)) {
                    int j = tempBlockState.getValue(SnowLayerBlock.LAYERS);
                    if (j < Math.min(i, 8)) {
                        BlockState tempBlockState2 = tempBlockState.setValue(SnowLayerBlock.LAYERS, j + 1);
                        Block.pushEntitiesUp(tempBlockState, tempBlockState2, level, blockPos1);
                        level.setBlockAndUpdate(blockPos1, tempBlockState2);
                    }
                } else {
                    level.setBlockAndUpdate(blockPos1, Blocks.SNOW.defaultBlockState());
                }
            }

            Biome.Precipitation biome$precipitation = biome.getPrecipitationAt(blockPos2);
            if (biome$precipitation != Biome.Precipitation.NONE) {
                BlockState tempBlockState = level.getBlockState(blockPos2);
                tempBlockState.getBlock().handlePrecipitation(tempBlockState, level, blockPos2, biome$precipitation);
            }
        }
    }

    /**
     * See {@link ServerLevel#tickPrecipitation}.
     */
    @Unique
    private void tickPrecipitation(ServerLevel level, Heightmap.Types heightmapType, BlockPos blockPos) {
        BlockPos blockPos1 = level.getHeightmapPos(heightmapType, blockPos);
        BlockPos blockPos2 = blockPos1.below();
        tickPrecipitation(level, blockPos1, blockPos2);
    }

    /**
     * Injects code into {@link ServerLevel#tickChunk}. Add additional seasons weather handling.
     */
    @Inject(method = "tickChunk", at = @At("TAIL"))
    private void injectTickChunk(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        // Convert this class to its mixin target
        ServerLevel serverLevel = (ServerLevel)(Object)this;

        // Level belongs to correct dimension
        if (serverLevel.hasData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT)) {
            // If season is winter and it is snowing
            if (BetaBiomeSeasons.isWinter(serverLevel.getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT).getSeason())
                    && serverLevel.isRaining()) {
                // For number of tries
                for (int i = 0; i < randomTickSpeed; i++) {
                    // Probability is 1/value
                    if (serverLevel.random.nextInt(10) == 0) {
                        // Random position inside chunk
                        ChunkPos chunkPos = chunk.getPos();
                        BlockPos pos = serverLevel
                                .getBlockRandomPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ(), 15);

                        // Beta biomes with seasons
                        TagKey<Biome> seasonable = TagKey.create(Registries.BIOME,
                                ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "seasonable"));
                        // If biome at positions is in tag
                        if (serverLevel.getBiome(pos).is(seasonable)) {
                            // Tick rain on two heightmaps
                            tickPrecipitation(serverLevel, Heightmap.Types.MOTION_BLOCKING, pos);
                            tickPrecipitation(serverLevel, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
                        }
                    }
                }
            }
        }
    }
}
