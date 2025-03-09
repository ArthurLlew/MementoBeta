package net.arthurllew.mementobeta.item;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.MementoBetaContent;
import net.arthurllew.mementobeta.util.MementoBetaTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;

public class MementoBetaTiers {
    /**
     * Resonance tools tier. Is above netherite. Can mine bedrock.
     */
    public static final Tier RESONANCE_TIER = TierSortingRegistry.registerTier(
            new ForgeTier(5, 2000, 10f, 4f, 20,
                    MementoBetaTags.NEEDS_RESONANCE_TOOL,
                    () -> Ingredient.of(MementoBetaContent.RESONANCE_STONE.get())),
            new ResourceLocation(MementoBeta.MODID, "resonance"), List.of(Tiers.NETHERITE), List.of());
}
