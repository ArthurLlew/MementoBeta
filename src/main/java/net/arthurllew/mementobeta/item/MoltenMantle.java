package net.arthurllew.mementobeta.item;

import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MoltenMantle extends Item {
    public MoltenMantle(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        // Get clicked block
        BlockState clickedBlock = level.getBlockState(pos);
        // If it is bedrock
        if (clickedBlock.is(Blocks.BEDROCK)) {
            // Replace it with molten bedrock
            level.setBlock(pos, MementoBetaBlocks.MOLTEN_BEDROCK.get().defaultBlockState(),
                    Block.UPDATE_ALL);

            // Play lava sound
            level.playSound(context.getPlayer(), pos, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 1.0F,
                    level.getRandom().nextFloat() * 0.1F + 0.9F);

            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        // If it is reinforced deepslate
        else if (clickedBlock.is(Blocks.REINFORCED_DEEPSLATE)) {
            // Replace it with molten reinforced deepslate
            level.setBlock(pos, MementoBetaBlocks.MOLTEN_REINFORCED_DEEPSLATE.get().defaultBlockState(),
                    Block.UPDATE_ALL);

            // Play lava sound
            level.playSound(context.getPlayer(), pos, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 1.0F,
                    level.getRandom().nextFloat() * 0.1F + 0.9F);

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }
}
