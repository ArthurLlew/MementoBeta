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
     */
    @SubscribeEvent
    public static void registerBrewing(RegisterBrewingRecipesEvent event) {
        // Molten mantle brewing recipe
        event.getBuilder().addRecipe(
                Ingredient.of(Items.GLASS_BOTTLE),
                Ingredient.of(MementoBetaItems.LAVA_CREAM.get()),
                new ItemStack(MementoBetaItems.MOLTEN_MANTLE.get()));
    }
}
