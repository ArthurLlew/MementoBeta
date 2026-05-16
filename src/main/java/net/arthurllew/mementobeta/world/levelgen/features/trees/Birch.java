package net.arthurllew.mementobeta.world.levelgen.features.trees;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class Birch extends Feature<BirchConfig> {
    /**
     * Constructor matching super.
     */
    public Birch(Codec<BirchConfig> codec) {
        super(codec);
    }

    /**
     * @return whether position is suitable for tree placement
     */
    public boolean isFree(LevelSimulatedReader level, BlockPos pos) {
        return level.isStateAtPosition(pos, (blockState) -> blockState.is(BlockTags.LOGS)
        || blockState.isAir() || blockState.is(BlockTags.REPLACEABLE_BY_TREES));
    }

    /**
     * @return available space for the tree along Y axis
     */
    private int getMaxFreeTreeHeight(LevelSimulatedReader level, int treeHeight, BlockPos bottomPos) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for(int y = 0; y <= treeHeight + 1; ++y) {
            int minSize = 2;

            for(int x = -minSize; x <= minSize; ++x) {
                for(int z = -minSize; z <= minSize; ++z) {
                    pos.setWithOffset(bottomPos, x, y + 2, z);
                    if (!isFree(level, pos)) {
                        return y - 2;
                    }
                }
            }
        }

        return treeHeight;
    }

    /**
     * Places the given feature at the given location.
     * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk
     * being generated, that they can safely generate into.
     *
     * @param context A context object with a reference to the level and the position the feature is being placed at
     */
    public final boolean place(FeaturePlaceContext<BirchConfig> context) {
        // Get context data
        final WorldGenLevel worldGenLevel = context.level();
        RandomSource randomSource = context.random();
        BlockPos.MutableBlockPos pos = context.origin().mutable();
        BirchConfig config = context.config();

        // Validate tree placement
        int maxTreeHeight = 13;
        int maxY = pos.getY() + maxTreeHeight;
        if (pos.getY() - 1 < worldGenLevel.getMinBuildHeight() + 1 || maxY > worldGenLevel.getMaxBuildHeight())
            return false;
        if (this.getMaxFreeTreeHeight(worldGenLevel, maxTreeHeight, pos) < maxTreeHeight)
            return false;

        // Roots
        worldGenLevel.setBlock(context.origin().mutable().move(Direction.DOWN, 1), config.roots, 19);

        // Bottom trunk
        for (int i = 0; i < 4 + randomSource.nextInt(1); i++) {
            worldGenLevel.setBlock(pos, config.trunk, 19);
            pos.move(Direction.UP, 1);
        }

        // Birch leaves bottom
        placeRing(worldGenLevel, pos, config.trunk, () -> placeRingMedium(config, worldGenLevel, pos));

        // Birch leaves middle
        for (int i = 0; i < 2 + randomSource.nextInt(2); i++) {
            placeRing(worldGenLevel, pos, config.trunk, () -> placeRingBig(config, worldGenLevel, pos));
        }

        // Birch leaves upper
        placeRing(worldGenLevel, pos, config.leaves, () -> placeRingSmall(config, worldGenLevel, pos));
        placeRing(worldGenLevel, pos, config.leaves, () -> placeRingTiny(config, worldGenLevel, pos));

        return true;
    }

    /**
     * Places birch ring with given center block and leaves placing method.
     */
    void placeRing(final WorldGenLevel worldGenLevel, BlockPos.MutableBlockPos pos, BlockState center,
                   Runnable leavesPlacer) {
        // Center block
        worldGenLevel.setBlock(pos, center, 19);

        // Leaves around center
        leavesPlacer.run();

        // Move up
        pos.move(Direction.UP, 1);
    }

    /**
     * Places birch tiny ring at given position.
     */
    void placeRingTiny(BirchConfig config, final WorldGenLevel worldGenLevel, BlockPos.MutableBlockPos pos) {
        // Tiny circle
        worldGenLevel.setBlock(pos.mutable().move(1, 0, 0), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(0, 0, 1), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(-1, 0, 0), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(0, 0, -1), config.leaves, 19);
    }

    /**
     * Places birch small ring at given position.
     */
    void placeRingSmall(BirchConfig config, final WorldGenLevel worldGenLevel, BlockPos.MutableBlockPos pos) {
        // Place tiny ring
        placeRingTiny(config, worldGenLevel, pos);
        // Expand
        worldGenLevel.setBlock(pos.mutable().move(1, 0, 1), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(1, 0, -1), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(-1, 0, 1), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(-1, 0, -1), config.leaves, 19);
    }

    /**
     * Places birch medium ring at given position.
     */
    void placeRingMedium(BirchConfig config, final WorldGenLevel worldGenLevel, BlockPos.MutableBlockPos pos) {
        // Place small ring
        placeRingSmall(config, worldGenLevel, pos);
        // Expand
        worldGenLevel.setBlock(pos.mutable().move(0, 0, 2), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(0, 0, -2), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(2, 0, 0), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(-2, 0, 0), config.leaves, 19);
    }

    /**
     * Places birch big ring at given position (with randomness at edges if random source is provided).
     */
    void placeRingBig(BirchConfig config, final WorldGenLevel worldGenLevel, BlockPos.MutableBlockPos pos) {
        // Place medium ring
        placeRingMedium(config, worldGenLevel, pos);
        // Expand
        worldGenLevel.setBlock(pos.mutable().move(1, 0, 2), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(-1, 0, 2), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(1, 0, -2), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(-1, 0, -2), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(2, 0, 1), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(2, 0, -1), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(-2, 0, 1), config.leaves, 19);
        worldGenLevel.setBlock(pos.mutable().move(-2, 0, -1), config.leaves, 19);
    }
}
