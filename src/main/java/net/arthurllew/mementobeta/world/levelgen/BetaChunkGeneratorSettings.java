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
 *
 * @param grassBlocks grass blocks (should have dirt underneath)
 * @param surfaceBlocks other surface blocks
 * @param belowTopOne first block below top layer
 * @param belowTopOneDesert first block below top layer in deserts
 * @param belowTopTwo second block below top layer
 * @param stoneBlock main stone block
 * @param carverBlocks what blocks can be carved by caves (will include {@code surfaceBlocks})
 */
public record BetaChunkGeneratorSettings(List<Block> grassBlocks, List<Block> surfaceBlocks,
                                         Block belowTopOne, Block belowTopOneDesert, Block belowTopTwo,
                                         Block stoneBlock, List<Block> carverBlocks) {
    /**
     * Codec for reading file.
     */
    public static final Codec<BetaChunkGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create((settings) ->
            settings.group(
                    BuiltInRegistries.BLOCK.byNameCodec().stable().listOf().fieldOf("grass_blocks").forGetter(
                            BetaChunkGeneratorSettings::grassBlocks),
                    BuiltInRegistries.BLOCK.byNameCodec().stable().listOf().fieldOf("surface_blocks").forGetter(
                            BetaChunkGeneratorSettings::surfaceBlocks),
                    BuiltInRegistries.BLOCK.byNameCodec().stable().fieldOf("below_top_1").forGetter(
                            BetaChunkGeneratorSettings::belowTopOne),
                    BuiltInRegistries.BLOCK.byNameCodec().stable().fieldOf("below_top_1_desert").forGetter(
                            BetaChunkGeneratorSettings::belowTopOneDesert),
                    BuiltInRegistries.BLOCK.byNameCodec().stable().fieldOf("below_top_2").forGetter(
                            BetaChunkGeneratorSettings::belowTopTwo),
                    BuiltInRegistries.BLOCK.byNameCodec().stable().fieldOf("stone_block").forGetter(
                            BetaChunkGeneratorSettings::stoneBlock),
                    BuiltInRegistries.BLOCK.byNameCodec().stable().listOf().fieldOf("carver_blocks").forGetter(
                            BetaChunkGeneratorSettings::carverBlocks)
            ).apply(settings, BetaChunkGeneratorSettings::new));

    /**
     * Codec for producing holder.
     */
    public static final Codec<Holder<BetaChunkGeneratorSettings>> CODEC =
            RegistryFileCodec.create(DataPackRegister.BETA_SETTINGS, DIRECT_CODEC);

    /**
     * Constructor with some extra steps.
     */
    public BetaChunkGeneratorSettings(List<Block> grassBlocks, List<Block> surfaceBlocks,
                                      Block belowTopOne, Block belowTopOneDesert, Block belowTopTwo,
                                      Block stoneBlock, List<Block> carverBlocks) {
        this.grassBlocks = grassBlocks;
        this.surfaceBlocks = surfaceBlocks;
        this.belowTopOne = belowTopOne;
        this.belowTopOneDesert = belowTopOneDesert;
        this.belowTopTwo = belowTopTwo;
        this.stoneBlock = stoneBlock;

        // Init list with provided values
        ArrayList<Block> allCarverBlocks = new ArrayList<>(carverBlocks);
        // All previous blocks should be carvable too
        allCarverBlocks.addAll(this.grassBlocks);
        allCarverBlocks.addAll(this.surfaceBlocks);
        allCarverBlocks.add(this.belowTopOne);
        // allCarverBlocks.add(this.belowTopOneDesert); // Sandstone was not carvable in Beta
        allCarverBlocks.add(this.belowTopTwo);
        allCarverBlocks.add(this.stoneBlock);

        this.carverBlocks = Collections.unmodifiableList(allCarverBlocks);
    }
}
