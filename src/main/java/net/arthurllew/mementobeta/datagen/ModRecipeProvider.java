package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.arthurllew.mementobeta.item.MementoBetaItems;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
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
        // Resonance stone
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, MementoBetaItems.RESONANCE_STONE.get(), 1)
                .pattern("EAE")
                .pattern("ADA")
                .pattern("EAE")
                .define('A', Items.AMETHYST_SHARD)
                .define('D', Items.DIAMOND)
                .define('E', Items.ECHO_SHARD)
                .unlockedBy(getHasName(Items.AMETHYST_SHARD), has(Items.AMETHYST_SHARD))
                .unlockedBy(getHasName(Items.DIAMOND), has(Items.DIAMOND))
                .unlockedBy(getHasName(Items.ECHO_SHARD), has(Items.ECHO_SHARD))
                .save(writer);

        // Resonance pickaxe
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, MementoBetaItems.RESONANCE_PICKAXE.get(), 1)
                .pattern("CCC")
                .pattern(" N ")
                .pattern(" N ")
                .define('C', MementoBetaItems.RESONANCE_STONE.get())
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy(getHasName(MementoBetaItems.RESONANCE_STONE.get()),
                        has(MementoBetaItems.RESONANCE_STONE.get()))
                .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .save(writer);

        // Reinforced bedrock
        // Corrupted stone allows the destruction by corrupted tools due to the same resonance frequency
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, MementoBetaBlocks.REINFORCED_BEDROCK.get(), 1)
                .pattern("RSR")
                .pattern("SBS")
                .pattern("RSR")
                .define('R', Items.REINFORCED_DEEPSLATE)
                .define('B', Items.BEDROCK)
                .define('S', MementoBetaItems.RESONANCE_STONE.get())
                .unlockedBy(getHasName(Items.REINFORCED_DEEPSLATE), has(Items.REINFORCED_DEEPSLATE))
                .unlockedBy(getHasName(Items.BEDROCK), has(Items.BEDROCK))
                .unlockedBy(getHasName(MementoBetaItems.RESONANCE_STONE.get()),
                        has(MementoBetaItems.RESONANCE_STONE.get()))
                .save(writer);
    }
}
