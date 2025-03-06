package net.arthurllew.mementobeta.world.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class WorldGenLakes {
    public static boolean generate(WorldGenLevel genRegion, Random rand, int x, int y, int z, Block block) {
        // Prepare block position
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        x -= 8;
        z -= 8;

        // Find top most air block
        pos.set(x, y, z);
        while(y > 0 && genRegion.getBlockState(pos).isAir()) {
            y--;
            pos.set(x, y, z);
        }

        y -= 4;

        boolean[] noise = new boolean[2048];
        int max_i = rand.nextInt(4) + 4;

        for(int i = 0; i < max_i; ++i) {
            double factor1 = rand.nextDouble() * 6.0D + 3.0D;
            double factor2 = rand.nextDouble() * 4.0D + 2.0D;
            double factor3 = rand.nextDouble() * 6.0D + 3.0D;
            double factor4 = rand.nextDouble() * (16.0D - factor1 - 2.0D) + 1.0D + factor1 / 2.0D;
            double factor5 = rand.nextDouble() * (8.0D - factor2 - 4.0D) + 2.0D + factor2 / 2.0D;
            double factor6 = rand.nextDouble() * (16.0D - factor3 - 2.0D) + 1.0D + factor3 / 2.0D;

            for(int iX = 1; iX < 15; ++iX) {
                for(int iZ = 1; iZ < 15; ++iZ) {
                    for(int iY = 1; iY < 7; ++iY) {
                        double factorX = ((double)iX - factor4) / (factor1 / 2.0D);
                        double factorY = ((double)iY - factor5) / (factor2 / 2.0D);
                        double factorZ = ((double)iZ - factor6) / (factor3 / 2.0D);
                        double factor = factorX * factorX + factorY * factorY + factorZ * factorZ;
                        if(factor < 1.0D) {
                            noise[(iX * 16 + iZ) * 8 + iY] = true;
                        }
                    }
                }
            }
        }

        boolean condition;
        for(int iX = 0; iX < 16; ++iX) {
            for(int iZ = 0; iZ < 16; ++iZ) {
                for(int iY = 0; iY < 8; ++iY) {
                    condition = !noise[(iX * 16 + iZ) * 8 + iY]
                            && (iX < 15 && noise[((iX + 1) * 16 + iZ) * 8 + iY] || iX > 0
                                && noise[((iX - 1) * 16 + iZ) * 8 + iY] || iZ < 15
                                && noise[(iX * 16 + iZ + 1) * 8 + iY] || iZ > 0
                                && noise[(iX * 16 + (iZ - 1)) * 8 + iY] || iY < 7
                                && noise[(iX * 16 + iZ) * 8 + iY + 1] || iY > 0
                                && noise[(iX * 16 + iZ) * 8 + (iY - 1)]);
                    if(condition) {
                        pos.set(x + iX, y + iY, z + iZ);

                        BlockState observedBlock = genRegion.getBlockState(pos);
                        if(iY >= 4 && !observedBlock.getFluidState().isEmpty()) {
                            return false;
                        }

                        if(iY < 4 && !observedBlock.isSolid()
                                && genRegion.getBlockState(pos) != block.defaultBlockState()) {
                            return false;
                        }
                    }
                }
            }
        }

        for(int iX = 0; iX < 16; ++iX) {
            for(int iZ = 0; iZ < 16; ++iZ) {
                for(int iY = 0; iY < 8; ++iY) {
                    if(noise[(iX * 16 + iZ) * 8 + iY]) {
                        pos.set(x + iX, y + iY, z + iZ);
                        genRegion.setBlock(pos,
                                iY >= 4 ? Blocks.AIR.defaultBlockState() : block.defaultBlockState(), 19);
                    }
                }
            }
        }

        for(int iX = 0; iX < 16; ++iX) {
            for(int iZ = 0; iZ < 16; ++iZ) {
                for(int iY = 4; iY < 8; ++iY) {
                    pos.set(x + iX, y + iY - 1, z + iZ);
                    if(noise[(iX * 16 + iZ) * 8 + iY] &&
                            genRegion.getBlockState(pos) == Blocks.DIRT.defaultBlockState()
                            && genRegion.getLightEmission(pos) > 0) {
                        genRegion.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 19);
                    }
                }
            }
        }

        if(block == Blocks.LAVA) {
            for(int iX = 0; iX < 16; ++iX) {
                for(int iZ = 0; iZ < 16; ++iZ) {
                    for(int iY = 0; iY < 8; ++iY) {
                        condition = !noise[(iX * 16 + iZ) * 8 + iY]
                                && (iX < 15 && noise[((iX + 1) * 16 + iZ) * 8 + iY] || iX > 0
                                    && noise[((iX - 1) * 16 + iZ) * 8 + iY] || iZ < 15
                                    && noise[(iX * 16 + iZ + 1) * 8 + iY] || iZ > 0
                                    && noise[(iX * 16 + (iZ - 1)) * 8 + iY] || iY < 7
                                    && noise[(iX * 16 + iZ) * 8 + iY + 1] || iY > 0
                                    && noise[(iX * 16 + iZ) * 8 + (iY - 1)]);


                        pos.set(x + iX, y + iY, z + iZ);

                        if(condition && (iY < 4 || rand.nextInt(2) != 0)
                                && genRegion.getBlockState(pos).isSolid()) {
                            genRegion.setBlock(pos, Blocks.STONE.defaultBlockState(), 19);
                        }
                    }
                }
            }
        }

        return true;
    }
}
