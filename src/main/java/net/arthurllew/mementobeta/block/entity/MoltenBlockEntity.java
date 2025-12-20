package net.arthurllew.mementobeta.block.entity;

import net.arthurllew.mementobeta.block.MoltenBlock;
import net.arthurllew.mementobeta.network.MementoBetaNetwork;
import net.arthurllew.mementobeta.network.packet.MoltenBlockPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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

            // On server
            if (level instanceof ServerLevel serverLevel) {
                // Send notification to all players in this level
                MementoBetaNetwork.sendToPlayersInDimension(serverLevel, new MoltenBlockPacket(pos));
            }
        }
    }

    /**
     * Loads block entity data from world save file.
     * @param tag data to load.
     * @param registries game registries.
     */
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.lifeTime = tag.getInt("LifeTime");
    }

    /**
     * Saves block entity data in world save file.
     * @param tag data to save.
     * @param registries game registries.
     */
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("LifeTime", this.lifeTime);
    }
}
