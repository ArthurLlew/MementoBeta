package net.arthurllew.mementobeta.world.levelgen;

import net.arthurllew.mementobeta.world.noise.PerlinOctaveNoiseGen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;
import java.util.function.BiConsumer;

public class BetaTerrainSampler {
    // Noise generators
    public PerlinOctaveNoiseGen minLimitOctaveNoise;
    public PerlinOctaveNoiseGen maxLimitOctaveNoise;
    public PerlinOctaveNoiseGen mainOctaveNoise;
    public PerlinOctaveNoiseGen beachOctaveNoise;
    public PerlinOctaveNoiseGen surfaceOctaveNoise;
    public PerlinOctaveNoiseGen scaleOctaveNoise;
    public PerlinOctaveNoiseGen depthOctaveNoise;
    public PerlinOctaveNoiseGen forestOctaveNoise;

    // Helper noises
    private double[] mainNoise;
    private double[] minLimitNoise;
    private double[] maxLimitNoise;
    private double[] scaleNoise;
    private double[] depthNoise;

    public BetaTerrainSampler(long seed) {
        // Init octave noises
        Random rand = new Random(seed);
        this.minLimitOctaveNoise = new PerlinOctaveNoiseGen(rand, 16);
        this.maxLimitOctaveNoise = new PerlinOctaveNoiseGen(rand, 16);
        this.mainOctaveNoise = new PerlinOctaveNoiseGen(rand, 8);
        this.beachOctaveNoise = new PerlinOctaveNoiseGen(rand, 4);
        this.surfaceOctaveNoise = new PerlinOctaveNoiseGen(rand, 4);
        this.scaleOctaveNoise = new PerlinOctaveNoiseGen(rand, 10);
        this.depthOctaveNoise = new PerlinOctaveNoiseGen(rand, 16);
        this.forestOctaveNoise = new PerlinOctaveNoiseGen(rand, 8);
    }

    /**
     * Generates Beta 1.7.3 terrain noise.
     * @param noise noise buffer.
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param z Z coordinate.
     * @param sizeX noise X size.
     * @param sizeY noise Y size.
     * @param sizeZ noise Z size.
     * @return filled buffer.
     */
    public double[] sampleNoise(double[] noise, int x, int y, int z, int sizeX, int sizeY, int sizeZ,
                                double[] tempArr, double[] humArr) {
        if (noise == null)  {
            noise = new double[sizeX * sizeY * sizeZ];
        }

        double scaleX = 684.412D;
        double scaleY = 684.412D;
        this.scaleNoise = this.scaleOctaveNoise.sampleXZ(this.scaleNoise, x, z, sizeX, sizeZ,
                1.121D, 1.121D);
        this.depthNoise = this.depthOctaveNoise.sampleXZ(this.depthNoise, x, z, sizeX, sizeZ,
                200.0D, 200.0D);
        this.mainNoise = this.mainOctaveNoise.sample(this.mainNoise, x, y, z,
                sizeX, sizeY, sizeZ,
                scaleX / 80.0D,
                scaleY / 160.0D,
                scaleX / 80.0D);
        this.minLimitNoise = this.minLimitOctaveNoise.sample(this.minLimitNoise, x, y, z,
                sizeX, sizeY, sizeZ, scaleX, scaleY, scaleX);
        this.maxLimitNoise = this.maxLimitOctaveNoise.sample(this.maxLimitNoise, x, y, z,
                sizeX, sizeY, sizeZ, scaleX, scaleY, scaleX);

        int noiseIndex1 = 0;
        int noiseIndex2 = 0;
        int sizeDifference = 16 / sizeX;

        for(int localX = 0; localX < sizeX; ++localX) {
            int shiftedLocalX = localX * sizeDifference + sizeDifference / 2;

            for(int localZ = 0; localZ < sizeZ; ++localZ) {
                int shiftedLocalZ = localZ * sizeDifference + sizeDifference / 2;

                double temperature = tempArr[shiftedLocalX * 16 + shiftedLocalZ];
                double humidity = humArr[shiftedLocalX * 16 + shiftedLocalZ] * temperature;

                humidity = 1.0D - humidity;
                humidity *= humidity;
                humidity *= humidity;
                humidity = 1.0D - humidity;

                double scale = (this.scaleNoise[noiseIndex2] + 256.0D) / 512.0D;
                scale *= humidity;
                if(scale > 1.0D) {
                    scale = 1.0D;
                }

                double depth = this.depthNoise[noiseIndex2] / 8000.0D;
                if(depth < 0.0D) {
                    depth = -depth * 0.3D;
                }

                depth = depth * 3.0D - 2.0D;
                if(depth < 0.0D) {
                    depth /= 2.0D;
                    if(depth < -1.0D) {
                        depth = -1.0D;
                    }

                    depth /= 1.4D;
                    depth /= 2.0D;
                    scale = 0.0D;
                } else {
                    if(depth > 1.0D) {
                        depth = 1.0D;
                    }

                    depth /= 8.0D;
                }

                if(scale < 0.0D) {
                    scale = 0.0D;
                }

                scale += 0.5D;
                depth = depth * (double)sizeY / 16.0D;
                double var31 = (double)sizeY / 2.0D + depth * 4.0D;
                ++noiseIndex2;

                for(int NoiseY = 0; NoiseY < sizeY; ++NoiseY) {
                    double densityOffset = ((double)NoiseY - var31) * 12.0D / scale;
                    if(densityOffset < 0.0D) {
                        densityOffset *= 4.0D;
                    }

                    double minLimitNoise = this.minLimitNoise[noiseIndex1] / 512.0D;
                    double maxLimitNoise = this.maxLimitNoise[noiseIndex1] / 512.0D;
                    double mainNoise = (this.mainNoise[noiseIndex1] / 10.0D + 1.0D) / 2.0D;

                    double density;
                    if(mainNoise < 0.0D) {
                        density = minLimitNoise;
                    } else if(mainNoise > 1.0D) {
                        density = maxLimitNoise;
                    } else {
                        density = minLimitNoise + (maxLimitNoise - minLimitNoise) * mainNoise;
                    }

                    density -= densityOffset;
                    if(NoiseY > sizeY - 4) {
                        double var44 = (float)(NoiseY - (sizeY - 4)) / 3.0F;
                        density = density * (1.0D - var44) + -10.0D * var44;
                    }

                    noise[noiseIndex1] = density;
                    ++noiseIndex1;
                }
            }
        }

        return noise;
    }

    /**
     * Generates Beta 1.7.3 terrain via provided generation action.
     * @param terrainNoise Beta 1.7.3 terrain noise.
     * @param seaLevel sea level.
     * @param genAction generation action.
     */
    public void sampleTerrain(double[] terrainNoise, int seaLevel,
                              BiConsumer<BlockPos.MutableBlockPos, BlockState> genAction) {
        // Prepare block position
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // ================================================================================================
        // In Vanilla Beta 1.7.3 this section is done by ChunkProviderGenerate.generateTerrain(...) method.
        // ================================================================================================

        // Those are initialized at the beginning of ChunkProviderGenerate.generateTerrain(...) method.
        byte sizeHorizontal = 4;
        byte sizeVertical = 16;
        //int sizeX = sizeHorizontal + 1;
        int sizeY = sizeVertical + 1;
        int sizeZ = sizeHorizontal + 1;

        // Sea level is stored in generator settings
        //int seaLevel = this.generatorSettings().value().seaLevel();

        // Generate terrain
        for(int sectionX = 0; sectionX < sizeHorizontal; ++sectionX) {
            for(int sectionZ = 0; sectionZ < sizeHorizontal; ++sectionZ) {
                for(int sectionY = 0; sectionY < sizeVertical; ++sectionY) {
                    // Get noises
                    double noise1 = terrainNoise[((sectionX + 0) * sizeZ + sectionZ + 0) * sizeY + sectionY + 0];
                    double noise2 = terrainNoise[((sectionX + 0) * sizeZ + sectionZ + 1) * sizeY + sectionY + 0];
                    double noise3 = terrainNoise[((sectionX + 1) * sizeZ + sectionZ + 0) * sizeY + sectionY + 0];
                    double noise4 = terrainNoise[((sectionX + 1) * sizeZ + sectionZ + 1) * sizeY + sectionY + 0];
                    double noiseDelta1 =
                            (terrainNoise[((sectionX + 0) * sizeZ + sectionZ + 0) * sizeY + sectionY + 1] - noise1)
                                    * 0.125D;
                    double noiseDelta2 =
                            (terrainNoise[((sectionX + 0) * sizeZ + sectionZ + 1) * sizeY + sectionY + 1] - noise2)
                                    * 0.125D;
                    double noiseDelta3 =
                            (terrainNoise[((sectionX + 1) * sizeZ + sectionZ + 0) * sizeY + sectionY + 1] - noise3)
                                    * 0.125D;
                    double NoiseDelta4 =
                            (terrainNoise[((sectionX + 1) * sizeZ + sectionZ + 1) * sizeY + sectionY + 1] - noise4)
                                    * 0.125D;

                    for(int localY = 0; localY < 8; ++localY) {
                        // Pre-density values
                        double preDensity1 = noise1;
                        double preDensity2 = noise2;
                        double preDensityDelta1 = (noise3 - noise1) * 0.25D;
                        double preDensityDelta2 = (noise4 - noise2) * 0.25D;

                        for(int localX = 0; localX < 4; ++localX) {
                            // Density values
                            double density = preDensity1;
                            double densityDelta = (preDensity2 - preDensity1) * 0.25D;

                            for(int localZ = 0; localZ < 4; ++localZ) {
                                // Set block position
                                pos.set(localX + sectionX * 4,
                                        localY + sectionY * 8,
                                        localZ + sectionZ * 4);

                                // Choose block
                                Block block;
                                if(density > 0.0D) {
                                    // Stone for any density > 0
                                    block = Blocks.STONE;
                                }
                                else
                                {
                                    // Set water if below sea level
                                    if(localY + sectionY * 8 < seaLevel) {
                                        block = Blocks.WATER;
                                    }
                                    // Air otherwise
                                    else {
                                        block = Blocks.AIR;
                                    }
                                }

                                // Generation action (e.g. set block or/and update heightmap)
                                genAction.accept(pos, block.defaultBlockState());

                                // Update density
                                density += densityDelta;
                            }

                            // Update pre-density
                            preDensity1 += preDensityDelta1;
                            preDensity2 += preDensityDelta2;
                        }

                        // Update noise
                        noise1 += noiseDelta1;
                        noise2 += noiseDelta2;
                        noise3 += noiseDelta3;
                        noise4 += NoiseDelta4;
                    }
                }
            }
        }
    }
}
