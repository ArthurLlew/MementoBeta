package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.arthurllew.mementobeta.registry.MementoBetaItems;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
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
                .save(recipeOutput);

        // Beta lava block
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, MementoBetaBlocks.BETA_lAVA.get(), 1)
                .requires(MementoBetaItems.BETA_LAVA_BUCKET.get())
                .unlockedBy(getHasName(MementoBetaItems.BETA_LAVA_BUCKET.get()),
                        has(MementoBetaItems.BETA_LAVA_BUCKET.get()))
                .save(recipeOutput);

        // Beta fire block
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MementoBetaBlocks.BETA_FIRE.get(), 1)
                .requires(MementoBetaItems.HEATED_DRAGON_BREATH.get())
                .unlockedBy(getHasName(MementoBetaItems.HEATED_DRAGON_BREATH.get()),
                        has(MementoBetaItems.HEATED_DRAGON_BREATH.get()))
                .save(recipeOutput);
    }
}
