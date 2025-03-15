package net.arthurllew.mementobeta.world;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.arthurllew.mementobeta.world.biome.BetaBiomeSupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
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
     * POI type Deferred Register.
     */
    public static final DeferredRegister<PoiType> POI =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, MementoBeta.MODID);
    /**
     * Beta portal POI type.
     */
    public static final RegistryObject<PoiType> BETA_PORTAL =
            POI.register("beta_portal", () -> new PoiType(ImmutableSet
                        .copyOf(MementoBetaBlocks.BETA_PORTAL.get().getStateDefinition().getPossibleStates()),
                    0, 1));

    /**
     * Beta biome source Deferred Register.
     */
    public static DeferredRegister<Codec<? extends BiomeSource>> BETA_BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, MementoBeta.MODID);
    /**
     * Beta biome source.
     */
    public static final RegistryObject<Codec<? extends BiomeSource>> BETA_BIOME_SOURCE =
            BETA_BIOME_SOURCES.register("beta_biome_source", () -> BetaBiomeSupplier.CODEC);

    /**
     * Chunk generator Deferred Register.
     */
    public static DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MementoBeta.MODID);
    /**
     * Chunk generator.
     */
    public static final RegistryObject<Codec<? extends ChunkGenerator>> BETA_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("beta_chunk_generator", () -> BetaChunkGenerator.CODEC);
}
