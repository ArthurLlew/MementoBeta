package net.arthurllew.mementobeta.world.noise;

import java.util.Arrays;
import java.util.Random;

public class NoiseGeneratorOctaves {
    private final NoiseGenerator[] generatorCollection;
    private final int octaves;

    public NoiseGeneratorOctaves(Random random, int octaves) {
        this.octaves = octaves;
        this.generatorCollection = new NoiseGenerator[octaves];

        for(int var3 = 0; var3 < octaves; ++var3) {
            this.generatorCollection[var3] = new NoiseGenerator(random);
        }

    }

    public double[] sample(double[] noise, double var2, double var4, int var6, int var7, double var8, double var10, double var12) {
        var8 /= 1.5D;
        var10 /= 1.5D;
        if(noise != null && noise.length >= var6 * var7) {
            Arrays.fill(noise, 0.0D);
        } else {
            noise = new double[var6 * var7];
        }

        double var21 = 1.0D;
        double var18 = 1.0D;

        for(int var20 = 0; var20 < this.octaves; ++var20) {
            this.generatorCollection[var20].sample(noise, var2, var4, var6, var7, var8 * var18, var10 * var18, 0.55D / var21);
            var18 *= var12;
            var21 *= 0.5D;
        }

        return noise;
    }
}
