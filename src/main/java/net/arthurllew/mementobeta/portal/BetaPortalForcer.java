package net.arthurllew.mementobeta.portal;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.MementoBetaContent;
import net.arthurllew.mementobeta.block.BetaPortalBlock;
import net.arthurllew.mementobeta.mixin.EntityAccessor;
import net.arthurllew.mementobeta.network.MementoBetaPacketHandler;
import net.arthurllew.mementobeta.network.packet.BetaTravelSoundPacket;
import net.arthurllew.mementobeta.world.BetaDimension;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

public class BetaPortalForcer implements ITeleporter {
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
     * Plays teleportation sound. Returns {@code false} to disable Vanilla behaviour
     * (see {@link ServerPlayer#changeDimension(ServerLevel, ITeleporter)}).
     */
    @Override
    public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceLevel, ServerLevel destinationLevel) {
        // Send travel sound packet to the specified player
        MementoBetaPacketHandler.sendToPlayer(player, new BetaTravelSoundPacket());

        return false;
    }

    /**
     * Finds or creates Beta dimension portal.
     * @param entity The entity teleporting before the teleport.
     * @param destination The world the entity is teleporting to.
     * @return portal info.
     */
    @Nullable
    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destination,
                                    Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        // Entity can travel only to beta dimension
        if (entity.level().dimension() != BetaPortalUtil.destinationDimension
                && !(destination.dimension() == BetaPortalUtil.destinationDimension)) {
            return null;
        } else {
            WorldBorder worldborder = destination.getWorldBorder();

            // Teleportation scale (some dimensions, like nether, might have different coordinate scaling)
            double scale = DimensionType.getTeleportationScale(entity.level().dimensionType(),
                    destination.dimensionType());

            // Portal position
            BlockPos portalPos =
                    worldborder.clampToBounds(entity.getX() * scale, entity.getY(), entity.getZ() * scale);

            // Find ot create portal
            return this.findOrCreatePortal(entity, portalPos, worldborder).map((portalRect) -> {
                EntityAccessor entityAccessor = (EntityAccessor) entity;
                BlockState blockstate = entity.level().getBlockState(entityAccessor.getPortalEntrancePos());

                Direction.Axis direction;
                Vec3 vec3;

                if (blockstate.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                    direction = blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                    BlockUtil.FoundRectangle newPortalRect =
                            BlockUtil.getLargestRectangleAround(entityAccessor.getPortalEntrancePos(),
                                    direction, 21, Direction.Axis.Y, 21,
                                    (p_284700_) -> entity.level().getBlockState(p_284700_) == blockstate);

                    vec3 = this.getRelativePortalPosition(entity, direction, newPortalRect);
                } else {
                    direction = Direction.Axis.X;

                    vec3 = new Vec3(0.5D, 0.0D, 0.0D);
                }

                return PortalShape.createPortalInfo(destination, portalRect, direction, vec3,
                        entity, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
            }).orElse(null);
        }
    }

    /**
     * @return portal position relative to player.
     */
    private Vec3 getRelativePortalPosition(Entity entity, Direction.Axis axis, BlockUtil.FoundRectangle portal) {
        return PortalShape.getRelativePosition(portal, axis, entity.position(),
                entity.getDimensions(entity.getPose()));
    }

    /**
     * Tries to find or create (last happens only if provided entity is a server player) portal frame.
     * @return found or created portal.
     */
    protected Optional<BlockUtil.FoundRectangle> findOrCreatePortal(Entity entity, BlockPos fromPos,
                                                                    WorldBorder worldBorder) {
        // Try to find existing portal
        Optional<BlockUtil.FoundRectangle> optional = this.findPortal(fromPos, worldBorder);

        // On fail if entity is player
        if (optional.isEmpty() && entity instanceof ServerPlayer) {
            // Get portal direction
            EntityAccessor entityAccessor = (EntityAccessor) entity;
            Direction.Axis portalDirection = entity.level().getBlockState(entityAccessor.getPortalEntrancePos())
                    .getOptionalValue(BetaPortalBlock.AZIMUTH).orElse(Direction.Axis.X);

            // Try to create portal
            optional = this.createPortal(fromPos, portalDirection);
            if (optional.isEmpty()) {
                MementoBeta.LOGGER.error("Unable to create a portal (likely target out of the world border)");
            }
        }

        return optional;
    }

    /**
     * Tries to find existing portal. Is identical to Vanilla's
     * {@link net.minecraft.world.level.portal.PortalForcer#findPortalAround(BlockPos, boolean, WorldBorder)}.
     */
    public Optional<BlockUtil.FoundRectangle> findPortal(BlockPos fromPos, WorldBorder pWorldBorder) {
        // POI manager
        PoiManager poiManager = this.level.getPoiManager();

        // Portal search distance
        int portalSearchDistance = 64;

        // Check level chunks
        poiManager.ensureLoadedAndValid(this.level, fromPos, portalSearchDistance);

        // Search
        Optional<PoiRecord> optional = poiManager.getInSquare((poiType) -> {
            // If block is a correct portal block
            return poiType.is(BetaDimension.BETA_PORTAL.getKey());
        }, fromPos, portalSearchDistance, PoiManager.Occupancy.ANY).filter((poiRecord) -> {
            // Clamp block position by world borders
            return pWorldBorder.isWithinBounds(poiRecord.getPos());
        }).sorted(Comparator.<PoiRecord>comparingDouble((poiRecord) -> {
            // Check distance
            return poiRecord.getPos().distSqr(fromPos);
        }).thenComparingInt((poiRecord) -> {
            // Check height
            return poiRecord.getPos().getY();
        })).filter((poiRecord) -> {
            // Check block properties
            return this.level.getBlockState(poiRecord.getPos()).hasProperty(BlockStateProperties.HORIZONTAL_AXIS);
        }).findFirst();

        // Process search results
        return optional.map((poiRecord) -> {
            BlockPos blockpos = poiRecord.getPos();

            this.level.getChunkSource().addRegionTicket(TicketType.PORTAL,
                    new ChunkPos(blockpos), 3, blockpos);

            BlockState blockstate = this.level.getBlockState(blockpos);

            return BlockUtil.getLargestRectangleAround(blockpos,
                    blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS),
                    21, Direction.Axis.Y, 21, (pos) -> this.level.getBlockState(pos) == blockstate);
        });
    }

    /**
     * Creates portal frame. Is identical to Vanilla's
     * {@link net.minecraft.world.level.portal.PortalForcer#createPortal(BlockPos, Direction.Axis)}.
     * @return portal frame rectangle.
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
                for(int y = surfaceY; y >= this.level.getMinBuildHeight(); --y) {
                    // Set Y to current height
                    mutableBlockPos2.setY(y);

                    // Check whether portal can replace observed block
                    if (this.canPortalReplaceBlock(mutableBlockPos2)) {
                        // This loop is probably inspired by Alpha/Beta Minecraft code :)
                        int minReplaceableY;
                        for(minReplaceableY = y; y > this.level.getMinBuildHeight()
                                && this.canPortalReplaceBlock(mutableBlockPos2.move(Direction.DOWN)); --y) {
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
        BlockState frameBlock = MementoBetaContent.REINFORCED_BEDROCK.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, axis);

        // If no suitable place was found
        if (dist == -1.0D) {
            int k1 = Math.max(this.level.getMinBuildHeight() + 1, 70);
            int i2 = maxY - 9;
            if (i2 < k1) {
                return Optional.empty();
            }

            foundPos = (new BlockPos(pos.getX(), Mth.clamp(pos.getY(), k1, i2), pos.getZ())).immutable();
            Direction direction1 = direction.getClockWise();
            if (!worldborder.isWithinBounds(foundPos)) {
                return Optional.empty();
            }

            // Portal frame and air around portal
            for(int i3 = -1; i3 < 2; ++i3) {
                for(int j3 = 0; j3 < 2; ++j3) {
                    for(int k3 = -1; k3 < 3; ++k3) {
                        mutableBlockPos1.setWithOffset(foundPos,
                                j3 * direction.getStepX() + i3 * direction1.getStepX(), k3,
                                j3 * direction.getStepZ() + i3 * direction1.getStepZ());
                        this.level.setBlockAndUpdate(mutableBlockPos1,
                                k3 < 0 ? frameBlock : Blocks.AIR.defaultBlockState());
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
                    this.level.setBlock(mutableBlockPos1, frameBlock, 3);
                }
            }
        }

        // Prepare portal block
        BlockState blockstate = MementoBetaContent.BETA_PORTAL.get()
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
     * @return whether a portal frame can be inserted into the provided location.
     */
    @SuppressWarnings("deprecation")
    private boolean canHostFrame(BlockPos originalPos, BlockPos.MutableBlockPos offsetPos, Direction direction,
                                 int offsetScale) {
        Direction directionRot = direction.getClockWise();

        for(int i = -1; i < 3; ++i) {
            for(int j = -1; j < 4; ++j) {
                offsetPos.setWithOffset(originalPos,
                        directionRot.getStepX() * i + directionRot.getStepX() * offsetScale, j,
                        directionRot.getStepZ() * i + directionRot.getStepZ() * offsetScale);
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
     * @return whether a block at given position can be replaced by portal frame.
     */
    private boolean canPortalReplaceBlock(BlockPos.MutableBlockPos pPos) {
        BlockState blockstate = this.level.getBlockState(pPos);

        return blockstate.canBeReplaced() && blockstate.getFluidState().isEmpty();
    }
}
