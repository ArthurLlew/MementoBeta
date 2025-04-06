package net.arthurllew.mementobeta.block.entity;

import net.arthurllew.mementobeta.block.MoltenBlock;
import net.arthurllew.mementobeta.network.MementoBetaPacketHandler;
import net.arthurllew.mementobeta.network.packet.MoltenBlockSolidifiedPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Block entity that allows to tick and save/load {@link MoltenBlock} lifetime (time until it solidifies).
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MoltenBlockEntity extends BlockEntity {
    /**
     * Molten block lifetime (until it will cool down).
     */
    private int lifeTime = 300;

    /**
     * Constructor.
     */
    public MoltenBlockEntity(BlockPos pos, BlockState blockState) {
        super(MementoBetaBlockEntities.MOLTEN_BLOCK_ENTITY.get(), pos, blockState);
    }

    /**
     * Ticks molten block cooling.
     * @return whether the block has cooled down.
     */
    public boolean tickCooling() {
        if (this.lifeTime > 0) {
            this.lifeTime--;
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Ticks molten block on server.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        // Tick and check molten block lifetime
        if (((MoltenBlockEntity)blockEntity).tickCooling()) {
            // Replace with solid version if lifetime has expired
            level.setBlock(pos, ((MoltenBlock)state.getBlock()).solidBlock.defaultBlockState(), 3);

            // Send notification to all players in this level
            MementoBetaPacketHandler.sendToPlayersInDimension(level, new MoltenBlockSolidifiedPacket(pos));
        }
    }

    /**
     * Loads block entity data from world save file.
     * @param tag data to load.
     */
    public void load(CompoundTag tag) {
        super.load(tag);
        this.lifeTime = tag.getInt("LifeTime");
    }

    /**
     * Saves block entity data in world save file.
     * @param tag data to save.
     */
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("LifeTime", this.lifeTime);
    }
}
