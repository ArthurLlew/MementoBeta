package net.arthurllew.mementobeta.world.levelgen.noise;

import java.util.Random;

public class SimplexNoiseGen extends NoiseGen {
    /**
     * Dot product gradients.
     */
    private static final int[][] gradients = new int[][]{
            {1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0},
            {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
            {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};

    // Historical constants
    private static final double SKEW_FACTOR_2D = 0.5D * (Math.sqrt(3.0D) - 1.0D);
    private static final double UNSKEW_FACTOR_2D = (3.0D - Math.sqrt(3.0D)) / 6.0D;

    /**
     * Constructor.
     * @param random random source
     */
    public SimplexNoiseGen(Random random) {
        super(random);
    }

    /**
     * Samples Simplex XZ noise.
     * @param x block X coordinate
     * @param z block Z coordinate
     * @param scaleX noise X scale
     * @param scaleZ noise Z scale
     * @param amplitude noise amplitude
     */
    public double sampleXZ(double x, double z, double scaleX, double scaleZ, double amplitude) {
        // Noise coordinates + offset
        double noiseX = x * scaleX + this.offsetX;
        double noiseZ = z * scaleZ + this.offsetY;

        // Get floored noise coordinates
        double s = (noiseX + noiseZ) * SKEW_FACTOR_2D;
        int floorNoiseX = floor(noiseX + s);
        int floorNoiseZ = floor(noiseZ + s);

        double t = (floorNoiseX + floorNoiseZ) * UNSKEW_FACTOR_2D;
        double x0 = floorNoiseX - t;
        double z0 = floorNoiseZ - t;
        double xDist = noiseX - x0;
        double zDist = noiseZ - z0;

        int offsetI;
        int offsetJ;
        if (xDist > zDist) {
            offsetI = 1;
            offsetJ = 0;
        }
        else {
            offsetI = 0;
            offsetJ = 1;
        }

        double offsetMidX = xDist - offsetI + UNSKEW_FACTOR_2D;
        double offsetMidZ = zDist - offsetJ + UNSKEW_FACTOR_2D;
        double offsetLastX = xDist - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
        double offsetLastZ = zDist - 1.0 + 2.0 * UNSKEW_FACTOR_2D;

        // Find unit cube that contains point
        int hash0 = floorNoiseX & 255;
        int hash1 = floorNoiseZ & 255;

        // Apply
        int gradNdx0 = this.permutations[hash0 + this.permutations[hash1]] % 12;
        int gradNdx1 = this.permutations[hash0 + offsetI + this.permutations[hash1 + offsetJ]] % 12;
        int gradNdx2 = this.permutations[hash0 + 1 + this.permutations[hash1 + 1]] % 12;

        // T0
        double t0 = 0.5 - xDist * xDist - zDist * zDist;
        double contrib0;
        if (t0 < 0.0) {
            contrib0 = 0.0;
        }
        else {
            t0 *= t0;
            contrib0 = t0 * t0 * dot(gradients[gradNdx0], xDist, zDist);
        }

        // T1
        double t1 = 0.5 - offsetMidX * offsetMidX - offsetMidZ * offsetMidZ;
        double contrib1;
        if (t1 < 0.0) {
            contrib1 = 0.0;
        }
        else {
            t1 *= t1;
            contrib1 = t1 * t1 * dot(gradients[gradNdx1], offsetMidX, offsetMidZ);
        }

        // T2
        double t2 = 0.5 - offsetLastX * offsetLastX - offsetLastZ * offsetLastZ;
        double contrib2;
        if (t2 < 0.0) {
            contrib2 = 0.0;
        }
        else {
            t2 *= t2;
            contrib2 = t2 * t2 * dot(gradients[gradNdx2], offsetLastX, offsetLastZ);
        }

        // Return final noise
        return 70.0 * (contrib0 + contrib1 + contrib2) * amplitude;
    }

    /**
     * Original floor.
     * @return floored value
     */
    private static int floor(double value) {
        return (value > 0.0) ? ((int)value) : ((int)value - 1);
    }

    /**
     * @param gradients gradients
     * @param value1 1rst value
     * @param value2 2nd value
     * @return dot product
     */
    private static double dot(int[] gradients, double value1, double value2) {
        return (double)gradients[0] * value1 + (double)gradients[1] * value2;
    }
}
