package net.arthurllew.mementobeta.registry;

import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
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
        event.getBuilder().addRecipe(Ingredient.of(PotionContents.createItemStack(Items.POTION, Potions.STRONG_STRENGTH)),
                Ingredient.of(Items.MAGMA_CREAM), new ItemStack(MementoBetaItems.MOLTEN_MANTLE.get()));
    }
}
