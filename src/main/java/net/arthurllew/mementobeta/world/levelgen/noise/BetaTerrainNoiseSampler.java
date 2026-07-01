package net.arthurllew.mementobeta.world.levelgen.noise;

import net.arthurllew.mementobeta.world.biome.BetaClimate;

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

        double scaleX = 684.412D;
        double scaleY = 684.412D;
        double[] scaleNoise = this.scaleOctaveNoise.sampleXZ(null, x, z, sizeX, sizeZ,
                1.121D, 1.121D);
        double[] depthNoise = this.depthOctaveNoise.sampleXZ(null, x, z, sizeX, sizeZ,
                200.0D, 200.0D);
        double[] mainNoise = this.mainOctaveNoise.sampleXYZ(null, x, y, z,
                sizeX, sizeY, sizeZ,
                scaleX / 80.0D,
                scaleY / 160.0D,
                scaleX / 80.0D);
        double[] minLimitNoise = this.minLimitOctaveNoise.sampleXYZ(null, x, y, z,
                sizeX, sizeY, sizeZ, scaleX, scaleY, scaleX);
        double[] maxLimitNoise = this.maxLimitOctaveNoise.sampleXYZ(null, x, y, z,
                sizeX, sizeY, sizeZ, scaleX, scaleY, scaleX);

        int noiseIndex1 = 0;
        int noiseIndex2 = 0;
        int sizeDifference = 16 / sizeX;

        for(int localX = 0; localX < sizeX; ++localX) {
            int shiftedLocalX = localX * sizeDifference + sizeDifference / 2;

            for(int localZ = 0; localZ < sizeZ; ++localZ) {
                int shiftedLocalZ = localZ * sizeDifference + sizeDifference / 2;

                double temperature = climate[shiftedLocalX * 16 + shiftedLocalZ].temperature();
                double humidity = climate[shiftedLocalX * 16 + shiftedLocalZ].humidity() * temperature;

                humidity = 1.0D - humidity;
                humidity *= humidity;
                humidity *= humidity;
                humidity = 1.0D - humidity;

                double scale = (scaleNoise[noiseIndex2] + 256.0D) / 512.0D;
                scale *= humidity;
                if(scale > 1.0D) {
                    scale = 1.0D;
                }

                double depth = depthNoise[noiseIndex2] / 8000.0D;
                if(depth < 0.0D) {
                    depth = -depth * 0.3D;
                }

                depth = depth * 3.0D - 2.0D;
                if(depth < 0.0D) {
                    depth /= 2.0D;
                    if(depth < -1.0D) {
                        depth = -1.0D;
                    }

                    depth /= 1.4D;
                    depth /= 2.0D;
                    scale = 0.0D;
                } else {
                    if(depth > 1.0D) {
                        depth = 1.0D;
                    }

                    depth /= 8.0D;
                }

                if(scale < 0.0D) {
                    scale = 0.0D;
                }

                scale += 0.5D;
                depth = depth * (double)sizeY / 16.0D;
                double var31 = (double)sizeY / 2.0D + depth * 4.0D;
                ++noiseIndex2;

                for(int NoiseY = 0; NoiseY < sizeY; ++NoiseY) {
                    double densityOffset = ((double)NoiseY - var31) * 12.0D / scale;
                    if(densityOffset < 0.0D) {
                        densityOffset *= 4.0D;
                    }

                    double minLimit = minLimitNoise[noiseIndex1] / 512.0D;
                    double maxLimit = maxLimitNoise[noiseIndex1] / 512.0D;
                    double main = (mainNoise[noiseIndex1] / 10.0D + 1.0D) / 2.0D;

                    double density;
                    if(main < 0.0D) {
                        density = minLimit;
                    } else if(main > 1.0D) {
                        density = maxLimit;
                    } else {
                        density = minLimit + (maxLimit - minLimit) * main;
                    }

                    density -= densityOffset;
                    if(NoiseY > sizeY - 4) {
                        double var44 = (float)(NoiseY - (sizeY - 4)) / 3.0F;
                        density = density * (1.0D - var44) + -10.0D * var44;
                    }

                    noise[noiseIndex1] = density;
                    ++noiseIndex1;
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
        return this.beachOctaveNoise.sampleXYZ(null, x, y, z, sizeX, sizeY, sizeZ, scaleX, scaleY, scaleZ);
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
        return this.surfaceOctaveNoise.sampleXYZ(null, x, y, z, sizeX, sizeY, sizeZ, scaleX, scaleY, scaleZ);
    }
}
