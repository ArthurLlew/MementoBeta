package net.arthurllew.mementobeta.block;

import net.arthurllew.mementobeta.block.entity.MementoBetaBlockEntities;
import net.arthurllew.mementobeta.block.entity.MoltenBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * {@link net.minecraft.world.level.block.MagmaBlock} extension with cooling functionality.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MoltenBlock extends MagmaBlock implements EntityBlock {
    /**
     * Solid version of this block from which it melted and into which it will solidify with time.
     */
    public final Block solidBlock;

    /**
     * Constructor.
     */
    public MoltenBlock(BlockBehaviour.Properties properties, Block solidBlock) {
        super(properties);
        this.solidBlock = solidBlock;
    }

    /**
     * @return block entity associated with this block.
     */
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MoltenBlockEntity(pos, state);
    }

    /**
     * @return ticker for block entity associated with this block.
     */
    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        return (!level.isClientSide && (blockEntityType == MementoBetaBlockEntities.MOLTEN_BLOCK_ENTITY.get())) ?
                MoltenBlockEntity::serverTick : null;
    }
}
