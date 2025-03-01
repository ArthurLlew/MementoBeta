package net.arthurllew.mementobeta.world.noise;

import java.util.Arrays;
import java.util.Random;

public class NoiseGeneratorPerlinOctaves {
    private final NoiseGeneratorPerlin[] generatorCollection;
    private final int octaves;

    public NoiseGeneratorPerlinOctaves(Random random, int octaves) {
        this.octaves = octaves;
        this.generatorCollection = new NoiseGeneratorPerlin[octaves];

        for(int i = 0; i < octaves; ++i) {
            this.generatorCollection[i] = new NoiseGeneratorPerlin(random);
        }

    }

    public double generateMobSpawnerNoise(double var1, double var3) {
        double var5 = 0.0D;
        double var7 = 1.0D;

        for(int var9 = 0; var9 < this.octaves; ++var9) {
            var5 += this.generatorCollection[var9].func_801_a(var1 * var7, var3 * var7) / var7;
            var7 /= 2.0D;
        }

        return var5;
    }

    public double[] generateNoiseOctaves(double[] noise, double x, double y, double z, int sizeX, int sizeY, int sizeZ, double scaleX, double scaleY, double scaleZ) {
        if(noise == null) {
            noise = new double[sizeX * sizeY * sizeZ];
        } else {
            Arrays.fill(noise, 0.0D);
        }

        double frequency = 1.0D;

        for(int i = 0; i < this.octaves; ++i) {
            this.generatorCollection[i].sample(noise, x, y, z, sizeX, sizeY, sizeZ, scaleX * frequency, scaleY * frequency, scaleZ * frequency, frequency);
            frequency /= 2.0D;
        }

        return noise;
    }

    public double[] generateNoiseOctavesFlat(double[] noise, int var2, int var3, int var4, int var5, double var6, double var8, double var10) {
        return this.generateNoiseOctaves(noise, var2, 10.0D, var3, var4, 1, var5, var6, 1.0D, var8);
    }
}
