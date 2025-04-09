package net.arthurllew.mementobeta.world.levelgen;

import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Random;

public class BetaCavesCarver {
    /**
     * XZ carver radius.
     */
    protected int range = 8;
    /**
     * Local random.
     */
    protected Random rand = new Random();

    /**
     * Generates caves in provided chunk with provided world seed.
     */
    public void carve(ChunkAccess chunk, long seed) {
        // Save chunk position
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // Seed adjustments
        this.rand.setSeed(seed);
        long seed1 = this.rand.nextLong() / 2L * 2L + 1L;
        long seed2 = this.rand.nextLong() / 2L * 2L + 1L;

        // Try to generate caves inside bounding box, centered with the chunk
        for(int localX = chunkX - this.range; localX <= chunkX + this.range; ++localX) {
            for(int localZ = chunkZ - this.range; localZ <= chunkZ + this.range; ++localZ) {
                // Set seed from position
                this.rand.setSeed((long)localX * seed1 + (long)localZ * seed2 ^ seed);

                // Carve
                this.tryToCarveAtPoint(chunk, chunkX, chunkZ, localX, localZ);
            }
        }

    }

    /**
     * Tries to generate caves and tunnels at provided position inside chunk.
     */
    private void tryToCarveAtPoint(ChunkAccess chunk, int chunkX, int chunkZ, int localX, int localZ) {
        // Determine number of caves
        int CaveCount = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(40) + 1) + 1);
        if(this.rand.nextInt(15) != 0) {
            CaveCount = 0;
        }

        // Carve caves
        for(int cave = 0; cave < CaveCount; ++cave) {
            double x = localX * 16 + this.rand.nextInt(16);
            double y = this.rand.nextInt(this.rand.nextInt(120) + 8);
            double z = localZ * 16 + this.rand.nextInt(16);

            // Min number of tunnels
            int tunnelCount = 1;
            // Carve cave and increase number of tunnels
            if(this.rand.nextInt(4) == 0) {
                this.carveCave(chunk, chunkX, chunkZ, x, y, z);
                tunnelCount += this.rand.nextInt(4);
            }

            // Carve tunnels
            for(int tunnel = 0; tunnel < tunnelCount; ++tunnel) {
                float width = this.rand.nextFloat() * (float)Math.PI * 2.0F;
                float yaw = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float pitch = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();
                this.carveTunnels(chunk, chunkX, chunkZ, x, y, z, pitch, width, yaw,
                        0, 0, 1.0D);
            }
        }
    }

    /**
     * Generates cave.
     */
    private void carveCave(ChunkAccess chunk, int chunkX, int chunkZ, double x, double y, double z) {
        this.carveTunnels(chunk, chunkX, chunkZ, x, y, z, 1.0F + this.rand.nextFloat() * 6.0F,
                0.0F, 0.0F, -1, -1, 0.5D);
    }

    /**
     * Generates tunnels.
     */
    private void carveTunnels(ChunkAccess chunk, int chunkX, int chunkZ, double x, double y, double z,
                              float width, float yaw, float pitch, int branch, int branchCount, double yawPitchRatio) {
        // Prepare block position
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        double xInChunkMiddle = chunkX * 16 + 8;
        double yInChunkMiddle = chunkZ * 16 + 8;

        float factorYaw = 0.0F;
        float factorPitch = 0.0F;

        Random localRand = new Random(this.rand.nextLong());
        if(branchCount <= 0) {
            int var24 = this.range * 16 - 16;
            branchCount = var24 - localRand.nextInt(var24 / 4);
        }

        boolean isEnclosed = false;
        if(branch == -1) {
            branch = branchCount / 2;
            isEnclosed = true;
        }

        int additionalBranches = localRand.nextInt(branchCount / 2) + branchCount / 4;

        float pitchScale = (localRand.nextInt(6) == 0) ? 0.92F : 0.7F;

        for(; branch < branchCount; ++branch) {
            double tunnelHorizontalScale = 1.5D + (double)(Mth.sin((float)branch * (float)Math.PI / (float)branchCount) * width * 1.0F);
            double tunnelVerticalScale = tunnelHorizontalScale * yawPitchRatio;
            float pitchCos = Mth.cos(pitch);
            float pitchSin = Mth.sin(pitch);
            x += Mth.cos(yaw) * pitchCos;
            y += pitchSin;
            z += Mth.sin(yaw) * pitchCos;

            pitch *= pitchScale;

            pitch += factorPitch * 0.1F;
            yaw += factorYaw * 0.1F;

            factorPitch *= 0.9F;
            factorYaw *= 0.75F;

            factorPitch += (localRand.nextFloat() - localRand.nextFloat()) * localRand.nextFloat() * 2.0F;
            factorYaw += (localRand.nextFloat() - localRand.nextFloat()) * localRand.nextFloat() * 4.0F;

            // Either create additional branches and stop or go further
            if(!isEnclosed && branch == additionalBranches && width > 1.0F) {
                this.carveTunnels(chunk, chunkX, chunkZ, x, y, z,
                        localRand.nextFloat() * 0.5F + 0.5F, yaw - (float)Math.PI * 0.5F,
                        pitch / 3.0F, branch, branchCount, 1.0D);
                this.carveTunnels(chunk, chunkX, chunkZ, x, y, z,
                        localRand.nextFloat() * 0.5F + 0.5F, yaw + (float)Math.PI * 0.5F,
                        pitch / 3.0F, branch, branchCount, 1.0D);
                return;
            }

            if(isEnclosed || localRand.nextInt(4) != 0) {
                // Check stop condition
                double xTemp = x - xInChunkMiddle;
                double zTemp = z - yInChunkMiddle;
                double branchesLeft = branchCount - branch;
                double adjustedWidth = width + 18.0F;
                if(xTemp * xTemp + zTemp * zTemp - branchesLeft * branchesLeft > adjustedWidth * adjustedWidth) {
                    return;
                }

                if(x >= xInChunkMiddle - 16.0D - tunnelHorizontalScale * 2.0D
                        && z >= yInChunkMiddle - 16.0D - tunnelHorizontalScale * 2.0D
                        && x <= xInChunkMiddle + 16.0D + tunnelHorizontalScale * 2.0D
                        && z <= yInChunkMiddle + 16.0D + tunnelHorizontalScale * 2.0D) {

                    // Calculate cave bounding box
                    int minX = Mth.floor(x - tunnelHorizontalScale) - chunkX * 16 - 1;
                    int maxX = Mth.floor(x + tunnelHorizontalScale) - chunkX * 16 + 1;
                    int minY = Mth.floor(y - tunnelVerticalScale) - 1;
                    int maxY = Mth.floor(y + tunnelVerticalScale) + 1;
                    int minZ = Mth.floor(z - tunnelHorizontalScale) - chunkZ * 16 - 1;
                    int maxZ = Mth.floor(z + tunnelHorizontalScale) - chunkZ * 16 + 1;

                    // Clamp bounding box in chunk borders
                    minX = Mth.clamp(minX, 0, 16);
                    maxX = Mth.clamp(maxX, 0, 16);
                    minZ = Mth.clamp(minZ, 0, 16);
                    maxZ = Mth.clamp(maxZ, 0, 16);
                    if(minY < 1) {
                        minY = 1;
                    }
                    if(maxY > 120) {
                        maxY = 120;
                    }

                    // Check for water presence
                    boolean isWater = false;
                    for(int localX = minX; !isWater && localX < maxX; ++localX) {
                        for(int localZ = minZ; !isWater && localZ < maxZ; ++localZ) {
                            for(int localY = maxY + 1; !isWater && localY >= minY - 1; --localY) {
                                pos.set(localX, localY, localZ);

                                // Check water
                                if(chunk.getBlockState(pos).is(Blocks.WATER)) {
                                    isWater = true;
                                }

                                // Update Y position
                                if(localY != minY - 1
                                        && localX != minX
                                        && localX != maxX - 1
                                        && localZ != minZ
                                        && localZ != maxZ - 1) {
                                    localY = minY;
                                }
                            }
                        }
                    }


                    if(!isWater) {
                        // Prepare block
                        BlockState block;

                        for(int localX = minX; localX < maxX; ++localX) {
                            // X density
                            double xDensity = ((double)(localX + chunkX * 16) + 0.5D - x) / tunnelHorizontalScale;

                            for(int localZ = minZ; localZ < maxZ; ++localZ) {
                                // Z density
                                double zDensity = ((double)(localZ + chunkZ * 16) + 0.5D - z) / tunnelHorizontalScale;

                                // Set Y at
                                int currentY = maxY;

                                // Will be true if we will carve into grass
                                boolean isGrass = false;

                                //
                                if(xDensity * xDensity + zDensity * zDensity < 1.0D) {
                                    // Oy
                                    for(int localY = maxY - 1; localY >= minY; --localY) {
                                        double yDensity = ((double)localY + 0.5D - y) / tunnelVerticalScale;

                                        if(yDensity > -0.7D && xDensity * xDensity + yDensity * yDensity + zDensity * zDensity < 1.0D) {
                                            pos.set(localX, currentY, localZ);

                                            // Get block at this position
                                            block = chunk.getBlockState(pos);

                                            // Update grass condition
                                            if(block.is(Blocks.GRASS_BLOCK)) {
                                                isGrass = true;
                                            }

                                            // Block is in list of blocks we can carve
                                            if(block.is(Blocks.STONE) || block.is(Blocks.DIRT)
                                                    || block.is(Blocks.RED_SAND) || block.is(Blocks.GRASS_BLOCK)) {
                                                // Below certain height
                                                if(localY < 10) {
                                                    // Carve with lava
                                                    chunk.setBlockState(pos,
                                                            MementoBetaBlocks.BETA_lAVA.get().defaultBlockState(),
                                                            false);
                                                }
                                                // Everywhere else
                                                else {
                                                    // Carve with air
                                                    chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(),
                                                            false);

                                                    // Check grass condition and below block for dirt
                                                    pos.set(localX, currentY - 1, localZ);
                                                    if(isGrass && chunk.getBlockState(pos).is(Blocks.DIRT)) {
                                                        // Replace with grass
                                                        chunk.setBlockState(pos, Blocks.GRASS_BLOCK.defaultBlockState(),
                                                                false);
                                                    }
                                                }
                                            }
                                        }

                                        --currentY;
                                    }
                                }
                            }
                        }

                        if(isEnclosed) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
