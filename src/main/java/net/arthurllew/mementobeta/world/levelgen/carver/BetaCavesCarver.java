package net.arthurllew.mementobeta.world.levelgen.carver;

import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.arthurllew.mementobeta.world.levelgen.BetaChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Random;

public class BetaCavesCarver {
    /**
     * XZ carver radius.
     */
    private static final int CHUNK_RANGE = 8;
    /**
     * Related chunk generator.
     */
    private final BetaChunkGenerator betaChunkGenerator;

    /**
     * Constructor.
     *
     * @param betaChunkGenerator related chunk generator
     */
    public BetaCavesCarver(BetaChunkGenerator betaChunkGenerator) {
        this.betaChunkGenerator = betaChunkGenerator;
    }

    /**
     * Generates caves in provided chunk with provided world seed.
     */
    public void generate(ChunkAccess chunk, long seed) {
        // Save chunk position
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // Seed adjustments
        Random rand = new Random(seed);
        long seedFactor1 = rand.nextLong() / 2L * 2L + 1L;
        long seedFactor2 = rand.nextLong() / 2L * 2L + 1L;

        // Try to generate caves inside bounding box
        for(int neighborX = chunkX - CHUNK_RANGE; neighborX <= chunkX + CHUNK_RANGE; ++neighborX) {
            for(int neighborZ = chunkZ - CHUNK_RANGE; neighborZ <= chunkZ + CHUNK_RANGE; ++neighborZ) {
                // Set seed from position
                rand.setSeed((long)neighborX * seedFactor1 + (long)neighborZ * seedFactor2 ^ seed);

                // Carve
                this.tryToCarve(chunk, rand, chunkX, chunkZ, neighborX, neighborZ);
            }
        }
    }

    /**
     * Tries to generate caves and tunnels at provided position inside chunk.
     */
    private void tryToCarve(ChunkAccess chunk, Random rand,
                            int chunkX, int chunkZ,
                            int neighborX, int neighborZ) {
        // Determine number of caves
        // Note: nested nextInt() calls bias heavily toward 0; most chunks generate
        // no caves at all; when they do, cave count is usually small.
        int cavesCount = rand.nextInt(rand.nextInt(rand.nextInt(40) + 1) + 1);
        if(rand.nextInt(15) != 0) {
            cavesCount = 0;
        }

        // Carve caves
        for(int cave = 0; cave < cavesCount; ++cave) {
            // Determine cave starting point
            double startX = SectionPos.sectionToBlockCoord(neighborX) + rand.nextInt(16);
            double startY = rand.nextInt(rand.nextInt(120) + 8);
            double startZ = SectionPos.sectionToBlockCoord(neighborZ) + rand.nextInt(16);

            // Min number of tunnels
            int tunnelsCount = 1;
            // 1/4 chance that this system starts with a room, and spawns extra tunnels
            if(rand.nextInt(4) == 0) {
                this.carveRoom(chunk, rand, chunkX, chunkZ, startX, startY, startZ);
                tunnelsCount += rand.nextInt(4);
            }

            // Carve tunnels
            for(int tunnel = 0; tunnel < tunnelsCount; ++tunnel) {
                // Tunnel direction and width
                float yaw = rand.nextFloat() * (float)Math.PI * 2.0F;
                float pitch = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float width = rand.nextFloat() * 2.0F + rand.nextFloat();

                // Carve tunnel
                this.carveTunnel(chunk, rand, chunkX, chunkZ, startX, startY, startZ, width, yaw, pitch,
                        0, 0, 1.0D);
            }
        }
    }

    /**
     * Generates cave room.
     */
    private void carveRoom(ChunkAccess chunk, Random rand,
                           int chunkX, int chunkZ,
                           double x, double y, double z) {
        this.carveTunnel(chunk, rand, chunkX, chunkZ, x, y, z, 1.0F + rand.nextFloat() * 6.0F,
                0.0F, 0.0F, -1, -1, 0.5D);
    }

    /**
     * Generates cave tunnel.
     */
    private void carveTunnel(ChunkAccess chunk, Random rand,
                             int chunkX, int chunkZ,
                             double x, double y, double z,
                             float width, float yaw, float pitch,
                             int step, int steps, double verticalScale) {
        // Prepare block position
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Middle of the chunk
        double chunkMiddleX = SectionPos.sectionToBlockCoord(chunkX) + 8;
        double chunkMiddleZ = SectionPos.sectionToBlockCoord(chunkZ) + 8;

        // Per tunnel random
        Random tunnelRand = new Random(rand.nextLong());

        // Room case
        if(steps <= 0) {
            // Determine room radius
            int range = SectionPos.sectionToBlockCoord(CHUNK_RANGE - 1);
            steps = range - tunnelRand.nextInt(range / 4);
        }

        // Room case
        boolean isRoom = false;
        if(step == -1) {
            step = steps / 2;
            isRoom = true;
        }

        // Additional branches
        int extraTunnelsCount = tunnelRand.nextInt(steps / 2) + steps / 4;

        // Modification to tunnel direction
        float tunnelTwist = (tunnelRand.nextInt(6) == 0) ? 0.92F : 0.7F;

        // For number of generation steps
        float yawFactor = 0.0F;
        float pitchFactor = 0.0F;
        for(; step < steps; ++step) {
            double tunnelHorizontalScale = 1.5D + (double)(Mth.sin((float)step * (float)Math.PI / (float)steps) * width * 1.0F);
            double tunnelVerticalScale = tunnelHorizontalScale * verticalScale;

            float pitchCos = Mth.cos(pitch);
            float pitchSin = Mth.sin(pitch);

            x += Mth.cos(yaw) * pitchCos;
            y += pitchSin;
            z += Mth.sin(yaw) * pitchCos;

            pitch *= tunnelTwist;

            pitch += pitchFactor * 0.1F;
            yaw += yawFactor * 0.1F;

            pitchFactor *= 0.9F;
            yawFactor *= 0.75F;

            pitchFactor += (tunnelRand.nextFloat() - tunnelRand.nextFloat()) * tunnelRand.nextFloat() * 2.0F;
            yawFactor += (tunnelRand.nextFloat() - tunnelRand.nextFloat()) * tunnelRand.nextFloat() * 4.0F;

            // Create additional tunnels and stop
            if(!isRoom && step == extraTunnelsCount && width > 1.0F) {
                this.carveTunnel(chunk, rand, chunkX, chunkZ, x, y, z,
                        tunnelRand.nextFloat() * 0.5F + 0.5F, yaw - (float)Math.PI * 0.5F,
                        pitch / 3.0F, step, steps, 1.0D);
                this.carveTunnel(chunk, rand, chunkX, chunkZ, x, y, z,
                        tunnelRand.nextFloat() * 0.5F + 0.5F, yaw + (float)Math.PI * 0.5F,
                        pitch / 3.0F, step, steps, 1.0D);
                return;
            }

            // Or if room || chance
            if(isRoom || tunnelRand.nextInt(4) != 0) {
                // Check stop condition
                double xTemp = x - chunkMiddleX;
                double zTemp = z - chunkMiddleZ;
                double branchesLeft = steps - step;
                double adjustedWidth = width + 18.0F;
                if(xTemp * xTemp + zTemp * zTemp - branchesLeft * branchesLeft > adjustedWidth * adjustedWidth) {
                    return;
                }

                if(x >= chunkMiddleX - 16.0D - tunnelHorizontalScale * 2.0D
                        && z >= chunkMiddleZ - 16.0D - tunnelHorizontalScale * 2.0D
                        && x <= chunkMiddleX + 16.0D + tunnelHorizontalScale * 2.0D
                        && z <= chunkMiddleZ + 16.0D + tunnelHorizontalScale * 2.0D) {

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

                    // If not water
                    if(!isWater) {
                        // Prepare block
                        BlockState block;

                        // For X coordinate
                        for(int localX = minX; localX < maxX; ++localX) {
                            // X distance
                            double dx = ((double)(localX + chunkX * 16) + 0.5D - x) / tunnelHorizontalScale;

                            // For Z coordinate
                            for(int localZ = minZ; localZ < maxZ; ++localZ) {
                                // Z distance
                                double dz = ((double)(localZ + chunkZ * 16) + 0.5D - z) / tunnelHorizontalScale;

                                // Set current Y coordinate
                                int currentY = maxY;

                                // Will be populated with appropriate top block if we carve into one
                                BlockState grass = null;

                                // Check XZ distance
                                if(dx * dx + dz * dz < 1.0D) {
                                    // For Y coordinate
                                    for(int localY = maxY - 1; localY >= minY; --localY) {
                                        // Y distance
                                        double dy = ((double)localY + 0.5D - y) / tunnelVerticalScale;

                                        // Check XYZ distance
                                        if(dy > -0.7D && dx * dx + dy * dy + dz * dz < 1.0D) {
                                            pos.set(localX, currentY, localZ);

                                            // Get block at this position
                                            block = chunk.getBlockState(pos);

                                            // Update grass condition
                                            for (Block grassBlock : betaChunkGenerator.betaSettings.value()
                                                    .grassBlocks())
                                            {
                                                if(block.is(grassBlock)) {
                                                    grass = grassBlock.defaultBlockState();
                                                }
                                            }

                                            // Carve block if it is in the appropriate list
                                            boolean canCarve = false;
                                            for (Block carverBlock : betaChunkGenerator.betaSettings.value()
                                                    .carverBlocks())
                                            {
                                                if(block.is(carverBlock)) {
                                                    canCarve = true;
                                                }
                                            }
                                            if(canCarve) {
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
                                                    if(grass != null && chunk.getBlockState(pos).is(Blocks.DIRT)) {
                                                        // Replace with grass
                                                        chunk.setBlockState(pos, grass, false);
                                                    }
                                                }
                                            }
                                        }

                                        // Move down
                                        --currentY;
                                    }
                                }
                            }
                        }

                        // If room
                        if(isRoom) {
                            // Stop generation
                            break;
                        }
                    }
                }
            }
        }
    }
}
