package net.arthurllew.mementobeta.world.levelgen.noise;

import net.minecraft.util.Mth;

import java.util.Random;

/**
 * Notch clearly used earlier implementation by Ken Perlin, because most functions match identical when decompiling
 * Beta 1.7.3 Vanilla version. All the things going on below are carefully explained here
 * <a href="https://adrianb.io/2014/08/09/perlinnoise.html">https://adrianb.io/2014/08/09/perlinnoise.html</a>.
 * The original code behaviour cannot be replicated with newer Minecraft classes.
 * Thus, this class uses the original code with some minor modifications.
 */
public class PerlinNoiseGen extends NoiseGen {
    /**
     * Constructor.
     * @param random random source
     */
    public PerlinNoiseGen(Random random) {
        super(random);
    }

    /**
     * Samples Beta 1.7.3 Perlin noise.
     * @param noise noise array
     * @param x block X coordinate
     * @param y block Y coordinate
     * @param z block Z coordinate
     * @param sizeX noise array X size
     * @param sizeY noise array Y size
     * @param sizeZ noise array Z size
     * @param scaleX noise X scale
     * @param scaleY noise Y scale
     * @param scaleZ noise Z scale
     * @param frequency noise frequency
     */
    public void sample(double[] noise, double x, double y, double z, int sizeX, int sizeY, int sizeZ,
                       double scaleX, double scaleY, double scaleZ, double frequency) {
        // 3D case
        if (sizeY != 1) {
            this.sampleXYZ(noise, x, y, z, sizeX, sizeY, sizeZ, scaleX, scaleY, scaleZ, frequency);
        }
        // 2D case
        else {
            // Iterate over noise array
            int i = 0;
            for (int iX = 0; iX < sizeX; iX++) {
                for (int iZ = 0; iZ < sizeZ; iZ++) {
                    // Noise coordinates
                    double noiseX = (x + (double)iX) * scaleX;
                    double noiseZ = (z + (double)iZ) * scaleZ;
                    // Sample noise
                    noise[i++] += this.sampleXZ(noiseX, noiseZ, frequency);
                }
            }
        }
    }

    /**
     * Samples Perlin XYZ noise. Optimized version for noise array.
     * @param noise noise array
     * @param x block X coordinate
     * @param y block Y coordinate
     * @param z block Z coordinate
     * @param sizeX noise array X size
     * @param sizeY noise array Y size
     * @param sizeZ noise array Z size
     * @param scaleX noise X scale
     * @param scaleY noise Y scale
     * @param scaleZ noise Z scale
     */
    public void sampleXYZ(double[] noise, double x, double y, double z, int sizeX, int sizeY, int sizeZ,
                          double scaleX, double scaleY, double scaleZ, double frequency) {
        // Prepare frequency
        frequency = 1.0D / frequency;

        // Cached interpolation values
        double lerp0 = 0.0D;
        double lerp1 = 0.0D;
        double lerp2 = 0.0D;
        double lerp3 = 0.0D;

        // Optimization
        int prevY = -1;
        // Iterate over a collection of noise points
        int idx = 0;
        for (int localX = 0; localX < sizeX; localX++) {
            for (int localZ = 0; localZ < sizeZ; localZ++) {
                for (int localY = 0; localY < sizeY; localY++) {
                    // Noise coordinates + offset
                    double noiseX = (x + (double)localX) * scaleX + this.offsetX;
                    double noiseY = (y + (double)localY) * scaleY + this.offsetY;
                    double noiseZ = (z + (double)localZ) * scaleZ + this.offsetZ;

                    // Get floored noise coordinates
                    int floorX = Mth.floor(noiseX);
                    int floorY = Mth.floor(noiseY);
                    int floorZ = Mth.floor(noiseZ);

                    // Find unit cube that contains point
                    int X = floorX & 0xFF;
                    int Y = floorY & 0xFF;
                    int Z = floorZ & 0xFF;

                    // Find local x, y, z of point in cube
                    noiseX -= floorX;
                    noiseY -= floorY;
                    noiseZ -= floorZ;

                    // Compute fade curves for x, y, z
                    double u = fade(noiseX);
                    double v = fade(noiseY);
                    double w = fade(noiseZ);

                    // Skip already known values
                    if (localY == 0 || Y != prevY) {
                        // Otherwise remember current Y
                        prevY = Y;

                        // Apply
                        int A =  this.permutations[X] + Y;
                        int AA = this.permutations[A] + Z;
                        int AB = this.permutations[A + 1] + Z;
                        int B =  this.permutations[X + 1] + Y;
                        int BA = this.permutations[B] + Z;
                        int BB = this.permutations[B + 1] + Z;

                        // Cache interpolation values
                        lerp0 = lerp(
                                u,
                                grad(this.permutations[AA], noiseX, noiseY, noiseZ),
                                grad(this.permutations[BA], noiseX - 1.0D, noiseY, noiseZ)
                        );
                        lerp1 = lerp(
                                u,
                                grad(this.permutations[AB], noiseX, noiseY - 1.0D, noiseZ),
                                grad(this.permutations[BB], noiseX - 1.0D, noiseY - 1.0D, noiseZ)
                        );
                        lerp2 = lerp(
                                u,
                                grad(this.permutations[AA + 1], noiseX, noiseY, noiseZ - 1.0D),
                                grad(this.permutations[BA + 1], noiseX - 1.0D, noiseY, noiseZ - 1.0D)
                        );
                        lerp3 = lerp(
                                u,
                                grad(this.permutations[AB + 1], noiseX, noiseY - 1.0D, noiseZ - 1.0D),
                                grad(this.permutations[BB + 1], noiseX - 1.0D, noiseY - 1.0D, noiseZ - 1.0D)
                        );
                    }

                    // Sample final noise and apply frequency
                    noise[idx++] += lerp(w, lerp(v, lerp0, lerp1), lerp(v, lerp2, lerp3)) * frequency;
                }
            }
        }
    }

    /**
     * Samples Perlin XYZ noise.
     * @param noiseX noise X coordinate
     * @param noiseY noise X coordinate
     * @param noiseZ noise Z coordinate
     * @param frequency noise frequency
     * @return sampled noise
     */
    @SuppressWarnings("unused")
    private double sampleXYZ(double noiseX, double noiseY, double noiseZ, double frequency) {
        // Offset noise
        noiseX = noiseX + this.offsetX;
        noiseY = noiseY + this.offsetY;
        noiseZ = noiseZ + this.offsetZ;

        // Get floored noise coordinates
        int floorNoiseX = Mth.floor(noiseX);
        int floorNoiseY = Mth.floor(noiseY);
        int floorNoiseZ = Mth.floor(noiseZ);

        // Find unit cube that contains point
        int X = floorNoiseX & 0xFF;
        int Y = floorNoiseY & 0xFF;
        int Z = floorNoiseZ & 0xFF;

        // Find local x, y, z of point in cube
        noiseX -= floorNoiseX;
        noiseY -= floorNoiseY;
        noiseZ -= floorNoiseZ;

        // Compute fade curves for x, y, z
        double fX = fade(noiseX);
        double fY = fade(noiseY);
        double fZ = fade(noiseZ);

        // Apply
        int A = this.permutations[X] + Y;
        int AA = this.permutations[A] + Z;
        int AB = this.permutations[A + 1] + Z;
        int B = this.permutations[X + 1] + Y;
        int BA = this.permutations[B] + Z;
        int BB = this.permutations[B + 1] + Z;

        // Sample noise
        double noise = lerp(fZ,
                lerp(fY,
                        lerp(fX,
                                grad(this.permutations[AA], noiseX, noiseY, noiseZ),
                                grad(this.permutations[BA], noiseX - 1.0D, noiseY, noiseZ)),
                        lerp(fX,
                                grad(this.permutations[AB], noiseX, noiseY - 1.0D, noiseZ),
                                grad(this.permutations[BB], noiseX - 1.0D, noiseY - 1.0D, noiseZ))),
                lerp(fY,
                        lerp(fX,
                                grad(this.permutations[AA + 1], noiseX, noiseY, noiseZ - 1.0D),
                                grad(this.permutations[BA + 1], noiseX - 1.0D, noiseY, noiseZ - 1.0D)),
                        lerp(fX,
                                grad(this.permutations[AB + 1], noiseX, noiseY - 1.0D, noiseZ - 1.0D),
                                grad(this.permutations[BB + 1], noiseX - 1.0D, noiseY - 1.0D, noiseZ - 1.0D))));
        // Apply frequency
        frequency = 1.0D / frequency;
        return noise * frequency;
    }

    /**
     * Samples Perlin XZ noise.
     * @param noiseX noise X coordinate
     * @param noiseZ noise Z coordinate
     * @param frequency noise frequency
     * @return sampled noise
     */
    private double sampleXZ(double noiseX, double noiseZ, double frequency) {
        // Offset noise
        noiseX = noiseX + this.offsetX;
        noiseZ = noiseZ + this.offsetZ;

        // Get floored noise coordinates
        int floorX = Mth.floor(noiseX);
        int floorZ = Mth.floor(noiseZ);

        // Find unit cube that contains point
        int X = floorX & 0xFF;
        int Z = floorZ & 0xFF;

        // Find local x, y, z of point in cube
        noiseX -= floorX;
        noiseZ -= floorZ;

        // Compute fade curves for x, z
        double fX = fade(noiseX);
        double fZ = fade(noiseZ);

        // Apply
        int A = this.permutations[X];
        int AA = this.permutations[A] + Z;
        int B = this.permutations[X + 1];
        int BA = this.permutations[B] + Z;

        // Sample noise
        double noise = lerp(fZ,
                lerp(fX,
                        grad(this.permutations[AA], noiseX, 0.0D, noiseZ),
                        grad(this.permutations[BA], noiseX - 1.0D, 0.0D, noiseZ)),
                lerp(fX,
                        grad(this.permutations[AA + 1], noiseX, 0.0D, noiseZ - 1.0D),
                        grad(this.permutations[BA + 1], noiseX - 1.0D, 0.0D, noiseZ - 1.0D)));
        // Apply frequency
        frequency = 1.0D / frequency;
        return noise * frequency;
    }

    /**
     * Original fade method.
     */
    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    /**
     * Optimized version of the original Perlin lerp.
     */
    private static double lerp(double t, double a, double b) {
        return Math.fma(t, b - a, a);
    }

    /**
     * Optimized version of the original Perlin grad.
     */
    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        // Conditional assignment using bitwise logic to prevent pipeline flushes
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);

        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
