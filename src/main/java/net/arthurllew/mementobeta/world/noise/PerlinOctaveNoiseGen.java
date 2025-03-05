package net.arthurllew.mementobeta.world.noise;

import java.util.Arrays;
import java.util.Random;

public class PerlinOctaveNoiseGen {
    private final PerlinNoiseGen[] octaves;
    private final int octaveCount;

    public PerlinOctaveNoiseGen(Random random, int octaveCount) {
        this.octaveCount = octaveCount;
        this.octaves = new PerlinNoiseGen[octaveCount];

        for(int i = 0; i < octaveCount; ++i) {
            this.octaves[i] = new PerlinNoiseGen(random);
        }

    }

    public double[] sample(double[] noise, double x, double y, double z, int sizeX, int sizeY, int sizeZ,
                           double scaleX, double scaleY, double scaleZ) {
        if(noise == null) {
            noise = new double[sizeX * sizeY * sizeZ];
        } else {
            Arrays.fill(noise, 0.0D);
        }

        double frequency = 1.0D;

        for(int i = 0; i < this.octaveCount; ++i) {
            this.octaves[i].sampleBeta(noise, x, y, z, sizeX, sizeY, sizeZ,
                    scaleX * frequency, scaleY * frequency, scaleZ * frequency, frequency);
            frequency /= 2.0D;
        }

        return noise;
    }

    public double[] sampleXZ(double[] noise, double x, double z, int sizeX, int sizeZ, double scaleX, double scaleZ) {
        return this.sample(noise, x, 10.0D, z, sizeX, 1, sizeZ, scaleX, 1.0D, scaleZ);
    }

    public double generateMobSpawnerNoise(double x, double y) {
        double noise = 0.0D;
        double frequency = 1.0D;

        for(int i = 0; i < this.octaveCount; ++i) {
            noise += this.octaves[i].sampleModSpawnerNoise(x * frequency, y * frequency) / frequency;
            frequency /= 2.0D;
        }

        return noise;
    }
}
