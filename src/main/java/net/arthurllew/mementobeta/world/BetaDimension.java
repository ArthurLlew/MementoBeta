package net.arthurllew.mementobeta.world;

import com.mojang.serialization.Codec;
import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BetaDimension {
    /**
     * Total day cycle time in ticks for this dimension.
     */
    public static final long DAY_CYCLE_TOTAL_TIME = 24000L;

    // Dimension resource keys
    public static final ResourceKey<LevelStem> BETA_DIMENSION =
            ResourceKey.create(Registries.LEVEL_STEM,
                    new ResourceLocation(MementoBeta.MODID, "betaworld"));
    public static final ResourceKey<Level> BETA_DIMENSION_LEVEL =
            ResourceKey.create(Registries.DIMENSION,
                    new ResourceLocation(MementoBeta.MODID, "betaworld"));
    public static final ResourceKey<DimensionType> BETA_DIMENSION_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    new ResourceLocation(MementoBeta.MODID, "betaworld_type"));

    /**
     * Chunk generator register.
     */
    public static DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MementoBeta.MODID);
    /**
     * Chunk generator.
     */
    public static final RegistryObject<Codec<? extends ChunkGenerator>> BETA_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("beta_chunk_generator", () -> BetaChunkGenerator.CODEC);
}
