package net.arthurllew.mementobeta.portal;

import net.arthurllew.mementobeta.world.BetaDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;

public class BetaPortalUtil {
    /**
     * Destination dimension.
     */
    public static ResourceKey<Level> destinationDimension =
            ResourceKey.create(Registries.DIMENSION, BetaDimension.BETA_DIMENSION_LEVEL.location());

    /**
     * Home dimension.
     */
    public static ResourceKey<Level> returnDimension =
            ResourceKey.create(Registries.DIMENSION, Level.OVERWORLD.location());

    /**
     * Handles portal creation by player placing block.
     * @return whether a portal was created.
     */
    public static boolean createPortal(Player player, Level level, BlockPos pos, @Nullable Direction direction,
                                       ItemStack stack, InteractionHand hand) {
        // Direction should exist
        if (direction != null) {
            // Get relative position
            BlockPos relativePos = pos.relative(direction);
            // Block item matches activation condition
            if (stack.is(Items.OBSIDIAN)) {
                // Travel dimension match
                if ((level.dimension() == returnDimension || level.dimension() == destinationDimension)) {
                    // Find any existing portal frame
                    Optional<BetaPortalShape> optional =
                            BetaPortalShape.findEmptyBetaPortalShape(level, relativePos, Direction.Axis.X);
                    // If frame is present
                    if (optional.isPresent()) {
                        // Create portal blocks
                        optional.get().createPortalBlocks();

                        // Play activation sound and swing hand
                        player.playSound(SoundEvents.LAVA_EXTINGUISH, 1.0F, 1.0F);
                        player.swing(hand);

                        // Decrement item stack
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Handles portal creation on neighbor block updates.
     * @return whether a portal was created.
     */
    public static boolean detectInFrame(LevelAccessor levelAccessor, BlockPos pos, BlockState blockState) {
        // If level is a valid level instance
        if (levelAccessor instanceof Level level) {
            // Block matches activation condition
            if (blockState.getBlock() == Blocks.OBSIDIAN) {
                // Travel dimension match
                if ((level.dimension() == returnDimension || level.dimension() == destinationDimension)) {
                    // Find any existing portal frame
                    Optional<BetaPortalShape> optional =
                            BetaPortalShape.findEmptyBetaPortalShape(level, pos, Direction.Axis.X);
                    // If frame is present
                    if (optional.isPresent()) {
                        // Create portal
                        optional.get().createPortalBlocks();
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
