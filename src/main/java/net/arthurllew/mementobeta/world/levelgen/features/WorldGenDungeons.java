package net.arthurllew.mementobeta.world.levelgen.features;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class WorldGenDungeons {
    /**
     * Interior height.
     */
    private static final byte ROOM_HEIGHT = 3;

    /**
     * Generates dungeon from Beta 1.7.3.
     */
    @SuppressWarnings({"deprecation", "DataFlowIssue"})
    public static void generate(WorldGenLevel genRegion, Random rand, int x, int y, int z) {
        // Prepare block position
        BlockPos.MutableBlockPos pos1 = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos pos2 = new BlockPos.MutableBlockPos();

        // Compute dungeon size
        int halfSizeX = rand.nextInt(2) + 2;
        int halfSizeZ = rand.nextInt(2) + 2;

        // Get box around dungeon
        int minX = x - halfSizeX - 1;
        int maxX = x + halfSizeX + 1;
        int minY = y - 1;
        int maxY = y + ROOM_HEIGHT + 1;
        int minZ = z - halfSizeZ - 1;
        int maxZ = z + halfSizeZ + 1;

        // Iterate room box to validate floor/ceiling and count the number of air blocks on the room's edge
        int airGaps = 0;
        for(int iX = minX; iX <= maxX; ++iX) {
            for(int iY = minY; iY <= maxY; ++iY) {
                for(int iZ = minZ; iZ <= maxZ; ++iZ) {
                    // Current position
                    pos1.set(iX, iY, iZ);
                    // Position above
                    pos2.set(iX, iY + 1, iZ);
                    // Current block
                    BlockState block = genRegion.getBlockState(pos1);

                    // If non-solid floor
                    if(iY == minY && !block.isSolid()) {
                        // Abort
                        return;
                    }

                    // If non-solid ceiling (doesn't check for sand that will fall though :) )
                    if(iY == maxY && !block.isSolid()) {
                        // Abort
                        return;
                    }

                    // If XZ edge of room + Y == room origin position + block at current position or above is air
                    if((iX == minX || iX == maxX
                            || iZ == minZ || iZ == maxZ)
                            && iY == y
                            && genRegion.getBlockState(pos1).isAir()
                            && genRegion.getBlockState(pos2).isAir()) {
                        ++airGaps;
                    }
                }
            }
        }

        // None or too many air blocks found
        if(airGaps < 1 || airGaps > 5) {
            return;
        }

        // Generate dungeon shell
        for(int iX = minX; iX <= maxX; ++iX) {
            for(int iY = maxY - 1; iY >= minY; --iY) {
                for(int iZ = minZ; iZ <= maxZ; ++iZ) {
                    // Current position
                    pos1.set(iX, iY, iZ);
                    // Position below
                    pos2.set(iX, iY - 1, iZ);

                    // Place air inside dungeon
                    if(iX != minX && iX != maxX
                            && iY != minY && iY != maxY
                            && iZ != minZ && iZ != maxZ) {
                        genRegion.setBlock(pos1, Blocks.AIR.defaultBlockState(), 3);

                        // Update blocks above dungeon air, so falling blocks (like sand) will fall inside
                        // just like in Beta 1.7.3
                        if (iY == y + ROOM_HEIGHT) {
                            pos1.move(Direction.UP);
                            BlockState block = genRegion.getBlockState(pos1);
                            if (!block.isAir()) {
                                genRegion.getChunk(pos1).markPosForPostprocessing(pos1);
                            }
                        }
                    }
                    // If space below floor is not occupied we leave floor empty
                    else if(iY >= 0 && !genRegion.getBlockState(pos2).isSolid()) {
                        genRegion.setBlock(pos1, Blocks.AIR.defaultBlockState(), 3);
                    }
                    // Floor and walls
                    else if(genRegion.getBlockState(pos1).isSolid()) {
                        // Mossy cobblestone patches on the floor
                        if(iY == minY && rand.nextInt(4) != 0) {
                            genRegion.setBlock(pos1, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 3);
                        // Walls and the rest of the floor
                        }
                        else {
                            genRegion.setBlock(pos1, Blocks.COBBLESTONE.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        // Generate chests with loot
        chestLoopLabel:
        // For max of 2 chests
        for(int chestIdx = 0; chestIdx < 2; ++chestIdx) {
            // Make 3 attempts
            for(int attempt = 0; attempt < 3; ++attempt) {
                // Chest position
                int chestX = x + rand.nextInt(halfSizeX * 2 + 1) - halfSizeX;
                int chestZ = z + rand.nextInt(halfSizeZ * 2 + 1) - halfSizeZ;
                pos1.set(chestX, y, chestZ);

                // If spot is empty
                if(genRegion.getBlockState(pos1).isAir()) {
                    // Number of solid blocks around current spot
                    int solidNeighborCount = 0;

                    // Check neighboring position
                    pos2.set(chestX - 1, y, chestZ);
                    if(genRegion.getBlockState(pos2).isSolid()) {
                        ++solidNeighborCount;
                    }

                    // Check neighboring position
                    pos2.set(chestX + 1, y, chestZ);
                    if(genRegion.getBlockState(pos2).isSolid()) {
                        ++solidNeighborCount;
                    }

                    // Check neighboring position
                    pos2.set(chestX, y, chestZ - 1);
                    if(genRegion.getBlockState(pos2).isSolid()) {
                        ++solidNeighborCount;
                    }

                    // Check neighboring position
                    pos2.set(chestX, y, chestZ + 1);
                    if(genRegion.getBlockState(pos2).isSolid()) {
                        ++solidNeighborCount;
                    }

                    // Place oly against one wall
                    if(solidNeighborCount == 1) {
                        // Place chest
                        genRegion.setBlock(pos1, Blocks.CHEST.defaultBlockState(), 2);
                        ChestBlockEntity chest = (ChestBlockEntity)genRegion.getBlockEntity(pos1);

                        // Make 8 attempts to generate loot
                        for (int slot = 0; slot < 8; ++slot) {
                            ItemStack loot = pickLootItem(rand);
                            if(loot != null) {
                                chest.setItem(rand.nextInt(chest.getContainerSize()), loot);
                            }
                        }

                        // Success of generating one chest
                        continue chestLoopLabel;
                    }
                }
            }
        }

        // Place spawner
        pos1.set(x, y, z);
        genRegion.setBlock(pos1, Blocks.SPAWNER.defaultBlockState(), 3);
        SpawnerBlockEntity spawner = (SpawnerBlockEntity)genRegion.getBlockEntity(pos1);
        spawner.setEntityId(pickMobSpawner(rand), genRegion.getRandom());
    }

    /**
     * Generates loot item for chest.
     * @param rand random source
     * @return item stack
     */
    private static ItemStack pickLootItem(Random rand) {
        return switch (rand.nextInt(11)) {
            case 0 -> new ItemStack(Items.SADDLE);
            case 1 -> new ItemStack(Items.IRON_INGOT, rand.nextInt(4) + 1);
            case 2 -> new ItemStack(Items.BREAD);
            case 3 -> new ItemStack(Items.WHEAT, rand.nextInt(4) + 1);
            case 4 -> new ItemStack(Items.GUNPOWDER, rand.nextInt(4) + 1);
            case 5 -> new ItemStack(Items.STRING, rand.nextInt(4) + 1);
            case 6 -> new ItemStack(Items.BUCKET);
            case 7 -> rand.nextInt(100) == 0 ?
                    new ItemStack(Items.ENCHANTED_GOLDEN_APPLE) : null;
            case 8 -> rand.nextInt(2) == 0 ?
                    new ItemStack(Items.REDSTONE, rand.nextInt(4) + 1) : null;
            case 9 -> rand.nextInt(10) == 0 ?
                    new ItemStack(rand.nextInt(2) == 0 ? Items.MUSIC_DISC_13 : Items.MUSIC_DISC_CAT) : null;
            case 10 -> new ItemStack(Items.COCOA_BEANS);
            default -> null;
        };
    }

    /**
     * Selects entity for mob spawner.
     * @param rand random source
     * @return entity type
     */
    private static EntityType<?> pickMobSpawner(Random rand) {
        return switch(rand.nextInt(4)) {
            case 0 -> EntityType.SKELETON;
            case 1, 2 -> EntityType.ZOMBIE;
            default -> EntityType.SPIDER;
        };
    }
}
