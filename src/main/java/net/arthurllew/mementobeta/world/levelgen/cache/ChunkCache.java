package net.arthurllew.mementobeta.world.levelgen.cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.arthurllew.mementobeta.world.levelgen.BetaChunkGenerator;
import net.minecraft.world.level.ChunkPos;
import oshi.util.tuples.Pair;

import java.util.function.BiFunction;

/**
 * Thread-safe chunk generation data cache. Caches climate, terrain noise and heightmap.
 */
public class ChunkCache<T> {
    /**
     * Cache capacity.
     */
    private final int capacity;
    /**
     * Cache.
     */
    private final Long2ObjectLinkedOpenHashMap<T> chunkMap;
    /**
     * New element factory.
     */
    protected final BiFunction<? super Pair<Integer, Integer>, ? super BetaChunkGenerator, ? extends T> factory;
    /**
     * Related chunk generator.
     */
    private final BetaChunkGenerator betaChunkGenerator;

    /**
     * Constructor.
     *
     * @param betaChunkGenerator related chunk generator
     */
    public ChunkCache(BiFunction<? super Pair<Integer, Integer>,
                              ? super BetaChunkGenerator,
                              ? extends T> factory,
                      BetaChunkGenerator betaChunkGenerator) {
        this(512, factory, betaChunkGenerator);
    }

    /**
     * Constructor.
     *
     * @param capacity cache capacity
     * @param betaChunkGenerator related chunk generator
     */
    private ChunkCache(int capacity,
                       BiFunction<? super Pair<Integer, Integer>,
                               ? super BetaChunkGenerator,
                               ? extends T> factory,
                       BetaChunkGenerator betaChunkGenerator) {
        this.capacity = capacity;
        this.chunkMap = new Long2ObjectLinkedOpenHashMap<>(capacity);
        this.factory = factory;
        this.betaChunkGenerator = betaChunkGenerator;
    }

    /**
     * @param chunkX chunk X position
     * @param chunkZ chunk Z position
     *
     * @return cached data
     */
    public T get(int chunkX, int chunkZ) {
        T data;
        long key = ChunkPos.asLong(chunkX, chunkZ);

        // Return existing data or generate a new one
        return ((data = this.chunkMap.get(key)) != null) ? data : set(key, chunkX, chunkZ);
    }

    /**
     * Thread-safe method for inserting new data into cache.
     *
     * @param key cache key
     * @param chunkX chunk X position
     * @param chunkZ chunk Z position
     *
     * @return newly created data
     */
    private synchronized T set(long key, int chunkX, int chunkZ) {
        T data;

        // If previous thread have not accidentally created data for us
        if ((data = this.chunkMap.get(key)) == null) {
            // Cache size must remain below capacity
            if (this.chunkMap.size() >= this.capacity) {
                this.chunkMap.removeFirst();
            }

            data = factory.apply(new Pair<>(chunkX, chunkZ), betaChunkGenerator);
            // Put data
            this.chunkMap.put(key, data);
        }

        return data;
    }
}
