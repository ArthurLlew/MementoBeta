package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.arthurllew.mementobeta.item.MementoBetaItems;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        // Reinforced bedrock
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, MementoBetaBlocks.REINFORCED_BEDROCK.get(), 1)
                .pattern("GMG")
                .pattern("EBE")
                .pattern("RDR")
                .define('M', MementoBetaItems.MOLTEN_MANTLE.get())
                .define('B', Items.BEDROCK)
                .define('D', Items.REINFORCED_DEEPSLATE)
                .define('G', Items.GLOWSTONE_DUST)
                .define('E', Items.ENDER_EYE)
                .define('R', Items.REDSTONE)
                .unlockedBy(getHasName(Items.BEDROCK), has(Items.BEDROCK))
                .unlockedBy(getHasName(Items.REINFORCED_DEEPSLATE), has(Items.REINFORCED_DEEPSLATE))
                .save(writer);

        // Beta lava block
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, MementoBetaBlocks.BETA_lAVA.get(), 1)
                .requires(MementoBetaItems.BETA_LAVA_BUCKET.get())
                .unlockedBy(getHasName(MementoBetaItems.BETA_LAVA_BUCKET.get()),
                        has(MementoBetaItems.BETA_LAVA_BUCKET.get()))
                .save(writer);

        // Beta fire block
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MementoBetaBlocks.BETA_FIRE.get(), 1)
                .requires(MementoBetaItems.HEATED_DRAGON_BREATH.get())
                .unlockedBy(getHasName(MementoBetaItems.HEATED_DRAGON_BREATH.get()),
                        has(MementoBetaItems.HEATED_DRAGON_BREATH.get()))
                .save(writer);
    }
}
