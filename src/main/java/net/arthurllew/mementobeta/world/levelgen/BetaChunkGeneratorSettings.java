package net.arthurllew.mementobeta.world.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arthurllew.mementobeta.event.CustomDataRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Stores custom beta chunk generator settings.
 * @param stoneBlock
 * @param sandstoneBlock
 */
public record BetaChunkGeneratorSettings(Block stoneBlock, Block sandstoneBlock,
                                         List<Block> carverBlocks, List<Block> grassBlocks) {
    /**
     * Codec for reading file.
     */
    public static final Codec<BetaChunkGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create((settings) ->
            settings.group(
                    BuiltInRegistries.BLOCK.byNameCodec().stable().fieldOf("stone_block").forGetter(
                            BetaChunkGeneratorSettings::stoneBlock),
                    BuiltInRegistries.BLOCK.byNameCodec().stable().fieldOf("sandstone_block").forGetter(
                            BetaChunkGeneratorSettings::sandstoneBlock),
                    BuiltInRegistries.BLOCK.byNameCodec().stable().listOf().fieldOf("carver_blocks").forGetter(
                            BetaChunkGeneratorSettings::carverBlocks),
                    BuiltInRegistries.BLOCK.byNameCodec().stable().listOf().fieldOf("grass_blocks").forGetter(
                            BetaChunkGeneratorSettings::grassBlocks)
            ).apply(settings, BetaChunkGeneratorSettings::new));

    /**
     * Codec for producing holder.
     */
    public static final Codec<Holder<BetaChunkGeneratorSettings>> CODEC =
            RegistryFileCodec.create(CustomDataRegister.BETA_SETTINGS, DIRECT_CODEC);
}
