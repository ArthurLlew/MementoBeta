package net.arthurllew.mementobeta.block.portal;

import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.arthurllew.mementobeta.network.MementoBetaNetwork;
import net.arthurllew.mementobeta.network.packet.BetaTravelSoundPacket;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;

import java.util.Comparator;
import java.util.Optional;

public class BetaPortalForcer {
    public static final DimensionTransition.PostDimensionTransition PLAY_PORTAL_SOUND = BetaPortalForcer::playTeleportSound;

    /**
     * Destination level.
     */
    private final ServerLevel level;

    /**
     * Constructor.
     */
    public BetaPortalForcer(ServerLevel level) {
        this.level = level;
    }

    /**
     * Plays teleportation sound.
     */
    public static void playTeleportSound(Entity entity) {
        // If a player is traveling
        if (entity instanceof ServerPlayer player) {
            // Send travel sound packet
            MementoBetaNetwork.sendToPlayer(player, new BetaTravelSoundPacket());
        }
    }

    /**
     * Tries to find existing portal. Is identical to Vanilla's
     * {@link net.minecraft.world.level.portal.PortalForcer#findClosestPortalPosition(BlockPos, boolean, WorldBorder)}.
     */
    public Optional<BlockPos> findClosestPortalPosition(BlockPos exitPos, WorldBorder worldBorder) {
        // POI manager
        PoiManager poiManager = this.level.getPoiManager();

        // Portal search distance
        int portalSearchDistance = 128;

        // Check level chunks
        poiManager.ensureLoadedAndValid(this.level, exitPos, portalSearchDistance);

        // Perform search
        return poiManager.getInSquare(
                // Portal block check function
                (poiType) -> poiType.is(MementoBetaDimension.POI_TYPE),
                        // Other parameters
                        exitPos, portalSearchDistance, PoiManager.Occupancy.ANY)
                // Map position
                .map(PoiRecord::getPos)
                // Clamp by world borders
                .filter(worldBorder::isWithinBounds)
                // Check block properties
                .filter(pos -> this.level.getBlockState(pos).hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
                // Check distance and height
                .min(Comparator.<BlockPos>comparingDouble(pos -> pos.distSqr(exitPos)).thenComparingInt(Vec3i::getY));
    }

    /**
     * Creates portal frame. Is identical to Vanilla's
     * {@link net.minecraft.world.level.portal.PortalForcer#createPortal(BlockPos, Direction.Axis)}.
     *
     * @return portal frame rectangle
     */
    public Optional<BlockUtil.FoundRectangle> createPortal(BlockPos pos, Direction.Axis axis) {
        // Initial portal direction
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);

        // Init distances
        double dist = -1.0D;
        double helperDist = -1.0D;

        // Init bloc positions
        BlockPos foundPos = null;
        BlockPos helperFoundPos = null;

        // World border
        WorldBorder worldborder = this.level.getWorldBorder();

        // Topmost Y where blocks can be placed
        int maxY = Math.min(this.level.getMaxBuildHeight(),
                this.level.getMinBuildHeight() + this.level.getLogicalHeight()) - 1;

        // Changeable block position
        BlockPos.MutableBlockPos mutableBlockPos1 = pos.mutable();

        // Positions in spiral around search position
        for(BlockPos.MutableBlockPos mutableBlockPos2 :
                BlockPos.spiralAround(pos, 16, Direction.EAST, Direction.SOUTH)) {
            // Surface Y
            int surfaceY = Math.min(maxY, this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, mutableBlockPos2.getX(),
                    mutableBlockPos2.getZ()));

            // Clamp by world border
            if (worldborder.isWithinBounds(mutableBlockPos2)
                    && worldborder.isWithinBounds(mutableBlockPos2.move(direction, 1))) {
                // Opposite direction move
                mutableBlockPos2.move(direction.getOpposite(), 1);

                // From surface Y to the bottom of the world
                for(int y = surfaceY; y >= this.level.getMinBuildHeight(); y--) {
                    // Set Y to current height
                    mutableBlockPos2.setY(y);

                    // Check whether portal can replace observed block
                    if (this.canPortalReplaceBlock(mutableBlockPos2)) {
                        // Try to find the lowest replaceable block
                        int minReplaceableY = y;
                        while (y > this.level.getMinBuildHeight()
                                && this.canPortalReplaceBlock(mutableBlockPos2.move(Direction.DOWN))) {
                            y--;
                        }

                        if (y + 4 <= maxY) {
                            int portalSize = minReplaceableY - y;
                            if (portalSize <= 0 || portalSize >= 3) {
                                mutableBlockPos2.setY(y);
                                if (this.canHostFrame(mutableBlockPos2, mutableBlockPos1, direction, 0)) {
                                    // Get squared distance to search position
                                    double distanceToOrigin = pos.distSqr(mutableBlockPos2);

                                    // If position can host frame with smaller/larger offset scale
                                    // and nothing was found or distance is greater than newly found distance
                                    if (this.canHostFrame(mutableBlockPos2, mutableBlockPos1, direction, -1)
                                            && this.canHostFrame(mutableBlockPos2, mutableBlockPos1,
                                            direction, 1) && (dist == -1.0D || dist > distanceToOrigin)) {
                                        // Set distance and position
                                        dist = distanceToOrigin;
                                        foundPos = mutableBlockPos2.immutable();
                                    }

                                    // If nothing was found or helper distance is greater than newly found distance
                                    if (dist == -1.0D && (helperDist == -1.0D || helperDist > distanceToOrigin)) {
                                        // Set helper distance and position
                                        helperDist = distanceToOrigin;
                                        helperFoundPos = mutableBlockPos2.immutable();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // If only helper distance as found
        if (dist == -1.0D && helperDist != -1.0D) {
            foundPos = helperFoundPos;
            dist = helperDist;
        }

        // Prepare portal frame block
        BlockState frameBlock = MementoBetaBlocks.REINFORCED_BEDROCK.get().defaultBlockState();

        // If no suitable place was found
        if (dist == -1.0D) {
            int minY = Math.max(this.level.getMinBuildHeight() + 1, 70);
            int maxPortalY = maxY - 9;
            if (maxPortalY < minY) {
                return Optional.empty();
            }

            // Check within world border
            foundPos = new BlockPos(
                    pos.getX() - direction.getStepX(),
                    Mth.clamp(pos.getY(), minY, maxPortalY),
                    pos.getZ() - direction.getStepZ())
                    .immutable();
            foundPos = worldborder.clampToBounds(foundPos);

            // Rotate direction
            Direction directionR90 = direction.getClockWise();

            // Portal frame and air around portal
            for(int i = -1; i < 2; ++i) {
                for(int j = 0; j < 2; ++j) {
                    for(int y = -1; y < 3; ++y) {
                        mutableBlockPos1.setWithOffset(foundPos,
                                j * direction.getStepX() + i * directionR90.getStepX(), y,
                                j * direction.getStepZ() + i * directionR90.getStepZ());
                        // Frame blocks above and below portal blocks should be orientated accordingly
                        this.level.setBlockAndUpdate(mutableBlockPos1, y < 0
                                ? (i == -1 && j == 0) || (i == 1 && j == 1)
                                ? frameBlock : frameBlock.setValue(RotatedPillarBlock.AXIS, axis)
                                : Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        // Portal frame
        for(int xz = -1; xz < 3; ++xz) {
            for(int y = -1; y < 4; ++y) {
                if (xz == -1 || xz == 2 || y == -1 || y == 3) {
                    mutableBlockPos1.setWithOffset(foundPos,
                            xz * direction.getStepX(), y, xz * direction.getStepZ());
                    // Frame blocks above and below portal blocks should be orientated accordingly
                    this.level.setBlock(mutableBlockPos1, (xz == -1 || xz == 2) ? frameBlock
                            : frameBlock.setValue(RotatedPillarBlock.AXIS, axis), 3);
                }
            }
        }

        // Prepare portal block
        BlockState blockstate = MementoBetaBlocks.BETA_PORTAL.get()
                .defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);

        // Set portal blocks
        for(int xz = 0; xz < 2; ++xz) {
            for(int y = 0; y < 3; ++y) {
                mutableBlockPos1.setWithOffset(foundPos,
                        xz * direction.getStepX(), y, xz * direction.getStepZ());
                this.level.setBlock(mutableBlockPos1, blockstate, 18);
            }
        }

        return Optional.of(new BlockUtil.FoundRectangle(foundPos.immutable(), 2, 3));
    }

    /**
     * @return whether a portal frame can be inserted into the provided location
     */
    @SuppressWarnings("deprecation")
    private boolean canHostFrame(BlockPos originalPos, BlockPos.MutableBlockPos offsetPos, Direction p_direction, int offsetScale) {
        Direction direction = p_direction.getClockWise();

        for (int i = -1; i < 3; i++) {
            for (int j = -1; j < 4; j++) {
                offsetPos.setWithOffset(
                        originalPos, p_direction.getStepX() * i + direction.getStepX() * offsetScale, j, p_direction.getStepZ() * i + direction.getStepZ() * offsetScale
                );
                if (j < 0 && !this.level.getBlockState(offsetPos).isSolid()) {
                    return false;
                }

                if (j >= 0 && !this.canPortalReplaceBlock(offsetPos)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @return whether a block at given position can be replaced by portal frame
     */
    private boolean canPortalReplaceBlock(BlockPos.MutableBlockPos pos) {
        BlockState blockstate = this.level.getBlockState(pos);
        return blockstate.canBeReplaced() && blockstate.getFluidState().isEmpty();
    }
}
