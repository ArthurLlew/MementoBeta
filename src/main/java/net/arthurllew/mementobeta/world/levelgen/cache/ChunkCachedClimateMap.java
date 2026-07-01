package net.arthurllew.mementobeta.world.levelgen.cache;

import net.arthurllew.mementobeta.world.biome.BetaClimate;
import net.arthurllew.mementobeta.world.levelgen.BetaChunkGenerator;
import net.minecraft.core.SectionPos;
import oshi.util.tuples.Pair;

public class ChunkCachedClimateMap extends ChunkCachedMap<BetaClimate> {
    /**
     * Chunk climate data.
     */
    private final BetaClimate[] climate;

    /**
     * Constructor.
     * @param chunkX chunk X coordinate
     * @param chunkZ chunk Z coordinate
     * @param betaChunkGenerator chunk generator
     */
    public ChunkCachedClimateMap(int chunkX, int chunkZ, BetaChunkGenerator betaChunkGenerator) {
        this.climate = betaChunkGenerator.betaClimateSampler
                .sample(SectionPos.sectionToBlockCoord(chunkX),
                        SectionPos.sectionToBlockCoord(chunkZ),
                        16, 16);
    }

    /**
     * Helper constructor.
     * @param coords packed chunk local coordinates
     * @param betaChunkGenerator chunk generator
     */
    public ChunkCachedClimateMap(Pair<Integer, Integer> coords, BetaChunkGenerator betaChunkGenerator) {
        this(coords.getA(), coords.getB(), betaChunkGenerator);
    }

    /**
     * {@inheritDoc}
     */
    public BetaClimate get(int idx) {
        return this.climate[idx];
    }

    /**
     * {@inheritDoc}
     */
    public BetaClimate[] getAll() {
        return this.climate;
    }
}
