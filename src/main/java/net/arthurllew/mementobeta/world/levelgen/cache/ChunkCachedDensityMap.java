package net.arthurllew.mementobeta.world.levelgen.cache;

import net.arthurllew.mementobeta.world.levelgen.BetaChunkGenerator;
import net.arthurllew.mementobeta.world.levelgen.noise.BetaTerrainDensitySampler;
import oshi.util.tuples.Pair;

public class ChunkCachedDensityMap extends ChunkCachedMap<double[]> {
    /**
     * Chunk density data.
     */
    private final double[][] density = new double[256][128];

    /**
     * Constructor.
     * @param chunkX chunk X coordinate
     * @param chunkZ chunk Z coordinate
     * @param betaChunkGenerator chunk generator
     */
    public ChunkCachedDensityMap(int chunkX, int chunkZ, BetaChunkGenerator betaChunkGenerator) {
        // Those are initialized at the beginning of ChunkProviderGenerate.generateTerrain(...) method.
        byte sizeHorizontal = 4;
        byte sizeVertical = 16;
        int sizeY = sizeVertical + 1;
        int sizeZ = sizeHorizontal + 1;

        // Get terrain noise
        double[] terrainNoise = betaChunkGenerator.terrainNoiseCache.get(chunkX, chunkZ).getTerrainNoise();

        // Sample density
        for(int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                this.density[this.getIndex(localX, localZ)] = BetaTerrainDensitySampler
                        .sampleDensityColumn(localX, localZ, terrainNoise, sizeY, sizeZ);
            }
        }
    }

    /**
     * Helper constructor.
     * @param coords packed chunk local coordinates
     * @param betaChunkGenerator chunk generator
     */
    public ChunkCachedDensityMap(Pair<Integer, Integer> coords, BetaChunkGenerator betaChunkGenerator) {
        this(coords.getA(), coords.getB(), betaChunkGenerator);
    }

    /**
     * {@inheritDoc}
     */
    public double[] get(int idx) {
        return this.density[idx];
    }

    /**
     * {@inheritDoc}
     */
    public double[][] getAll() {
        return this.density;
    }
}
