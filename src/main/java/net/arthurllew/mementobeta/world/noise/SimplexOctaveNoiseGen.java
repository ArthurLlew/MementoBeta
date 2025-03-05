package net.arthurllew.mementobeta.world.noise;

import java.util.Random;

public class SimplexOctaveNoiseGen {
    private final SimplexNoiseGen[] octaves;
    private final int octaveCount;

    public SimplexOctaveNoiseGen(Random random, int octaveCount) {
        this.octaveCount = octaveCount;
        this.octaves = new SimplexNoiseGen[octaveCount];

        for(int var3 = 0; var3 < octaveCount; ++var3) {
            this.octaves[var3] = new SimplexNoiseGen(random);
        }
    }

    public double sample(double x, double z, double scaleX, double scaleZ, double lacunarity) {
        return this.sample(x, z, scaleX, scaleZ, lacunarity, 0.5);
    }

    public double sample(double x, double z, double scaleX, double scaleZ, double lacunarity, double persistence) {
        scaleX /= 1.5D;
        scaleZ /= 1.5D;

        double noise = 0.0;
        double amplitude = 1.0D;
        double frequency = 1.0D;

        for(int i = 0; i < this.octaveCount; ++i) {
            noise += this.octaves[i].sample(x, z, scaleX * frequency, scaleZ * frequency,
                    0.55D / amplitude);
            frequency *= lacunarity;
            amplitude *= persistence;
        }

        return noise;
    }
}
