package net.arthurllew.mementobeta.item;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    /**
     * Adds custom tooltip.
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("tooltip." + MementoBeta.MODID + ".molten_mantle"));
    }
}
