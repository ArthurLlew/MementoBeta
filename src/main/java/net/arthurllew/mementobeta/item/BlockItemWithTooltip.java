package net.arthurllew.mementobeta.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockItemWithTooltip extends BlockItem {
    /**
     * Tooltip translatable key.
     */
    private final String tooltipKey;

    /**
     * Block item with a tooltip.
     */
    public BlockItemWithTooltip(Block pBlock, Properties pProperties, String tooltipKey) {
        super(pBlock, pProperties);
        this.tooltipKey = tooltipKey;

    }
    /**
     * Adds custom tooltip.
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable(this.tooltipKey));
    }
}
