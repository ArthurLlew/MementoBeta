package net.arthurllew.mementobeta.world.levelgen.noise;

import net.arthurllew.mementobeta.world.biome.BetaClimate;

import java.util.Random;

/**
 * Beta 1.7.3 terrain sampler.
 */
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

    // Helper noises
    private double[] mainNoise;
    private double[] minLimitNoise;
    private double[] maxLimitNoise;
    private double[] scaleNoise;
    private double[] depthNoise;

    /**
     * Constructor.
     *
     * @param seed world seed
     */
    public BetaTerrainNoiseSampler(long seed) {
        // Init octave noises
        Random rand = new Random(seed);
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
        this.scaleNoise = this.scaleOctaveNoise.sampleXZ(this.scaleNoise, x, z, sizeX, sizeZ,
                1.121D, 1.121D);
        this.depthNoise = this.depthOctaveNoise.sampleXZ(this.depthNoise, x, z, sizeX, sizeZ,
                200.0D, 200.0D);
        this.mainNoise = this.mainOctaveNoise.sampleXYZ(this.mainNoise, x, y, z,
                sizeX, sizeY, sizeZ,
                scaleX / 80.0D,
                scaleY / 160.0D,
                scaleX / 80.0D);
        this.minLimitNoise = this.minLimitOctaveNoise.sampleXYZ(this.minLimitNoise, x, y, z,
                sizeX, sizeY, sizeZ, scaleX, scaleY, scaleX);
        this.maxLimitNoise = this.maxLimitOctaveNoise.sampleXYZ(this.maxLimitNoise, x, y, z,
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

                double scale = (this.scaleNoise[noiseIndex2] + 256.0D) / 512.0D;
                scale *= humidity;
                if(scale > 1.0D) {
                    scale = 1.0D;
                }

                double depth = this.depthNoise[noiseIndex2] / 8000.0D;
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

                    double minLimitNoise = this.minLimitNoise[noiseIndex1] / 512.0D;
                    double maxLimitNoise = this.maxLimitNoise[noiseIndex1] / 512.0D;
                    double mainNoise = (this.mainNoise[noiseIndex1] / 10.0D + 1.0D) / 2.0D;

                    double density;
                    if(mainNoise < 0.0D) {
                        density = minLimitNoise;
                    } else if(mainNoise > 1.0D) {
                        density = maxLimitNoise;
                    } else {
                        density = minLimitNoise + (maxLimitNoise - minLimitNoise) * mainNoise;
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
}
