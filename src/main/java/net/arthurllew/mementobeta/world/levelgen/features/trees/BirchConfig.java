package net.arthurllew.mementobeta.world.levelgen.features.trees;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class BirchConfig implements FeatureConfiguration {
    /**
     * Codec for reading data files.
     */
    public static final Codec<BirchConfig> CODEC = RecordCodecBuilder.create(
        (spruceConfig) -> spruceConfig.group(
            BlockState.CODEC.fieldOf("roots").forGetter((config) -> config.trunk),
            BlockState.CODEC.fieldOf("trunk").forGetter((config) -> config.trunk),
            BlockState.CODEC.fieldOf("leaves").forGetter((config) -> config.leaves)
        ).apply(spruceConfig, BirchConfig::new));

    // Birch parts
    public BlockState roots;
    public BlockState trunk;
    public BlockState leaves;

    /**
     * Initiates config. Is used by CODEC.
     */
    protected BirchConfig(BlockState roots, BlockState trunk, BlockState leaves) {
        // Fill in birch parts
        this.roots = roots;
        this.trunk = trunk;
        this.leaves = leaves.setValue(LeavesBlock.PERSISTENT, true);
    }
}
