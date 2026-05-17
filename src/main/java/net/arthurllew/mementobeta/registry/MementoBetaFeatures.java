package net.arthurllew.mementobeta.registry;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.world.levelgen.features.BetaFeatures;
import net.arthurllew.mementobeta.world.levelgen.features.BetaFeaturesConfig;
import net.arthurllew.mementobeta.world.levelgen.features.trees.Birch;
import net.arthurllew.mementobeta.world.levelgen.features.trees.BirchConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings({"SameParameterValue", "unused"})
public class MementoBetaFeatures {
    /**
     * Deferred Register for terrain generation features.
     */
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE, MementoBeta.MODID);

    /**
     * Packed Beta 1.7.3 features.
     */
    public static final DeferredHolder<Feature<?>, Feature<BetaFeaturesConfig>> BETA_FEATURES =
            registerFeature("beta_features", () -> new BetaFeatures(BetaFeaturesConfig.CODEC));

    /**
     * Fancy birch.
     */
    public static final DeferredHolder<Feature<?>, Feature<BirchConfig>> BIRCH =
            registerFeature("tree_birch_fancy", () -> new Birch(BirchConfig.CODEC));

    /**
     * Registers terrain generation feature.
     */
    private static <T extends Feature<?>> DeferredHolder<Feature<?>, T> registerFeature(String name,
                                                                                        Supplier<T> feature) {
        return FEATURES.register(name, feature);
    }
}
