package net.arthurllew.mementobeta.world.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Random;

public class WorldGenCaves {
    protected int size = 8;
    protected Random rand = new Random();

    public void generate(ChunkAccess chunk, long seed) {
        // Save chunk position
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        this.rand.setSeed(seed);
        long var7 = this.rand.nextLong() / 2L * 2L + 1L;
        long var9 = this.rand.nextLong() / 2L * 2L + 1L;

        for(int var11 = chunkX - this.size; var11 <= chunkX + this.size; ++var11) {
            for(int var12 = chunkZ - this.size; var12 <= chunkZ + this.size; ++var12) {
                this.rand.setSeed((long)var11 * var7 + (long)var12 * var9 ^ seed);
                this.generate2(chunk, var11, var12, chunkX, chunkZ);
            }
        }

    }

    private void generate2(ChunkAccess chunk, int var2, int var3, int chunkX, int chunkZ) {
        int var7 = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(40) + 1) + 1);
        if(this.rand.nextInt(15) != 0) {
            var7 = 0;
        }

        for(int var8 = 0; var8 < var7; ++var8) {
            double var9 = var2 * 16 + this.rand.nextInt(16);
            double var11 = this.rand.nextInt(this.rand.nextInt(120) + 8);
            double var13 = var3 * 16 + this.rand.nextInt(16);
            int var15 = 1;
            if(this.rand.nextInt(4) == 0) {
                this.generate3(chunk, chunkX, chunkZ, var9, var11, var13);
                var15 += this.rand.nextInt(4);
            }

            for(int var16 = 0; var16 < var15; ++var16) {
                float var17 = this.rand.nextFloat() * (float)Math.PI * 2.0F;
                float var18 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float var19 = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();
                this.generate3(chunk, chunkX, chunkZ, var9, var11, var13, var19, var17, var18,
                        0, 0, 1.0D);
            }
        }
    }

    private void generate3(ChunkAccess chunk, int chunkX, int chunkZ, double var4, double var6, double var8) {
        this.generate3(chunk, chunkX, chunkZ, var4, var6, var8, 1.0F + this.rand.nextFloat() * 6.0F,
                0.0F, 0.0F, -1, -1, 0.5D);
    }

    private void generate3(ChunkAccess chunk, int chunkX, int chunkZ, double var4, double var6, double var8,
                           float var10, float var11, float var12, int var13, int var14, double var15) {
        // Prepare block position
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        double var17 = chunkX * 16 + 8;
        double var19 = chunkZ * 16 + 8;
        float var21 = 0.0F;
        float var22 = 0.0F;
        Random var23 = new Random(this.rand.nextLong());
        if(var14 <= 0) {
            int var24 = this.size * 16 - 16;
            var14 = var24 - var23.nextInt(var24 / 4);
        }

        boolean var52 = false;
        if(var13 == -1) {
            var13 = var14 / 2;
            var52 = true;
        }

        int var25 = var23.nextInt(var14 / 2) + var14 / 4;

        for(boolean var26 = var23.nextInt(6) == 0; var13 < var14; ++var13) {
            double var27 = 1.5D + (double)(Mth.sin((float)var13 * (float)Math.PI / (float)var14) * var10 * 1.0F);
            double var29 = var27 * var15;
            float var31 = Mth.cos(var12);
            float var32 = Mth.sin(var12);
            var4 += Mth.cos(var11) * var31;
            var6 += var32;
            var8 += Mth.sin(var11) * var31;
            if(var26) {
                var12 *= 0.92F;
            } else {
                var12 *= 0.7F;
            }

            var12 += var22 * 0.1F;
            var11 += var21 * 0.1F;
            var22 *= 0.9F;
            var21 *= 12.0F / 16.0F;
            var22 += (var23.nextFloat() - var23.nextFloat()) * var23.nextFloat() * 2.0F;
            var21 += (var23.nextFloat() - var23.nextFloat()) * var23.nextFloat() * 4.0F;
            if(!var52 && var13 == var25 && var10 > 1.0F) {
                this.generate3(chunk, chunkX, chunkZ, var4, var6, var8,
                        var23.nextFloat() * 0.5F + 0.5F, var11 - (float)Math.PI * 0.5F,
                        var12 / 3.0F, var13, var14, 1.0D);
                this.generate3(chunk, chunkX, chunkZ, var4, var6, var8,
                        var23.nextFloat() * 0.5F + 0.5F, var11 + (float)Math.PI * 0.5F,
                        var12 / 3.0F, var13, var14, 1.0D);
                return;
            }

            if(var52 || var23.nextInt(4) != 0) {
                double var33 = var4 - var17;
                double var35 = var8 - var19;
                double var37 = var14 - var13;
                double var39 = var10 + 2.0F + 16.0F;
                if(var33 * var33 + var35 * var35 - var37 * var37 > var39 * var39) {
                    return;
                }

                if(var4 >= var17 - 16.0D - var27 * 2.0D
                        && var8 >= var19 - 16.0D - var27 * 2.0D
                        && var4 <= var17 + 16.0D + var27 * 2.0D
                        && var8 <= var19 + 16.0D + var27 * 2.0D) {
                    int var53 = Mth.floor(var4 - var27) - chunkX * 16 - 1;
                    int var34 = Mth.floor(var4 + var27) - chunkX * 16 + 1;
                    int minY = Mth.floor(var6 - var29) - 1;
                    int maxY = Mth.floor(var6 + var29) + 1;
                    int var55 = Mth.floor(var8 - var27) - chunkZ * 16 - 1;
                    int var38 = Mth.floor(var8 + var27) - chunkZ * 16 + 1;
                    if(var53 < 0) {
                        var53 = 0;
                    }

                    if(var34 > 16) {
                        var34 = 16;
                    }

                    if(minY < 1) {
                        minY = 1;
                    }

                    if(maxY > 120) {
                        maxY = 120;
                    }

                    if(var55 < 0) {
                        var55 = 0;
                    }

                    if(var38 > 16) {
                        var38 = 16;
                    }

                    boolean var56 = false;

                    for(int localX = var53; !var56 && localX < var34; ++localX) {
                        for(int localZ = var55; !var56 && localZ < var38; ++localZ) {
                            for(int localY = maxY + 1; !var56 && localY >= minY - 1; --localY) {
                                if(localY >= 0 && localY < 128) {
                                    pos.set(localX, localY, localZ);
                                    if(chunk.getBlockState(pos) == Blocks.WATER.defaultBlockState()) {
                                        var56 = true;
                                    }

                                    if(localY != minY - 1
                                            && localX != var53
                                            && localX != var34 - 1
                                            && localZ != var55
                                            && localZ != var38 - 1) {
                                        localY = minY;
                                    }
                                }
                            }
                        }
                    }

                    BlockState block;
                    if(!var56) {
                        for(int localX = var53; localX < var34; ++localX) {
                            double var57 = ((double)(localX + chunkX * 16) + 0.5D - var4) / var27;

                            for(int localZ = var55; localZ < var38; ++localZ) {
                                double var44 = ((double)(localZ + chunkZ * 16) + 0.5D - var8) / var27;
                                int currentY = maxY;
                                boolean isGrass = false;
                                if(var57 * var57 + var44 * var44 < 1.0D) {
                                    for(int localY = maxY - 1; localY >= minY; --localY) {
                                        double var49 = ((double)localY + 0.5D - var6) / var29;
                                        if(var49 > -0.7D && var57 * var57 + var49 * var49 + var44 * var44 < 1.0D) {

                                            pos.set(localX, currentY, localZ);
                                            block = chunk.getBlockState(pos);
                                            if(block == Blocks.GRASS_BLOCK.defaultBlockState()) {
                                                isGrass = true;
                                            }

                                            if(block == Blocks.STONE.defaultBlockState()
                                                    || block == Blocks.DIRT.defaultBlockState()
                                                    || block == Blocks.GRASS_BLOCK.defaultBlockState()) {
                                                if(localY < 10) {
                                                    chunk.setBlockState(pos, Blocks.LAVA.defaultBlockState(),
                                                            false);
                                                } else {
                                                    chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(),
                                                            false);

                                                    pos.set(localX, maxY - 1, localZ);
                                                    block = chunk.getBlockState(pos);
                                                    if(isGrass && block == Blocks.DIRT.defaultBlockState()) {
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

                        if(var52) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
