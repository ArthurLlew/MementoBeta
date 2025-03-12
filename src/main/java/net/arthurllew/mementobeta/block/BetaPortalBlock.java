package net.arthurllew.mementobeta.block;

import net.arthurllew.mementobeta.capabilities.BetaPlayerCapability;
import net.arthurllew.mementobeta.capabilities.MementoBetaCapabilities;
import net.arthurllew.mementobeta.mixin.EntityAccessor;
import net.arthurllew.mementobeta.portal.BetaPortalForcer;
import net.arthurllew.mementobeta.portal.BetaPortalShape;
import net.arthurllew.mementobeta.portal.BetaPortalUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BetaPortalBlock extends Block {
    /**
     * Stores horizontal axes state.
     */
    public static final EnumProperty<Direction.Axis> AZIMUTH = BlockStateProperties.HORIZONTAL_AXIS;

    // Voxel shapes
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    /**
     * Constructor matching super (also configures default block state).
     */
    public BetaPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AZIMUTH, Direction.Axis.X));
    }

    /**
     * Connects properties to a block state.
     */
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AZIMUTH);
    }

    /**
     * @return block voxel shape.
     */
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch (state.getValue(AZIMUTH)) {
            case Z:
                return Z_AXIS_AABB;
            case X:
            default:
                return X_AXIS_AABB;
        }
    }

    /**
     * Performs a random tick on a block.
     */
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Handle piglin zombification
        if (level.dimensionType().natural() && level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
                && random.nextInt(2000) < level.getDifficulty().getId()) {
            // Calculate position
            while (level.getBlockState(pos).is(this)) {
                pos = pos.below();
            }

            // Zombify piglin
            if (level.getBlockState(pos).isValidSpawn(level, pos, EntityType.ZOMBIFIED_PIGLIN)) {
                Entity entity = EntityType.ZOMBIFIED_PIGLIN.spawn(level, pos.above(), MobSpawnType.STRUCTURE);
                if (entity != null) {
                    entity.setPortalCooldown();
                }
            }
        }
    }

    /**
     * Determines a new block state after a neighboring block was changed.
     */
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction facing, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // Directions
        Direction.Axis direction = state.getValue(AZIMUTH);
        Direction.Axis newDirection = facing.getAxis();

        // If directions don't match,
        return !(newDirection.isHorizontal() && direction != newDirection)
                // neighbor is a different block
                && !neighborState.is(this)
                // and portal frame is broken
                && !(new BetaPortalShape(level, pos, direction)).isComplete()
                    // True: air
                    ? Blocks.AIR.defaultBlockState()
                        // False: state matching neighbor
                        : super.updateShape(state, facing, neighborState, level, pos, neighborPos);
    }

    /**
     * Called when entity enters this block.
     */
    @SuppressWarnings("deprecation")
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // Entity is not a passenger or a vehicle and can change dimension
        if (!entity.isPassenger() && !entity.isVehicle() && entity.canChangeDimensions()) {
            // Check portal cooldown
            if (entity.isOnPortalCooldown()) {
                entity.setPortalCooldown();
            } else {
                // Set portal position
                EntityAccessor entityAccessor = (EntityAccessor) entity;
                if (!entity.level().isClientSide() && !pos.equals(entityAccessor.getPortalEntrancePos())) {
                    entityAccessor.setPortalEntrancePos(pos.immutable());
                }

                // Check if entity has beta player capability (thus is a player,
                // since that capability is only applied to a player)
                LazyOptional<BetaPlayerCapability> betaPlayer =
                        entity.getCapability(MementoBetaCapabilities.BETA_PLAYER_CAPABILITY);
                if (!betaPlayer.isPresent()) {
                    // Non-fancy teleport
                    this.handleTeleportation(entity);
                } else {
                    // Fancy player teleport
                    betaPlayer.ifPresent(handler -> {
                        handler.setInPortal(true);

                        // Get already time in portal
                        int waitTime = handler.getPortalTime();
                        // Compair to max wait time
                        if (waitTime >= entity.getPortalWaitTime()) {
                            this.handleTeleportation(entity);
                            handler.setPortalTime(0);
                        }
                    });
                }
            }
        }
    }

    /**
     * Handle non-player entity teleportation.
     */
    private void handleTeleportation(Entity entity) {
        MinecraftServer server = entity.level().getServer();
        if (server != null) {
            // Get destination level
            ResourceKey<Level> destinationKey = entity.level().dimension() == BetaPortalUtil.destinationDimension
                    ? BetaPortalUtil.returnDimension : BetaPortalUtil.destinationDimension;
            ServerLevel destinationLevel = server.getLevel(destinationKey);

            // Has destination and entity is no a passenger
            if (destinationLevel != null && !entity.isPassenger()) {
                // Teleport
                entity.level().getProfiler().push("beta_portal");
                entity.setPortalCooldown();
                entity.changeDimension(destinationLevel, new BetaPortalForcer(destinationLevel));
                entity.level().getProfiler().pop();
            }
        }
    }

    /**
     * Handle particles on tick.
     */
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D,
                    (double)pos.getZ() + 0.5D, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS,
                    0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }

        for (int i = 0; i < 4; ++i) {
            double d0 = (double) pos.getX() + random.nextDouble();
            double d1 = (double) pos.getY() + random.nextDouble();
            double d2 = (double) pos.getZ() + random.nextDouble();
            double d3 = ((double) random.nextFloat() - 0.5D) * 0.5D;
            double d4 = ((double) random.nextFloat() - 0.5D) * 0.5D;
            double d5 = ((double) random.nextFloat() - 0.5D) * 0.5D;
            int j = random.nextInt(2) * 2 - 1;
            if (!level.getBlockState(pos.west()).is(this) && !level.getBlockState(pos.east()).is(this)) {
                d0 = (double) pos.getX() + 0.5D + 0.25D * (double) j;
                d3 = random.nextFloat() * 2.0F * (float) j;
            } else {
                d2 = (double) pos.getZ() + 0.5D + 0.25D * (double) j;
                d5 = random.nextFloat() * 2.0F * (float) j;
            }

            level.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
        }
    }

    /**
     * Picks block via middle-clicking.
     * @return empty stack so creative player is unable to get portal block.
     */
    @SuppressWarnings("deprecation")
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    /**
     * Rotates block.
     * @return block state corresponding to rotation context.
     */
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch (state.getValue(AZIMUTH)) {
                    case Z:
                        return state.setValue(AZIMUTH, Direction.Axis.X);
                    case X:
                        return state.setValue(AZIMUTH, Direction.Axis.Z);
                    default:
                        return state;
                }
            default:
                return state;
        }
    }
}
