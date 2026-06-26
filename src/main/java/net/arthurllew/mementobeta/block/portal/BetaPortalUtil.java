package net.arthurllew.mementobeta.block.portal;

import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;

public class BetaPortalUtil {
    /**
     * Handles portal creation by player placing a block.
     *
     * @return whether a portal was created
     */
    public static boolean createPortal(BetaPortalBlock portalBlock,
                                       Player player, Level level, BlockPos pos,
                                       @Nullable Direction direction,
                                       ItemStack stack, InteractionHand hand) {
        // Direction should exist
        if (direction != null) {
            // Get relative position
            BlockPos relativePos = pos.relative(direction);
            // Block item matches activation condition
            if (stack.is(MementoBetaBlocks.BETA_FIRE.get().asItem())) {
                // Level dimension matches
                if ((level.dimension() == portalBlock.getHomeDimension()
                        || level.dimension() == portalBlock.getDestinationDimension())) {
                    // Find any existing portal frame
                    Optional<BetaPortalShape> optional =
                            BetaPortalShape.findEmptyBetaPortalShape(portalBlock, level, relativePos, Direction.Axis.X);
                    // If frame is present
                    if (optional.isPresent()) {
                        // Create portal blocks
                        optional.get().createPortalBlocks();

                        // Play activation sound
                        RandomSource randomSource = level.getRandom();
                        level.playLocalSound(pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                                2.6F + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8F,
                                false);

                        // Swing player's hand
                        player.swing(hand);

                        // Decrement item stack
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }

                        // Notify about success
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Handles portal creation on neighbor block updates.
     *
     * @return whether a portal was created
     */
    public static boolean detectInFrame(BetaPortalBlock portalBlock,
                                        LevelAccessor levelAccessor,
                                        BlockPos pos, BlockState blockState) {
        // If level is a valid level instance
        if (levelAccessor instanceof Level level) {
            // Block matches activation condition
            if (blockState.getBlock() == MementoBetaBlocks.BETA_FIRE.get()) {
                // Level dimension matches
                if ((level.dimension() == portalBlock.getHomeDimension()
                        || level.dimension() == portalBlock.getDestinationDimension())) {
                    // Find any existing portal frame
                    Optional<BetaPortalShape> optional =
                            BetaPortalShape.findEmptyBetaPortalShape(portalBlock, level, pos, Direction.Axis.X);
                    // If frame is present
                    if (optional.isPresent()) {
                        // Create portal
                        optional.get().createPortalBlocks();

                        // Play activation sound
                        RandomSource randomSource = level.getRandom();
                        level.playLocalSound(pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                                2.6F + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8F,
                                false);

                        // Notify about success
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
