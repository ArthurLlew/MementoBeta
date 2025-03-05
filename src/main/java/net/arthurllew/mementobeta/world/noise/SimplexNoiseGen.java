package net.arthurllew.mementobeta.world.noise;

import java.util.Random;

public class SimplexNoiseGen {
    private static final int[][] gradients = new int[][]{{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
    private final int[] permutations = new int[512];
    public double xOrigin;
    public double yOrigin;
    public double zOrigin;
    private static final double SKEW_FACTOR_2D = 0.5D * (Math.sqrt(3.0D) - 1.0D);
    private static final double UNSKEW_FACTOR_2D = (3.0D - Math.sqrt(3.0D)) / 6.0D;

    public SimplexNoiseGen(Random random) {
        // Calculate origin
        this.xOrigin = random.nextDouble() * 256.0D;
        this.yOrigin = random.nextDouble() * 256.0D;
        this.zOrigin = random.nextDouble() * 256.0D;

        // Fill permutations
        for (int i = 0; i < 256; ++i) {
            this.permutations[i] = i;
        }
        for (int i = 0; i < 256; ++i) {
            int var3 = random.nextInt(256 - i) + i;
            int var4 = this.permutations[i];
            this.permutations[i] = this.permutations[var3];
            this.permutations[var3] = var4;
            this.permutations[i + 256] = this.permutations[i];
        }

    }

    /**
     * Performs fast floor.
     * @param value value to floor.
     * @return floored value.
     */
    private static int fastFloor(double value) {
        return (value > 0.0) ? ((int)value) : ((int)value - 1);
    }

    /**
     * @param arr array.
     * @param value1 1rst value.
     * @param value2 2nd value.
     * @return sum of 1rst array element multiplied by 1rst value and 2nd element multiplied by 2nd value.
     */
    private static double dot(int[] arr, double value1, double value2) {
        return (double)arr[0] * value1 + (double)arr[1] * value2;
    }

    /**
     * Samples simplex noise at given X and Z.
     * @param x chunk X.
     * @param z chunk Z.
     * @param scaleX noise X scale.
     * @param scaleZ noise Z scale.
     * @param amplitude noise amplitude.
     */
    public double sample(double x, double z, double scaleX, double scaleZ, double amplitude) {
        x = x * scaleX + this.xOrigin;
        z = z * scaleZ + this.yOrigin;

        double s = (x + z) * SKEW_FACTOR_2D;
        int i = fastFloor(x + s);
        int j = fastFloor(z + s);

        double t = (i + j) * UNSKEW_FACTOR_2D;
        double x0 = i - t;
        double z0 = j - t;
        double xDist = x - x0;
        double zDist = z - z0;

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

        int hash0 = i & 255;
        int hash1 = j & 255;
        int gradNdx0 = this.permutations[hash0 + this.permutations[hash1]] % 12;
        int gradNdx1 = this.permutations[hash0 + offsetI + this.permutations[hash1 + offsetJ]] % 12;
        int gradNdx2 = this.permutations[hash0 + 1 + this.permutations[hash1 + 1]] % 12;

        double t0 = 0.5 - xDist * xDist - zDist * zDist;
        double contrib0;
        if (t0 < 0.0) {
            contrib0 = 0.0;
        }
        else {
            t0 *= t0;
            contrib0 = t0 * t0 * dot(gradients[gradNdx0], xDist, zDist);
        }

        double t1 = 0.5 - offsetMidX * offsetMidX - offsetMidZ * offsetMidZ;
        double contrib1;
        if (t1 < 0.0) {
            contrib1 = 0.0;
        }
        else {
            t1 *= t1;
            contrib1 = t1 * t1 * dot(gradients[gradNdx1], offsetMidX, offsetMidZ);
        }

        double t2 = 0.5 - offsetLastX * offsetLastX - offsetLastZ * offsetLastZ;
        double contrib2;
        if (t2 < 0.0) {
            contrib2 = 0.0;
        }
        else {
            t2 *= t2;
            contrib2 = t2 * t2 * dot(gradients[gradNdx2], offsetLastX, offsetLastZ);
        }

        return 70.0 * (contrib0 + contrib1 + contrib2) * amplitude;
    }
}
