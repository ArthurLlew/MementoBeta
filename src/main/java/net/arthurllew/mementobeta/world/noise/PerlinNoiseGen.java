package net.arthurllew.mementobeta.world.noise;

import net.minecraft.util.Mth;

import java.util.Random;

/**
 * All the things going on below are carefully explained here
 * <a href="https://adrianb.io/2014/08/09/perlinnoise.html">https://adrianb.io/2014/08/09/perlinnoise.html</a>.
 * Notch used earlier implementation by Ken Perlin, because most functions match identical when decompiling
 * Beta 1.7.3 Vanilla version. For example, grad() function code (when decompiled) looks like so:
 *
 * int var8 = hash & 15;
 * double var9 = var8 < 8 ? x : y;
 * double var11 = var8 < 4 ? y : (var8 != 12 && var8 != 14 ? z : x);
 * return ((var8 & 1) == 0 ? var9 : -var9) + ((var8 & 2) == 0 ? var11 : -var11);
 *
 * which is identical to what Ken Perlin's code looked like.
 */
public class PerlinNoiseGen {
    private final int[] permutations = new int[512];
    public double offsetX;
    public double offsetY;
    public double offsetZ;

    public PerlinNoiseGen(Random random) {
        // Calculate origin
        this.offsetX = random.nextDouble() * 256.0D;
        this.offsetY = random.nextDouble() * 256.0D;
        this.offsetZ = random.nextDouble() * 256.0D;

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

    private static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    private static double grad(int hash, double x, double y, double z) {
        switch(hash & 0xF)
        {
            case 0x0:
                return x + y;
            case 0x1:
                return -x + y;
            case 0x2:
                return x - y;
            case 0x3:
                return -x - y;
            case 0x4:
                return x + z;
            case 0x5:
                return -x + z;
            case 0x6:
                return x - z;
            case 0x7:
                return -x - z;
            case 0x8:
                return y + z;
            case 0x9:
                return -y + z;
            case 0xA:
                return y - z;
            case 0xB:
                return -y - z;
            case 0xC:
                return y + x;
            case 0xD:
                return -y + z;
            case 0xE:
                return y - x;
            case 0xF:
                return -y - z;
            default:
                return 0; // never happens
        }
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    public void sampleAlpha(double[] arr, double x, double y, double z, int sizeX, int sizeY, int sizeZ,
                            double scaleX, double scaleY, double scaleZ, double frequency) {
        int idx = 0;
        frequency = 1.0D / frequency;
        int flagY = -1;

        double lerp0 = 0.0D;
        double lerp1 = 0.0D;
        double lerp2 = 0.0D;
        double lerp3 = 0.0D;

        // Iterate over a collection of noise points
        for (int sX = 0; sX < sizeX; sX++) {
            for (int sZ = 0; sZ < sizeZ; sZ++) {
                for (int sY = 0; sY < sizeY; sY++) {
                    double curX = (x + (double)sX) * scaleX + this.offsetX;
                    double curY = (y + (double)sY) * scaleY + this.offsetY;
                    double curZ = (z + (double)sZ) * scaleZ + this.offsetZ;

                    int floorX = Mth.floor(curX);
                    int floorY = Mth.floor(curY);
                    int floorZ = Mth.floor(curZ);

                    // Find unit cube that contains point.
                    int X = floorX & 0xFF;
                    int Y = floorY & 0xFF;
                    int Z = floorZ & 0xFF;

                    // Find local x, y, z of point in cube.
                    curX -= floorX;
                    curY -= floorY;
                    curZ -= floorZ;

                    // Compute fade curves for x, y, z.
                    double u = fade(curX);
                    double v = fade(curY);
                    double w = fade(curZ);

                    if (sY == 0 || Y != flagY) {
                        flagY = Y;

                        int A =  this.permutations[X] + Y;
                        int AA = this.permutations[A] + Z;
                        int AB = this.permutations[A + 1] + Z;
                        int B =  this.permutations[X + 1] + Y;
                        int BA = this.permutations[B] + Z;
                        int BB = this.permutations[B + 1] + Z;

                        lerp0 = lerp(
                                u,
                                grad(this.permutations[AA], curX, curY, curZ),
                                grad(this.permutations[BA], curX - 1.0D, curY, curZ)
                        );

                        lerp1 = lerp(
                                u,
                                grad(this.permutations[AB], curX, curY - 1.0D, curZ),
                                grad(this.permutations[BB], curX - 1.0D, curY - 1.0D, curZ)
                        );

                        lerp2 = lerp(
                                u,
                                grad(this.permutations[AA + 1], curX, curY, curZ - 1.0D),
                                grad(this.permutations[BA + 1], curX - 1.0D, curY, curZ - 1.0D)
                        );

                        lerp3 = lerp(
                                u,
                                grad(this.permutations[AB + 1], curX, curY - 1.0D, curZ - 1.0D),
                                grad(this.permutations[BB + 1], curX - 1.0D, curY - 1.0D, curZ - 1.0D)
                        );
                    }

                    double res = lerp(w, lerp(v, lerp0, lerp1), lerp(v, lerp2, lerp3));

                    arr[idx++] += res * frequency;
                }
            }
        }
    }

    private double sampleXZ(double x, double z, double frequency) {
        frequency = 1.0D / frequency;

        x = x + this.offsetX;
        z = z + this.offsetZ;

        int floorX = Mth.floor(x);
        int floorZ = Mth.floor(z);

        // Find unit cube that contains point.
        int X = floorX & 0xFF;
        int Z = floorZ & 0xFF;

        // Find local x, y, z of point in cube.
        x -= floorX;
        z -= floorZ;

        // Compute fade curves for x, y, z.
        double u = fade(x);
        double w = fade(z);

        int A = this.permutations[X];
        int AA = this.permutations[A] + Z;
        int B = this.permutations[X + 1];
        int BA = this.permutations[B] + Z;

        double lerp0 = lerp(
                u,
                grad(this.permutations[AA], x, 0.0D, z),
                grad(this.permutations[BA], x - 1.0D, 0.0D, z));
        double lerp1 = lerp(
                u,
                grad(this.permutations[AA + 1], x, 0.0D, z - 1.0D),
                grad(this.permutations[BA + 1], x - 1.0D, 0.0D, z - 1.0D));

        double res = lerp(w, lerp0, lerp1);

        return res * frequency;
    }

    public void sampleBeta(double[] arr, double x, double y, double z, int sizeX, int sizeY, int sizeZ,
                           double scaleX, double scaleY, double scaleZ, double frequency) {
        if (sizeY != 1) {
            this.sampleAlpha(arr, x, y, z, sizeX, sizeY, sizeZ, scaleX, scaleY, scaleZ, frequency);
        } else {
            int ndx = 0;
            for (int sX = 0; sX < sizeX; sX++) {
                for (int sZ = 0; sZ < sizeZ; sZ++) {
                    double curX = (x + (double)sX) * scaleX;
                    double curZ = (z + (double)sZ) * scaleZ;

                    arr[ndx++] += this.sampleXZ(curX, curZ, frequency);
                }
            }
        }
    }

    private double sampleXYZ(double x, double y, double z) {
        // Get noise coordinates
        double noiseX = x + this.offsetX;
        double noiseY = y + this.offsetY;
        double noiseZ = z + this.offsetZ;

        // Get floored noise coordinates
        int floorNoiseX = Mth.floor(noiseX);
        int floorNoiseY = Mth.floor(noiseY);
        int floorNoiseZ = Mth.floor(noiseZ);

        int X = floorNoiseX & 255;
        int Y = floorNoiseY & 255;
        int Z = floorNoiseZ & 255;

        noiseX -= floorNoiseX;
        noiseY -= floorNoiseY;
        noiseZ -= floorNoiseZ;

        double noiseFadeX = fade(noiseX);
        double noiseFadeY = fade(noiseY);
        double noiseFadeZ = fade(noiseZ);

        int A = this.permutations[X] + Y;
        int AA = this.permutations[A] + Z;
        int AB = this.permutations[A + 1] + Z;
        int B = this.permutations[X + 1] + Y;
        int BA = this.permutations[B] + Z;
        int BB = this.permutations[B + 1] + Z;

        return lerp(noiseFadeZ,
                lerp(noiseFadeY, lerp(noiseFadeX, grad(this.permutations[AA], noiseX, noiseY, noiseZ), grad(this.permutations[BA], noiseX - 1.0D, noiseY, noiseZ)),
                        lerp(noiseFadeX, grad(this.permutations[AB], noiseX, noiseY - 1.0D, noiseZ), grad(this.permutations[BB], noiseX - 1.0D, noiseY - 1.0D, noiseZ))),
                lerp(noiseFadeY, lerp(noiseFadeX, grad(this.permutations[AA + 1], noiseX, noiseY, noiseZ - 1.0D), grad(this.permutations[BA + 1], noiseX - 1.0D, noiseY, noiseZ - 1.0D)),
                        lerp(noiseFadeX, grad(this.permutations[AB + 1], noiseX, noiseY - 1.0D, noiseZ - 1.0D), grad(this.permutations[BB + 1], noiseX - 1.0D, noiseY - 1.0D, noiseZ - 1.0D))));
    }

    public double sampleModSpawnerNoise(double x, double y) {
        return this.sampleXYZ(x, y, 0.0D);
    }
}
