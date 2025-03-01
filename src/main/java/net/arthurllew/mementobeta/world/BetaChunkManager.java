package net.arthurllew.mementobeta.world;

import net.arthurllew.mementobeta.world.noise.NoiseGeneratorOctaves;

import java.util.Random;

public class BetaChunkManager {
    private final NoiseGeneratorOctaves temperatureNoise;
    private final NoiseGeneratorOctaves humidityNoise;
    private final NoiseGeneratorOctaves depthNoise;
    public double[] temperature;
    public double[] humidity;
    public double[] depth;

    public BetaChunkManager(long seed) {
        this.temperatureNoise = new NoiseGeneratorOctaves(new Random(seed * 9871L), 4);
        this.humidityNoise = new NoiseGeneratorOctaves(new Random(seed * 39811L), 4);
        this.depthNoise = new NoiseGeneratorOctaves(new Random(seed * 543321L), 2);
    }

    public double getTemperature(int var1, int var2) {
        this.temperature = this.temperatureNoise.sample(this.temperature, var1, var2, 1, 1, 0.025F, 0.025F, 0.5D);
        return this.temperature[0];
    }

    public double[] getTemperatures(double[] temperatures, int var2, int var3, int var4, int var5) {
        if(temperatures == null || temperatures.length < var4 * var5) {
            temperatures = new double[var4 * var5];
        }

        temperatures = this.temperatureNoise.sample(temperatures, var2, var3, var4, var5, 0.025F, 0.025F, 0.25D);
        this.depth = this.depthNoise.sample(this.depth, var2, var3, var4, var5, 0.25D, 0.25D, 0.5882352941176471D);
        int var6 = 0;

        for(int var7 = 0; var7 < var4; ++var7) {
            for(int var8 = 0; var8 < var5; ++var8) {
                double var9 = this.depth[var6] * 1.1D + 0.5D;
                double var15 = (temperatures[var6] * 0.15D + 0.7D) * 0.99D + var9 * 0.01D;
                var15 = 1.0D - (1.0D - var15) * (1.0D - var15);
                if(var15 < 0.0D) {
                    var15 = 0.0D;
                }

                if(var15 > 1.0D) {
                    var15 = 1.0D;
                }

                temperatures[var6] = var15;
                ++var6;
            }
        }

        return temperatures;
    }

    public BiomeGenBase[] generateBiomeInfo(BiomeGenBase[] biomes, int var2, int var3, int var4, int var5) {
        if(biomes == null || biomes.length < var4 * var5) {
            biomes = new BiomeGenBase[var4 * var5];
        }

        this.temperature = this.temperatureNoise.sample(this.temperature, var2, var3, var4, var5, 0.025F, 0.025F, 0.25D);
        this.humidity = this.humidityNoise.sample(this.humidity, var2, var3, var4, var5, 0.05F, 0.05F, 1.0D / 3.0D);
        this.depth = this.depthNoise.sample(this.depth, var2, var3, var4, var5, 0.25D, 0.25D, 0.5882352941176471D);
        int var6 = 0;

        for(int var7 = 0; var7 < var4; ++var7) {
            for(int var8 = 0; var8 < var5; ++var8) {
                double var9 = this.depth[var6] * 1.1D + 0.5D;
                double var11 = 0.01D;
                double var13 = 1.0D - var11;
                double var15 = (this.temperature[var6] * 0.15D + 0.7D) * var13 + var9 * var11;
                var11 = 0.002D;
                var13 = 1.0D - var11;
                double var17 = (this.humidity[var6] * 0.15D + 0.5D) * var13 + var9 * var11;
                var15 = 1.0D - (1.0D - var15) * (1.0D - var15);
                if(var15 < 0.0D) {
                    var15 = 0.0D;
                }

                if(var17 < 0.0D) {
                    var17 = 0.0D;
                }

                if(var15 > 1.0D) {
                    var15 = 1.0D;
                }

                if(var17 > 1.0D) {
                    var17 = 1.0D;
                }

                this.temperature[var6] = var15;
                this.humidity[var6] = var17;
                biomes[var6++] = BiomeGenBase.getBiomeFromLookup(var15, var17);
            }
        }

        return biomes;
    }
}
