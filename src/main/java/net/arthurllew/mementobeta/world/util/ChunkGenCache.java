package net.arthurllew.mementobeta.world.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.arthurllew.mementobeta.world.BetaChunkGenerator;
import net.arthurllew.mementobeta.world.biome.BetaClimate;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

/**
 * Thread-safe chunk generation data cache. Caches climate, terrain noise and heightmap.
 */
public class ChunkGenCache {
    /**
     * Cache capacity.
     */
    private final int capacity;
    /**
     * Cache.
     */
    private final Long2ObjectLinkedOpenHashMap<GenData> chunkMap;
    /**
     * Related chunk generator.
     */
    private final BetaChunkGenerator betaChunkGenerator;

    /**
     * Constructor.
     * @param betaChunkGenerator related chunk generator.
     */
    public ChunkGenCache(BetaChunkGenerator betaChunkGenerator) {
        this(512, betaChunkGenerator);
    }

    /**
     * Constructor.
     * @param capacity cache capacity.
     * @param betaChunkGenerator related chunk generator.
     */
    private ChunkGenCache(int capacity, BetaChunkGenerator betaChunkGenerator) {
        this.capacity = capacity;
        this.chunkMap = new Long2ObjectLinkedOpenHashMap<>(capacity);
        this.betaChunkGenerator = betaChunkGenerator;
    }

    /**
     * @param chunkX chunk X position.
     * @param chunkZ chunk Z position.
     * @return cached data.
     */
    public GenData get(int chunkX, int chunkZ) {
        GenData data;
        long key = ChunkPos.asLong(chunkX, chunkZ);

        // Return existing data or generate a new one
        return ((data = this.chunkMap.get(key)) != null) ? data : set(key, chunkX, chunkZ);
    }

    /**
     * Thread-safe method for inserting new data into cache.
     * @param key cache key.
     * @param chunkX chunk X position.
     * @param chunkZ chunk Z position.
     * @return newly created data.
     */
    private synchronized GenData set(long key, int chunkX, int chunkZ) {
        GenData data;

        // If previous thread have not accidentally created data for us
        if ((data = this.chunkMap.get(key)) == null) {
            // Cache size must remain below capacity
            if (this.chunkMap.size() >= this.capacity) {
                this.chunkMap.removeFirst();
            }

            data = GenData.create(chunkX, chunkZ, betaChunkGenerator);
            // Put data
            this.chunkMap.put(key, data);
        }

        return data;
    }

    /**
     * Cached generation data. Stores climate, terrain noise and heightmap.
     */
    public record GenData(BetaClimate[] climate, double[] terrainNoise, Heightmap heightmap) {
        /**
         * @param chunkX chunk X.
         * @param chunkZ chunk Y.
         * @param betaChunkGenerator chunk generator.
         * @return generation data.
         */
        private static GenData create(int chunkX, int chunkZ, BetaChunkGenerator betaChunkGenerator) {
            // In Vanilla Beta 1.7.3 temperature and humidity both are used right of the bat,
            // so biomes are generated first by WorldChunkManager.generateBiomeInfo(...) method.
            BetaClimate[] climate =
                    betaChunkGenerator.betaClimateSampler.sample(chunkX * 16, chunkZ * 16, 16, 16);

            // Those are initialized at the beginning of ChunkProviderGenerate.generateTerrain(...) method.
            byte sizeHorizontal = 4;
            byte sizeVertical = 16;
            int sizeX = sizeHorizontal + 1;
            int sizeY = sizeVertical + 1;
            int sizeZ = sizeHorizontal + 1;

            // Generate terrain noise
            double[] terrainNoise = betaChunkGenerator.betaTerrainSampler
                    .sampleNoise(chunkX * sizeHorizontal, 0, chunkZ * sizeHorizontal,
                        sizeX, sizeY, sizeZ, climate);

            // Fill heightmap
            Heightmap heightmap = new Heightmap();
            betaChunkGenerator.betaTerrainSampler.sampleTerrain(terrainNoise,
                    betaChunkGenerator.generatorSettings().value().seaLevel(),
                    heightmap::update);

            return new GenData(climate, terrainNoise, heightmap);
        }

        /**
         * Custom heightmap.
         */
        public static class Heightmap {
            /**
             * Chunk size heightmap.
             */
            private final byte[] data = new byte[256];

            /**
             * Constructor fills heightmap with 0, because in beta min height is 0.
             */
            public Heightmap() {
                Arrays.fill(data, (byte)0);
            }

            /**
             * Updates heightmap.
             * @param x chunk local X position
             * @param y chunk local Y position.
             * @param z chunk local Z position.
             * @param block block.
             */
            public void update(int x, int y, int z, BlockState block) {
                int i = getIndex(x, z);
                int height = this.data[i];

                if (y >= height && block.blocksMotion()) {
                    this.data[i] = (byte)(y + 1);
                }
            }

            /**
             * @param x chunk local X position
             * @param z chunk local Z position.
             * @return height at given coordinates.
             */
            public int getHeight(int x, int z) {
                // If the assigned value was > 127 than the byte value will be negative,
                // thus logical "and" with value
                // 0000 0000 0000 0000 0000 0000 1111 1111 (the 0xFF)
                // is required.
                return this.data[getIndex(x, z) & 0xFF];
            }

            /**
             * @param x chunk local X position
             * @param z chunk local Z position.
             * @return array index.
             */
            private static int getIndex(int x, int z) {
                return x + z * 16;
            }
        }
    }
}
