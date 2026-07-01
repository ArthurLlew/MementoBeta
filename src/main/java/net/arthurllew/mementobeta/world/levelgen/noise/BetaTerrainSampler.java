package net.arthurllew.mementobeta.world.levelgen.noise;

import net.arthurllew.mementobeta.world.biome.BetaClimate;
import net.minecraft.util.Mth;

import java.util.Random;

public class BetaTerrainSampler {
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
    public BetaTerrainSampler(long seed) {
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

    /**
     * Samples density.
     * @param localX chunk local X [0,15]
     * @param localY chunk local Y [0,127]
     * @param localZ chunk local Z [0,15]
     * @param terrainNoise terrain coarse noise
     * @param sizeY noise Y size
     * @param sizeZ noise Z size
     * @return sampled density
     */
    public static double sampleDensity(int localX, int localY, int localZ,
                                       double[] terrainNoise, int sizeY, int sizeZ) {
        // 4x8x4 noise grid cell coordinates
        int iX = localX >> 2;
        int iY = localY >> 3;
        int iZ = localZ >> 2;

        // Interpolation weights (0.0 to 1.0) inside the cell
        double fadeX = (localX & 3) * 0.25;
        double fadeY = (localY & 7) * 0.125;
        double fadeZ = (localZ & 3) * 0.25;

        // Use already existing 3D linear interpolations
        return Mth.lerp3(
                fadeX, fadeY, fadeZ,
                terrainNoise[getNoiseFlatIndex(iX, iY, iZ, sizeY, sizeZ)],
                terrainNoise[getNoiseFlatIndex(iX + 1, iY, iZ, sizeY, sizeZ)],
                terrainNoise[getNoiseFlatIndex(iX, iY + 1, iZ, sizeY, sizeZ)],
                terrainNoise[getNoiseFlatIndex(iX + 1, iY + 1, iZ, sizeY, sizeZ)],
                terrainNoise[getNoiseFlatIndex(iX, iY, iZ + 1, sizeY, sizeZ)],
                terrainNoise[getNoiseFlatIndex(iX + 1, iY, iZ + 1, sizeY, sizeZ)],
                terrainNoise[getNoiseFlatIndex(iX, iY + 1, iZ + 1, sizeY, sizeZ)],
                terrainNoise[getNoiseFlatIndex(iX + 1, iY + 1, iZ + 1, sizeY, sizeZ)]
        );
    }

    /**
     * Samples density column.
     * @param localX chunk local X [0,15]
     * @param localZ chunk local Z [0,15]
     * @param height density column size
     * @param terrainNoise terrain coarse noise
     * @param sizeY noise Y size
     * @param sizeZ noise Z size
     * @return sampled density column
     */
    public static double[] sampleDensityColumn(int localX, int localZ, int height,
                                               double[] terrainNoise, int sizeY, int sizeZ) {
        // 4x8x4 noise grid cell coordinates
        int iX = localX >> 2;
        int iZ = localZ >> 2;

        // Interpolation weights (0.0 to 1.0) inside the cell
        double fadeX = (localX & 3) * 0.250;
        double fadeZ = (localZ & 3) * 0.250;

        // Helper noise arrays
        double[] lowYNoise = new double[sizeY];
        double[] highYNoise = new double[sizeY];

        // Fill helper arrays with noise interpolation
        for (int iY = 0; iY < sizeY - 1; iY++) {
            lowYNoise[iY] = Mth.lerp2(
                    fadeX, fadeZ,
                    terrainNoise[getNoiseFlatIndex(iX, iY, iZ, sizeY, sizeZ)],
                    terrainNoise[getNoiseFlatIndex(iX + 1, iY, iZ, sizeY, sizeZ)],
                    terrainNoise[getNoiseFlatIndex(iX, iY, iZ + 1, sizeY, sizeZ)],
                    terrainNoise[getNoiseFlatIndex(iX + 1, iY, iZ + 1, sizeY, sizeZ)]
            );
            highYNoise[iY] = Mth.lerp2(
                    fadeX, fadeZ,
                    terrainNoise[getNoiseFlatIndex(iX, iY + 1, iZ, sizeY, sizeZ)],
                    terrainNoise[getNoiseFlatIndex(iX + 1, iY + 1, iZ, sizeY, sizeZ)],
                    terrainNoise[getNoiseFlatIndex(iX, iY + 1, iZ + 1, sizeY, sizeZ)],
                    terrainNoise[getNoiseFlatIndex(iX + 1, iY + 1, iZ + 1, sizeY, sizeZ)]
            );
        }

        // Fill in and return density column
        double[] density = new double[height];
        for (int localY = 0; localY < height; localY++) {
            // Coarse 4x8x4 noise grid cell coordinate
            int iY = localY >> 3;
            // Interpolation weight (0.0 to 1.0) inside the cell
            double fadeY = (localY & 7) * 0.125;

            density[localY] = Mth.lerp(fadeY, lowYNoise[iY], highYNoise[iY]);
        }
        return density;
    }

    /**
     * Samples density column.
     * @param localX chunk local X [0,15]
     * @param localZ chunk local Z [0,15]
     * @param terrainNoise terrain coarse noise
     * @param sizeY noise Y size
     * @param sizeZ noise Z size
     * @return sampled density column
     */
    public static double[] sampleDensityColumn(int localX, int localZ, double[] terrainNoise, int sizeY, int sizeZ) {
        return sampleDensityColumn(localX, localZ, 128, terrainNoise, sizeY, sizeZ);
    }

    /**
     * Helper to calculate flat array index from 3D grid coordinates.
     * @param iX array X index [0,sizeX-1]
     * @param iY array Y index [0,sizeY-1]
     * @param iZ array Z index [0,sizeZ-1]
     * @param sizeY array Y size
     * @param sizeZ array Z size
     * @return array index
     */
    private static int getNoiseFlatIndex(int iX, int iY, int iZ, int sizeY, int sizeZ) {
        return (iX * sizeZ + iZ) * sizeY + iY;
    }
}
