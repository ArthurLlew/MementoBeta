package net.arthurllew.mementobeta.world.levelgen.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class BetaFeaturesConfig implements FeatureConfiguration {
    /**
     * Codec for reading data files.
     */
    public static final Codec<BetaFeaturesConfig> CODEC = RecordCodecBuilder.create(
            (betaFeaturesConfig) -> betaFeaturesConfig.group(
                    Codec.BOOL.fieldOf("dummy_value").forGetter((config) -> config.dummyValue)
            ).apply(betaFeaturesConfig, BetaFeaturesConfig::new));

    /**
     * Placeholder.
     */
    public boolean dummyValue;

    /**
     * Initiates config. Is used by CODEC.
     */
    protected BetaFeaturesConfig(boolean dummyValue) {
        this.dummyValue = dummyValue;
    }
}
