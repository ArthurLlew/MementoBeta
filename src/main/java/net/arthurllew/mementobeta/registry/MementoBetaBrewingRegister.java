package net.arthurllew.mementobeta.registry;

import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

@EventBusSubscriber(modid = MementoBeta.MODID)
public class MementoBetaBrewingRegister {
    /**
     * Brewing recipes registering.
     * @param event brewing registering event.
     */
    @SubscribeEvent
    public static void registerBrewing(RegisterBrewingRecipesEvent event) {
        // Molten mantle brewing recipe(s)
        event.getBuilder().addRecipe(Ingredient.of(Items.DRAGON_BREATH),
                Ingredient.of(Items.BLAZE_POWDER), new ItemStack(MementoBetaItems.HEATED_DRAGON_BREATH.get()));
        event.getBuilder().addRecipe(Ingredient.of(MementoBetaItems.HEATED_DRAGON_BREATH.get()),
                Ingredient.of(Items.MAGMA_CREAM), new ItemStack(MementoBetaItems.MOLTEN_MANTLE.get()));
    }
}
