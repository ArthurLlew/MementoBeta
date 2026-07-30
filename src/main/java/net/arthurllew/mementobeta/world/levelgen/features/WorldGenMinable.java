package net.arthurllew.mementobeta.world.levelgen.features;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;

import java.util.Random;

public class WorldGenMinable {
    /**
     * Tag of blocks allowed for ore replacement.
     */
    private static final TagKey<Block> STONE_LIKE = TagKey.create(Registries.BLOCK,
            ResourceLocation.withDefaultNamespace("base_stone_overworld"));

    /**
     * Generates ore vein from Beta 1.7.3.
     */
    public static void generate(WorldGenLevel genRegion, Random random, int x, int y, int z,
                                Block block, int veinSize) {
        generate(genRegion, random, x, y, z, block, veinSize, true);
    }

    /**
     * Generates ore vein from Beta 1.7.3.
     * @param placeBlocks whether to actually place blocks (or just modify state of random source)
     */
    public static void generate(WorldGenLevel genRegion, Random rand, int x, int y, int z,
                                Block block, int veinSize, boolean placeBlocks) {
        // Pick a random horizontal direction and compute segment box
        float angle = rand.nextFloat() * (float) Math.PI;
        double startX = (double) (x + 8) + Math.sin(angle) * (float) veinSize / 8.0F;
        double startY = y + rand.nextInt(3) + 2;
        double startZ = (double) (z + 8) + Math.cos(angle) * (float) veinSize / 8.0F;
        double endX = (double) (x + 8) - Math.sin(angle) * (float) veinSize / 8.0F;
        double endY = y + rand.nextInt(3) + 2;
        double endZ = (double) (z + 8) - Math.cos(angle) * (float) veinSize / 8.0F;

        // Carve an ellipsoid of blocks
        for (int rad = 0; rad <= veinSize; ++rad) {
            // Radius tapers in near both ends of the vein
            double radiusNoise = rand.nextDouble() * (double) veinSize / 16.0D;
            double radius = (Math.sin((float) rad * (float) Math.PI / (float) veinSize) + 1.0F) * radiusNoise + 1.0D;

            // If not dummy
            if (placeBlocks) {
                // Center of vein box
                double t = (double) rad / (double) veinSize;
                double centerX = startX + (endX - startX) * t;
                double centerY = startY + (endY - startY) * t;
                double centerZ = startZ + (endZ - startZ) * t;

                // Get box around vein
                int minX = Mth.floor(centerX - radius / 2.0D);
                int minY = Mth.floor(centerY - radius / 2.0D);
                int minZ = Mth.floor(centerZ - radius / 2.0D);
                int maxX = Mth.floor(centerX + radius / 2.0D);
                int maxY = Mth.floor(centerY + radius / 2.0D);
                int maxZ = Mth.floor(centerZ + radius / 2.0D);

                // For X coordinate
                for (int blockX = minX; blockX <= maxX; ++blockX) {
                    // Whether X coordinate is inside ellipsoid
                    double dx = ((double) blockX + 0.5D - centerX) / (radius / 2.0D);
                    if (dx * dx >= 1.0D) {
                        continue;
                    }

                    // For Y coordinate
                    for (int blockY = minY; blockY <= maxY; ++blockY) {
                        // Whether XY coordinate is inside ellipsoid
                        double dy = ((double) blockY + 0.5D - centerY) / (radius / 2.0D);
                        if (dx * dx + dy * dy >= 1.0D) {
                            continue;
                        }

                        // For Z coordinate
                        for (int blockZ = minZ; blockZ <= maxZ; ++blockZ) {
                            // Whether XYZ coordinate is inside ellipsoid
                            double dz = ((double) blockZ + 0.5D - centerZ) / (radius / 2.0D);
                            if (dx * dx + dy * dy + dz * dz >= 1.0D) {
                                continue;
                            }

                            // Test block at current position for being replaceable
                            BlockPos pos = new BlockPos(blockX, blockY, blockZ);
                            if (genRegion.getBlockState(pos).is(STONE_LIKE)) {
                                // Set ore
                                genRegion.setBlock(pos, block.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }
        }
    }
}
