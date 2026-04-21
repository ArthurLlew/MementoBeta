package net.arthurllew.mementobeta.registry;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.world.biome.BetaBiomeSupplier;
import net.arthurllew.mementobeta.world.levelgen.BetaChunkGenerator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class MementoBetaDimension {
    /**
     * Total day cycle time in ticks for this dimension.
     */
    public static final long DAY_CYCLE_TOTAL_TIME = 24000L;

    /**
     * Dimension name.
     */
    public static final String DIMENSION_NAME = "betaworld";
    /**
     * Dimension name resource location.
     */
    public static final ResourceLocation DIMENSION_NAME_RESOURCE_LOCATION =
            ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, DIMENSION_NAME);

    // Dimension resource keys
    public static final ResourceKey<LevelStem> BETA_DIMENSION =
            ResourceKey.create(Registries.LEVEL_STEM, DIMENSION_NAME_RESOURCE_LOCATION);
    public static final ResourceKey<Level> BETA_DIMENSION_LEVEL =
            ResourceKey.create(Registries.DIMENSION, DIMENSION_NAME_RESOURCE_LOCATION);
    public static final ResourceKey<DimensionType> BETA_DIMENSION_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "betaworld_type"));
    public static final ResourceKey<PoiType> POI_TYPE =
            ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,
                    ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "beta_portal"));

    /**
     * POI type Deferred Register.
     */
    public static final DeferredRegister<PoiType> POI =
            DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, MementoBeta.MODID);
    /**
     * Beta portal POI type.
     */
    public static final Supplier<PoiType> BETA_PORTAL =
            POI.register(POI_TYPE.location().getPath(), () -> new PoiType(ImmutableSet
                        .copyOf(MementoBetaBlocks.BETA_PORTAL.get().getStateDefinition().getPossibleStates()),
                    0, 1));

    /**
     * Beta biome source Deferred Register.
     */
    public static DeferredRegister<MapCodec<? extends BiomeSource>> BETA_BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, MementoBeta.MODID);
    /**
     * Beta biome source.
     */
    public static final Supplier<MapCodec<? extends BiomeSource>> BETA_BIOME_SOURCE =
            BETA_BIOME_SOURCES.register("beta_biome_source", () -> BetaBiomeSupplier.CODEC);

    /**
     * Chunk generator Deferred Register.
     */
    public static DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MementoBeta.MODID);
    /**
     * Chunk generator.
     */
    public static final Supplier<MapCodec<? extends ChunkGenerator>> BETA_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("beta_chunk_generator", () -> BetaChunkGenerator.CODEC);
}
