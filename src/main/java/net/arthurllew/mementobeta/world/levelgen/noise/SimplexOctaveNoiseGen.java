package net.arthurllew.mementobeta.world.levelgen.noise;

import java.util.Random;

public class SimplexOctaveNoiseGen {
    /**
     * Octaves array.
     */
    private final SimplexNoiseGen[] octaves;
    /**
     * Number of octaves.
     */
    private final int octaveCount;

    /**
     * Constructor.
     * @param random random source
     * @param octavesCount number of octaves
     */
    public SimplexOctaveNoiseGen(Random random, int octavesCount) {
        this.octaveCount = octavesCount;

        // Init octaves array
        this.octaves = new SimplexNoiseGen[octavesCount];
        for(int var3 = 0; var3 < octavesCount; ++var3) {
            this.octaves[var3] = new SimplexNoiseGen(random);
        }
    }

    /**
     * Samples Simplex XZ noise.
     * @param x block X coordinate
     * @param z block Z coordinate
     * @param scaleX noise X scale
     * @param scaleZ noise Z scale
     * @param lacunarity lacunarity
     * @return sampled noise
     */
    public double sampleXZ(double x, double z, double scaleX, double scaleZ, double lacunarity) {
        return this.sampleXZ(x, z, scaleX, scaleZ, lacunarity, 0.5);
    }

    /**
     * Samples Simplex XZ noise.
     * @param x block X coordinate
     * @param z block Z coordinate
     * @param scaleX noise X scale
     * @param scaleZ noise Z scale
     * @param lacunarity lacunarity
     * @param persistence persistence
     * @return sampled noise
     */
    public double sampleXZ(double x, double z, double scaleX, double scaleZ,
                           double lacunarity, double persistence) {
        // Modify scale
        scaleX /= 1.5D;
        scaleZ /= 1.5D;

        // Initial values
        double noise = 0.0;
        double amplitude = 1.0D;
        double frequency = 1.0D;

        // Iterate over octaves
        for(int i = 0; i < this.octaveCount; ++i) {
            noise += this.octaves[i].sampleXZ(x, z, scaleX * frequency, scaleZ * frequency,
                    0.55D / amplitude);
            // Update values
            frequency *= lacunarity;
            amplitude *= persistence;
        }

        // Provide noise
        return noise;
    }
}
