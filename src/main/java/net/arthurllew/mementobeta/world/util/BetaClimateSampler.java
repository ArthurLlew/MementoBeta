package net.arthurllew.mementobeta.world.util;

import net.arthurllew.mementobeta.world.climate.ClimateMap;
import net.arthurllew.mementobeta.world.noise.SimplexOctaveNoiseGen;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;

public class BetaClimateSampler {
    // Noise generators
    private final SimplexOctaveNoiseGen temperatureNoise;
    private final SimplexOctaveNoiseGen humidityNoise;
    private final SimplexOctaveNoiseGen depthNoise;

    public BetaClimateSampler(long seed) {
        this.temperatureNoise = new SimplexOctaveNoiseGen(new Random(seed * 9871L), 4);
        this.humidityNoise = new SimplexOctaveNoiseGen(new Random(seed * 39811L), 4);
        this.depthNoise = new SimplexOctaveNoiseGen(new Random(seed * 543321L), 2);
    }

    /**
     * Samples temperature and humidity.
     * @param tempArr temperature array.
     * @param humArr humidity array.
     * @param x global X coordinate.
     * @param z global Z coordinate.
     * @param sizeX X array size.
     * @param sizeZ Z array size.
     */
    public void sample(double[] tempArr, double[] humArr, int x, int z, int sizeX, int sizeZ) {
        // Loop over chunk positions (in terms of global coordinates)
        int idx = 0;
        for (int sX = 0; sX < sizeX; sX++) {
            for (int sZ = 0; sZ < sizeZ; sZ++) {
                // Sample noise
                double temperature = this.temperatureNoise.sample(x + sX, z + sZ,
                        0.025D, 0.025D, 0.25D);
                double humidity = this.humidityNoise.sample(x + sX, z + sZ,
                        0.05D, 0.05D, 1.0D / 3.0D);
                double depth = this.depthNoise.sample(x + sX, z + sZ,
                        0.25D, 0.25D, 0.5882352941176471D);

                // Get temperature and humidity
                depth = depth * 1.1D + 0.5D;
                temperature = (temperature * 0.15D + 0.7D) * 0.99D + depth * 0.01D;
                humidity = (humidity * 0.15D + 0.5D) * 0.998D + depth * 0.002D;
                temperature = 1.0D - (1.0D - temperature) * (1.0D - temperature);

                // Clamp values
                tempArr[idx] = Mth.clamp(temperature, 0.0D, 1.0D);
                humArr[idx] = Mth.clamp(humidity, 0.0D, 1.0D);

                // Update array index
                idx++;
            }
        }
    }

    /**
     * Samples temperature.
     * @param tempArr temperature array.
     * @param x global X coordinate.
     * @param z global Z coordinate.
     * @param sizeX X array size.
     * @param sizeZ Z array size.
     */
    public void sampleTemperatures(double[] tempArr, int x, int z, int sizeX, int sizeZ) {
        // Loop over chunk positions (in terms of global coordinates)
        int idx = 0;
        for(int sX = 0; sX < sizeX; sX++) {
            for(int sZ = 0; sZ < sizeZ; sZ++) {
                // Sample noise
                double temperature = this.temperatureNoise.sample(x + sX, z + sZ,
                        0.025D, 0.025D, 0.25D);
                double depth = this.depthNoise.sample(x + sX, z + sZ,
                        0.25D, 0.25D, 0.5882352941176471D);

                // Get temperature
                depth = depth * 1.1D + 0.5D;
                temperature = (temperature * 0.15D + 0.7D) * 0.99D + depth * 0.01D;
                temperature = 1.0D - (1.0D - temperature) * (1.0D - temperature);

                // Clamp value
                tempArr[idx] = Mth.clamp(temperature, 0.0D, 1.0D);

                // Update array index
                idx++;
            }
        }
    }

    /**
     * Helper record that can give top layer blocks according to provided temperature and humidity.
     * @param topBlock top-most block.
     * @param fillerBlock blocks under top-most block.
     */
    public record BiomeTopLayerBlocks(Block topBlock, Block fillerBlock) {
        /**
         * @param temperature temperature.
         * @param humidity humidity.
         * @return top layer blocks.
         */
        public static BiomeTopLayerBlocks getFromClimate(double temperature, double humidity) {
            if (ClimateMap.getBiomeFromLookup(temperature, humidity) == ClimateMap.desert) {
                return new BiomeTopLayerBlocks(Blocks.SAND, Blocks.SAND);
            } else {
                return new BiomeTopLayerBlocks(Blocks.GRASS_BLOCK, Blocks.DIRT);
            }
        }
    }
}
