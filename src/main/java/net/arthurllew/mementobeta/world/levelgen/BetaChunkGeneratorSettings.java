package net.arthurllew.mementobeta.world.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arthurllew.mementobeta.event.DataPackRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores custom beta chunk generator settings.
 * @param stoneBlock stone equivalent block.
 * @param sandstoneBlock sandstone equivalent block.
 * @param grassBlocks grass blocks that can be met on the surface when carving caves.
 * @param carverBlocks what blocks can be carved by caves (will include {@code grassBlocks})
 */
public record BetaChunkGeneratorSettings(Block stoneBlock, Block sandstoneBlock,
                                         List<Block> grassBlocks, List<Block> carverBlocks) {
    /**
     * Codec for reading file.
     */
    public static final Codec<BetaChunkGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create((settings) ->
            settings.group(
                    BuiltInRegistries.BLOCK.byNameCodec().stable().fieldOf("stone_block").forGetter(
                            BetaChunkGeneratorSettings::stoneBlock),
                    BuiltInRegistries.BLOCK.byNameCodec().stable().fieldOf("sandstone_block").forGetter(
                            BetaChunkGeneratorSettings::sandstoneBlock),
                    BuiltInRegistries.BLOCK.byNameCodec().stable().listOf().fieldOf("grass_blocks").forGetter(
                            BetaChunkGeneratorSettings::grassBlocks),
                    BuiltInRegistries.BLOCK.byNameCodec().stable().listOf().fieldOf("carver_blocks").forGetter(
                            BetaChunkGeneratorSettings::carverBlocks)
            ).apply(settings, BetaChunkGeneratorSettings::new));

    /**
     * Codec for producing holder.
     */
    public static final Codec<Holder<BetaChunkGeneratorSettings>> CODEC =
            RegistryFileCodec.create(DataPackRegister.BETA_SETTINGS, DIRECT_CODEC);

    /**
     * Slightly modified constructor
     */
    public BetaChunkGeneratorSettings(Block stoneBlock, Block sandstoneBlock,
                                      List<Block> grassBlocks, List<Block> carverBlocks) {
        this.stoneBlock = stoneBlock;
        this.sandstoneBlock = sandstoneBlock;
        this.grassBlocks = grassBlocks;

        // All grass blocks can be carved
        ArrayList<Block> allCarverBlocks = new ArrayList<>();
        allCarverBlocks.addAll(carverBlocks);
        allCarverBlocks.addAll(grassBlocks);
        this.carverBlocks = Collections.unmodifiableList(allCarverBlocks);
    }
}
