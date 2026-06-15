package net.arthurllew.mementobeta.mixin;

import com.mojang.datafixers.DataFixer;
import net.arthurllew.mementobeta.attachments.data.BetaSeedData;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.arthurllew.mementobeta.world.levelgen.util.BetaSeedHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Modifies {@link ChunkMap} behaviour.
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapInjector {
    /**
     * Injects code into {@link ChunkMap} constructor. Grabs Beta dimension seed from saved extra world data
     * and saves it elsewhere, so {@link ServerLevelInjector#injectGetSeed} won't throw {@link NullPointerException}
     * because of empty {@link ServerChunkCache} where {@link DimensionDataStorage} is stored.
     */
    @Inject(at = @At("HEAD"), method = "<init>")
    private static void newChunkMap(ServerLevel level,
                                    LevelStorageSource.LevelStorageAccess levelStorageAccess,
                                    DataFixer fixerUpper,
                                    StructureTemplateManager structureManager,
                                    Executor dispatcher,
                                    BlockableEventLoop<Runnable> mainThreadExecutor,
                                    LightChunkGetter lightChunk,
                                    ChunkGenerator generator,
                                    ChunkProgressListener progressListener,
                                    ChunkStatusUpdateListener chunkStatusListener,
                                    Supplier<DimensionDataStorage> overworldDataStorage,
                                    int viewDistance,
                                    boolean sync,
                                    CallbackInfo info) {
        // If this instance is related to Beta dimension
        if (level.dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
            // Convert chunk cache to appropriate type
            ServerChunkCache chunkCache = (ServerChunkCache) lightChunk;

            // Get existing seed data or create and then init a new one
            BetaSeedData seedData = chunkCache.getDataStorage()
                    .computeIfAbsent(BetaSeedData.FACTORY, BetaSeedData.ID).initSeed(level.getServer());

            // Save seed data elsewhere
            BetaSeedHolder.setSavedBetaSeedInstance(seedData);
        }
    }
}
