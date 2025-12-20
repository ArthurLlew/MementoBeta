package net.arthurllew.mementobeta.item;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.arthurllew.mementobeta.network.MementoBetaNetwork;
import net.arthurllew.mementobeta.network.packet.MoltenBlockPacket;
import net.arthurllew.mementobeta.world.BetaDimension;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

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
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

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
            // Lava type depends on dimension
            if (level.dimensionTypeRegistration().is(BetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
                level.setBlock(pos, MementoBetaBlocks.BETA_lAVA.get().defaultBlockState(), Block.UPDATE_ALL);
            }
            else {
                level.setBlock(pos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        // On server
        if (level instanceof ServerLevel serverLevel) {
            // Send notification to all players in this level
            MementoBetaNetwork.sendToPlayersInDimension(serverLevel, new MoltenBlockPacket(pos));
        }

        // Shrink item stack
        context.getItemInHand().shrink(1);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Adds custom tooltip.
     */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip." + MementoBeta.MODID + ".molten_mantle"));
    }
}
