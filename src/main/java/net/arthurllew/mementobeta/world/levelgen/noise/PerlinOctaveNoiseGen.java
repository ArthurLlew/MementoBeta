package net.arthurllew.mementobeta.world.levelgen.noise;

import java.util.Random;

public class PerlinOctaveNoiseGen {
    /**
     * Octaves array.
     */
    private final PerlinNoiseGen[] octaves;
    /**
     * Number of octaves.
     */
    private final int octavesCount;

    /**
     * Constructor.
     * @param random random source
     * @param octavesCount number of octaves
     */
    public PerlinOctaveNoiseGen(Random random, int octavesCount) {
        this.octavesCount = octavesCount;

        // Init octaves array
        this.octaves = new PerlinNoiseGen[octavesCount];
        for(int i = 0; i < octavesCount; ++i) {
            this.octaves[i] = new PerlinNoiseGen(random);
        }

    }

    /**
     * Samples XZ noise.
     * @param x block X coordinate
     * @param z block Z coordinate
     * @param sizeX noise array X size
     * @param sizeZ noise array Z size
     * @param scaleX noise X scale
     * @param scaleZ noise Z scale
     * @return sampled noise
     */
    public double[] sampleXZ(double x, double z, int sizeX, int sizeZ, double scaleX, double scaleZ) {
        return this.sampleXYZ(x, 10.0D, z, sizeX, 1, sizeZ, scaleX, 1.0D, scaleZ);
    }

    /**
     * Samples XYZ noise.
     * @param x block X coordinate
     * @param y block Y coordinate
     * @param z block Z coordinate
     * @param sizeX noise array X size
     * @param sizeY noise array Y size
     * @param sizeZ noise array Z size
     * @param scaleX noise X scale
     * @param scaleY noise Y scale
     * @param scaleZ noise Z scale
     * @return sampled noise
     */
    public double[] sampleXYZ(double x, double y, double z, int sizeX, int sizeY, int sizeZ,
                              double scaleX, double scaleY, double scaleZ) {
        // Init array
        double[] noise = new double[sizeX * sizeY * sizeZ];

        // Starting frequency
        double frequency = 1.0D;
        // Iterate over octaves
        for(int i = 0; i < this.octavesCount; ++i) {
            // Sample noise
            this.octaves[i].sample(noise, x, y, z, sizeX, sizeY, sizeZ,
                    scaleX * frequency, scaleY * frequency, scaleZ * frequency, frequency);
            // Update frequency
            frequency /= 2.0D;
        }

        // Provide noise
        return noise;
    }
}
