package net.arthurllew.mementobeta.world.levelgen.noise;

import net.arthurllew.mementobeta.world.biome.BetaClimate;
import net.minecraft.util.Mth;

import java.util.Random;

public class BetaTerrainNoiseSampler {
    // Noise generators
    public PerlinOctaveNoiseGen minLimitOctaveNoise;
    public PerlinOctaveNoiseGen maxLimitOctaveNoise;
    public PerlinOctaveNoiseGen mainOctaveNoise;
    public PerlinOctaveNoiseGen beachOctaveNoise;
    public PerlinOctaveNoiseGen surfaceOctaveNoise;
    public PerlinOctaveNoiseGen scaleOctaveNoise;
    public PerlinOctaveNoiseGen depthOctaveNoise;
    public PerlinOctaveNoiseGen forestOctaveNoise;

    /**
     * Constructor.
     * @param seed world seed
     */
    public BetaTerrainNoiseSampler(long seed) {
        // Init octave noises
        Random rand = new Random(seed);
        // Oder of declaration matters because the random is used inside sequentially
        this.minLimitOctaveNoise = new PerlinOctaveNoiseGen(rand, 16);
        this.maxLimitOctaveNoise = new PerlinOctaveNoiseGen(rand, 16);
        this.mainOctaveNoise = new PerlinOctaveNoiseGen(rand, 8);
        this.beachOctaveNoise = new PerlinOctaveNoiseGen(rand, 4);
        this.surfaceOctaveNoise = new PerlinOctaveNoiseGen(rand, 4);
        this.scaleOctaveNoise = new PerlinOctaveNoiseGen(rand, 10);
        this.depthOctaveNoise = new PerlinOctaveNoiseGen(rand, 16);
        this.forestOctaveNoise = new PerlinOctaveNoiseGen(rand, 8);
    }

    /**
     * Generates Beta 1.7.3 terrain noise.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param sizeX noise X size
     * @param sizeY noise Y size
     * @param sizeZ noise Z size
     * @param climate climate
     * @return sampled noise
     */
    public double[] sampleNoise(int x, int y, int z, int sizeX, int sizeY, int sizeZ, BetaClimate[] climate) {
        double[] noise = new double[sizeX * sizeY * sizeZ];

        // Noise scales
        double scaleX = 684.412D;
        double scaleY = 684.412D;

        // Sample noise octaves
        double[] scaleNoise = this.scaleOctaveNoise.sampleXZ(x, z, sizeX, sizeZ,
                1.121D, 1.121D);
        double[] depthNoise = this.depthOctaveNoise.sampleXZ(x, z, sizeX, sizeZ,
                200.0D, 200.0D);
        double[] mainNoise = this.mainOctaveNoise.sampleXYZ(x, y, z, sizeX, sizeY, sizeZ,
                        scaleX / 80.0D, scaleY / 160.0D, scaleX / 80.0D);
        double[] minLimitNoise = this.minLimitOctaveNoise.sampleXYZ(x, y, z, sizeX, sizeY, sizeZ,
                scaleX, scaleY, scaleX);
        double[] maxLimitNoise = this.maxLimitOctaveNoise.sampleXYZ(x, y, z, sizeX, sizeY, sizeZ,
                scaleX, scaleY, scaleX);

        // Helper value
        int sizeDifference = 16 / sizeX;

        // Iterate X
        int noiseXZIndex = 0;
        int noiseXYZIndex = 0;
        for (int iX = 0; iX < sizeX; ++iX) {
            int localX = iX * sizeDifference + sizeDifference / 2;

            // Iterate Y
            for (int iZ = 0; iZ < sizeZ; ++iZ) {
                int localZ = iZ * sizeDifference + sizeDifference / 2;

                // Fetch climate settings
                BetaClimate currentClimate = climate[localX * 16 + localZ];
                double temperature = currentClimate.temperature();
                double humidity = currentClimate.humidity();

                // Calculate biome influence factor
                double humidityInfluence = 1.0D - humidity * temperature;
                humidityInfluence *= humidityInfluence;
                humidityInfluence *= humidityInfluence;
                humidityInfluence = 1.0D - humidityInfluence;

                // Scale influence from climate
                double scale = (scaleNoise[noiseXZIndex] + 256.0D) / 512.0D;
                scale = Math.min(scale * humidityInfluence, 1.0D);

                // Terrain depth offset
                double rawDepth = depthNoise[noiseXZIndex] / 8000.0D;
                if (rawDepth < 0.0D) {
                    rawDepth = -rawDepth * 0.3D;
                }

                // Terrain depth
                double depth = rawDepth * 3.0D - 2.0D;
                if (depth < 0.0D) {
                    depth = Math.max(depth / 2.0D, -1.0D) / 2.8D;
                    scale = 0.0D;
                } else {
                    depth = Math.min(depth, 1.0D) / 8.0D;
                }

                // Update scale
                scale = Math.max(scale, 0.0D);
                scale += 0.5D;

                // Pre-calculate values used in height falloff
                depth = depth * (double) sizeY / 16.0D;
                double baseHeightOffset = (double) sizeY / 2.0D + depth * 4.0D;

                // Update XZ index
                ++noiseXZIndex;

                // Iterate column
                for (int iY = 0; iY < sizeY; ++iY) {
                    // Noise limits
                    double minLimit = minLimitNoise[noiseXYZIndex] / 512.0D;
                    double maxLimit = maxLimitNoise[noiseXYZIndex] / 512.0D;

                    // Main noise acts as a weight/selector between min and max limits
                    double main = (mainNoise[noiseXYZIndex] / 10.0D + 1.0D) / 2.0D;

                    // Linearly interpolate clamped noise between min and max limits
                    double density = Mth.lerp(Math.clamp(main, 0.0D, 1.0D), minLimit, maxLimit);

                    // Apply height falloff
                    double densityOffset = ((double) iY - baseHeightOffset) * 12.0D / scale;
                    if (densityOffset < 0.0D) {
                        densityOffset *= 4.0D;
                    }
                    density -= densityOffset;

                    // Fade out terrain density near the sky boundary (top 4 blocks of the sub-chunk)
                    if (iY > sizeY - 4) {
                        double upperBoundaryFadeFactor = (float) (iY - (sizeY - 4)) / 3.0F;
                        density = density * (1.0D - upperBoundaryFadeFactor) + -10.0D * upperBoundaryFadeFactor;
                    }

                    // Save noise value
                    noise[noiseXYZIndex] = density;
                    ++noiseXYZIndex;
                }
            }
        }

        return noise;
    }

    /**
     * Samples Beta 1.7.3 beach noise.
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
    public double[] sampleBeachNoise(double x, double y, double z, int sizeX, int sizeY, int sizeZ,
                                     double scaleX, double scaleY, double scaleZ) {
        return this.beachOctaveNoise.sampleXYZ(x, y, z, sizeX, sizeY, sizeZ, scaleX, scaleY, scaleZ);
    }

    /**
     * Samples Beta 1.7.3 surface noise.
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
    public double[] sampleSurfaceNoise(double x, double y, double z, int sizeX, int sizeY, int sizeZ,
                                       double scaleX, double scaleY, double scaleZ) {
        return this.surfaceOctaveNoise.sampleXYZ(x, y, z, sizeX, sizeY, sizeZ, scaleX, scaleY, scaleZ);
    }
}
