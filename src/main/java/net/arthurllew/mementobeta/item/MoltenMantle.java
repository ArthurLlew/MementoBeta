package net.arthurllew.mementobeta.item;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

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
        // Bedrock is replaced with molten version
        if (clickedBlock.is(Blocks.BEDROCK)) {
            level.setBlock(pos, MementoBetaBlocks.MOLTEN_BEDROCK.get().defaultBlockState(),
                    Block.UPDATE_ALL);
        }
        // Reinforced deepslate is replaced with molten version
        else if (clickedBlock.is(Blocks.REINFORCED_DEEPSLATE)) {
            level.setBlock(pos, MementoBetaBlocks.MOLTEN_REINFORCED_DEEPSLATE.get().defaultBlockState(),
                    Block.UPDATE_ALL);
        }
        // Everything else turns into lava
        else {
            level.setBlock(pos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
        }

        // Play lava sound
        RandomSource randomSource = level.getRandom();
        level.playLocalSound(pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                2.6F + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8F, false);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Adds custom tooltip.
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("tooltip." + MementoBeta.MODID + ".molten_mantle"));
    }
}
