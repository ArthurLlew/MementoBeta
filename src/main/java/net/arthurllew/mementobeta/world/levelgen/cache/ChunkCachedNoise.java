package net.arthurllew.mementobeta.world.levelgen.cache;

import net.arthurllew.mementobeta.world.biome.BetaClimate;
import net.arthurllew.mementobeta.world.levelgen.BetaChunkGenerator;
import oshi.util.tuples.Pair;

public class ChunkCachedNoise {
    /**
     * Chunk terrain noise.
     */
    private final double[] terrainNoise;

    /**
     * Constructor.
     * @param chunkX chunk X coordinate
     * @param chunkZ chunk Z coordinate
     * @param betaChunkGenerator chunk generator
     */
    public ChunkCachedNoise(int chunkX, int chunkZ, BetaChunkGenerator betaChunkGenerator) {
        // Those are initialized at the beginning of ChunkProviderGenerate.generateTerrain(...) method.
        byte sizeHorizontal = 4;
        byte sizeVertical = 16;
        int sizeX = sizeHorizontal + 1;
        int sizeY = sizeVertical + 1;
        int sizeZ = sizeHorizontal + 1;

        // Get climate data
        BetaClimate[] climate = betaChunkGenerator.climateCache.get(chunkX, chunkZ).getAll();

        // Sample terrain noise
        this.terrainNoise = betaChunkGenerator.betaTerrainSampler
                .sampleNoise(chunkX * sizeHorizontal, 0, chunkZ * sizeHorizontal,
                        sizeX, sizeY, sizeZ, climate);
    }

    /**
     * Helper constructor.
     * @param coords packed chunk local coordinates
     * @param betaChunkGenerator chunk generator
     */
    public ChunkCachedNoise(Pair<Integer, Integer> coords, BetaChunkGenerator betaChunkGenerator) {
        this(coords.getA(), coords.getB(), betaChunkGenerator);
    }

    /**
     * @return cached terrain noise
     */
    public double[] getTerrainNoise() {
        return terrainNoise;
    }
}
