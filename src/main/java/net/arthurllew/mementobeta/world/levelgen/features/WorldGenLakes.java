package net.arthurllew.mementobeta.world.levelgen.features;

import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;

public class WorldGenLakes {
    /**
     * X/Z size of the lake volume.
     */
    private static final int LAKE_VOLUME_SIZE_XZ = 16;
    /**
     * Y size of the lake volume.
     */
    private static final int LAKE_VOLUME_SIZE_Y = 8;
    /**
     * Total lake volume.
     */
    private static final int LAKE_VOLUME = LAKE_VOLUME_SIZE_XZ * LAKE_VOLUME_SIZE_XZ * LAKE_VOLUME_SIZE_Y;

    /**
     * Generates lake from Beta 1.7.3.
     */
    @SuppressWarnings("deprecation")
    public static void generate(WorldGenLevel genRegion, Random rand, int x, int y, int z, Block block) {
        // Prepare mutable block position for further use
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Horizontal shift in chunk to avoid boring generation
        x -= 8;
        z -= 8;

        // Find top most air block
        pos.set(x, y, z);
        while(y > 0 && genRegion.getBlockState(pos).isAir()) {
            y--;
            pos.set(x, y, z);
        }

        // Drop into the ground
        y -= 4;

        // Lake markers inside it's volume
        boolean[] lakeVolumeMarkers = new boolean[LAKE_VOLUME];

        // Carve out several overlapping ellipsoids that form the lake
        int blobCount = rand.nextInt(4) + 4;
        for(int i = 0; i < blobCount; ++i) {
            // Get ellipsoid size and center
            double sizeX = rand.nextDouble() * 6.0D + 3.0D;
            double sizeY = rand.nextDouble() * 4.0D + 2.0D;
            double sizeZ = rand.nextDouble() * 6.0D + 3.0D;
            double centerX = rand.nextDouble() * (16.0D - sizeX - 2.0D) + 1.0D + sizeX / 2.0D;
            double centerY = rand.nextDouble() * (8.0D - sizeY - 4.0D) + 2.0D + sizeY / 2.0D;
            double centerZ = rand.nextDouble() * (16.0D - sizeZ - 2.0D) + 1.0D + sizeZ / 2.0D;

            // Iterate ellipsoid box
            for(int iX = 1; iX < LAKE_VOLUME_SIZE_XZ - 1; ++iX) {
                for(int iZ = 1; iZ < LAKE_VOLUME_SIZE_XZ - 1; ++iZ) {
                    for(int iY = 1; iY < LAKE_VOLUME_SIZE_Y - 1; ++iY) {
                        // If is inside ellipsoid
                        double dx = ((double)iX - centerX) / (sizeX / 2.0D);
                        double dy = ((double)iY - centerY) / (sizeY / 2.0D);
                        double dz = ((double)iZ - centerZ) / (sizeZ / 2.0D);
                        if(dx * dx + dy * dy + dz * dz < 1.0D) {
                            // Save coord to map
                            lakeVolumeMarkers[lakeVolumeIndex(iX, iY, iZ)] = true;
                        }
                    }
                }
            }
        }

        // Iterate lake volume
        for(int iX = 0; iX < LAKE_VOLUME_SIZE_XZ; ++iX) {
            for(int iZ = 0; iZ < LAKE_VOLUME_SIZE_XZ; ++iZ) {
                for(int iY = 0; iY < LAKE_VOLUME_SIZE_Y; ++iY) {
                    // On lakes edge
                    if(isLakeEdge(lakeVolumeMarkers, iX, iY, iZ)) {
                        // Get block at current position
                        pos.set(x + iX, y + iY, z + iZ);
                        BlockState edgeBlock = genRegion.getBlockState(pos);

                        // Upper half must not poke into existing liquid
                        if(iY >= 4 && !edgeBlock.getFluidState().isEmpty()) {
                            // Abort
                            return;
                        }

                        // Lower half must be solid ground, or already the same lake block
                        if(iY < 4 && !edgeBlock.isSolid()
                                && !genRegion.getBlockState(pos).is(block)) {
                            // Abort
                            return;
                        }
                    }
                }
            }
        }

        // Iterate lake volume
        for(int iX = 0; iX < LAKE_VOLUME_SIZE_XZ; ++iX) {
            for(int iZ = 0; iZ < LAKE_VOLUME_SIZE_XZ; ++iZ) {
                for(int iY = 0; iY < LAKE_VOLUME_SIZE_Y; ++iY) {
                    // Inside lake
                    if(lakeVolumeMarkers[lakeVolumeIndex(iX, iY, iZ)]) {
                        // Carve anf fill with liquid
                        pos.set(x + iX, y + iY, z + iZ);
                        genRegion.setBlock(pos,
                                iY >= 4 ? Blocks.AIR.defaultBlockState() : block.defaultBlockState(), 2);
                        // Avoid floating features like grass
                        markAboveForPostProcessing(genRegion, pos);
                    }
                }
            }
        }

        // Iterate lake volume
        for(int iX = 0; iX < LAKE_VOLUME_SIZE_XZ; ++iX) {
            for(int iZ = 0; iZ < LAKE_VOLUME_SIZE_XZ; ++iZ) {
                for(int iY = 4; iY < LAKE_VOLUME_SIZE_Y; ++iY) {
                    // Position below is inside lake
                    pos.set(x + iX, y + iY - 1, z + iZ);
                    if(lakeVolumeMarkers[lakeVolumeIndex(iX, iY, iZ)]
                            // and is an exposed dirt
                            && genRegion.getBlockState(pos).is(Blocks.DIRT)
                            && genRegion.getHeight(Heightmap.Types.WORLD_SURFACE, x + iX, z + iZ) == y + iY) {
                        // Turn exposed dirt into grass
                        genRegion.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                        // Avoid floating features like grass
                        markAboveForPostProcessing(genRegion, pos);
                    }
                }
            }
        }

        // If selected liquid is lava
        if(block == MementoBetaBlocks.BETA_lAVA.get()) {
            // Iterate lake volume
            for(int iX = 0; iX < LAKE_VOLUME_SIZE_XZ; ++iX) {
                for(int iZ = 0; iZ < LAKE_VOLUME_SIZE_XZ; ++iZ) {
                    for(int iY = 0; iY < LAKE_VOLUME_SIZE_Y; ++iY) {
                        // On lakes edge
                        pos.set(x + iX, y + iY, z + iZ);
                        if(isLakeEdge(lakeVolumeMarkers, iX, iY, iZ)
                                // and below water line (+ noise)
                                && (iY < 4 || rand.nextInt(2) != 0)
                                && genRegion.getBlockState(pos).isSolid()) {
                            genRegion.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
                            // Avoid floating features like grass
                            markAboveForPostProcessing(genRegion, pos);
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper function for computing lake volume array index.
     */
    private static int lakeVolumeIndex(int iX, int iY, int iZ) {
        return (iX * LAKE_VOLUME_SIZE_XZ + iZ) * LAKE_VOLUME_SIZE_Y + iY;
    }

    /**
     * A block position is treated as lake's volume edge if it is not part of the lake volume
     * but is directly adjacent (6-connected) to a marked position.
     * These block positions form the lake's boundary.
     */
    private static boolean isLakeEdge(boolean[] lakeVolumeMarkers, int iX, int iY, int iZ) {
        if (lakeVolumeMarkers[lakeVolumeIndex(iX, iY, iZ)]) {
            return false;
        }

        return (iX < LAKE_VOLUME_SIZE_XZ - 1 && lakeVolumeMarkers[lakeVolumeIndex(iX + 1, iY, iZ)])
                || (iX > 0 && lakeVolumeMarkers[lakeVolumeIndex(iX - 1, iY, iZ)])
                || (iY < LAKE_VOLUME_SIZE_Y - 1 && lakeVolumeMarkers[lakeVolumeIndex(iX, iY + 1, iZ)])
                || (iY > 0 && lakeVolumeMarkers[lakeVolumeIndex(iX, iY - 1, iZ)])
                || (iZ < LAKE_VOLUME_SIZE_XZ - 1 && lakeVolumeMarkers[lakeVolumeIndex(iX, iY, iZ + 1)])
                || (iZ > 0 && lakeVolumeMarkers[lakeVolumeIndex(iX, iY, iZ - 1)]);
    }

    /**
     * If the above two blocks are not air and can't fall, marks them for post-processing.
     * This is used to prevent floating grass during the generation of features that carve blocks out
     * of the terrain (such as lake features), after other plant-like blocks have generated.
     * This method will prevent falling blocks from updating and thus falling after generation.
     */
    protected static void markAboveForPostProcessing(WorldGenLevel genRegion, BlockPos pos) {
        BlockPos.MutableBlockPos mutableBlockPos = pos.mutable();

        for(int i = 0; i < 2; ++i) {
            // Move one block up
            mutableBlockPos.move(Direction.UP);
            // Abort if we hit air or block that can fall
            BlockState block = genRegion.getBlockState(mutableBlockPos);
            if (block.isAir() || (block.getBlock() instanceof FallingBlock)) {
                return;
            }

            // Mark for post-processing
            genRegion.getChunk(mutableBlockPos).markPosForPostprocessing(mutableBlockPos);
        }
    }
}
