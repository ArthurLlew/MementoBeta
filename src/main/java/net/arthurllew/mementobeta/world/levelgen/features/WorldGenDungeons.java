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
    @SuppressWarnings({"deprecation", "DataFlowIssue"})
    public static boolean generate(WorldGenLevel genRegion, Random rand, int x, int y, int z) {
        // Prepare block position
        BlockPos.MutableBlockPos pos1 = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos pos2 = new BlockPos.MutableBlockPos();

        byte dungeonHeight = 3;
        int randX = rand.nextInt(2) + 2;
        int randZ = rand.nextInt(2) + 2;
        int possibleLocations = 0;

        // Try to locate cave-like empty space underground
        for(int iX = x - randX - 1; iX <= x + randX + 1; ++iX) {
            for(int iY = y - 1; iY <= y + dungeonHeight + 1; ++iY) {
                for(int iZ = z - randZ - 1; iZ <= z + randZ + 1; ++iZ) {
                    pos1.set(iX, iY, iZ);
                    pos2.set(iX, iY + 1, iZ);

                    BlockState block = genRegion.getBlockState(pos1);
                    if(iY == y - 1 && !block.isSolid()) {
                        return false;
                    }

                    if(iY == y + dungeonHeight + 1 && !block.isSolid()) {
                        return false;
                    }

                    if((iX == x - randX - 1
                            || iX == x + randX + 1
                            || iZ == z - randZ - 1
                            || iZ == z + randZ + 1)
                            && iY == y
                            && genRegion.getBlockState(pos1).isAir()
                            && genRegion.getBlockState(pos2).isAir()) {
                        ++possibleLocations;
                    }
                }
            }
        }

        // If there are from 1 to 5 possible locations
        if(possibleLocations >= 1 && possibleLocations <= 5) {
            // Generate dungeon shell
            for(int iX = x - randX - 1; iX <= x + randX + 1; ++iX) {
                for(int iY = y + dungeonHeight; iY >= y - 1; --iY) {
                    for(int iZ = z - randZ - 1; iZ <= z + randZ + 1; ++iZ) {
                        pos1.set(iX, iY, iZ);
                        pos2.set(iX, iY - 1, iZ);

                        // Air inside dungeon
                        if(iX != x - randX - 1
                                && iY != y - 1
                                && iZ != z - randZ - 1
                                && iX != x + randX + 1
                                && iY != y + dungeonHeight + 1
                                && iZ != z + randZ + 1) {
                            genRegion.setBlock(pos1, Blocks.AIR.defaultBlockState(), 3);

                            // Update blocks above dungeon air, so falling blocks (like sand) will fall inside
                            if (iY == y + dungeonHeight) {
                                pos1.move(Direction.UP);
                                BlockState block = genRegion.getBlockState(pos1);
                                if (!block.isAir()) {
                                    genRegion.getChunk(pos1).markPosForPostprocessing(pos1);
                                }
                            }
                        // If space below floor is not occupied we leave floor empty
                        }
                        else if(iY >= 0 && !genRegion.getBlockState(pos2).isSolid()) {
                            genRegion.setBlock(pos1, Blocks.AIR.defaultBlockState(), 3);
                        // Floor and walls
                        }
                        else if(genRegion.getBlockState(pos1).isSolid()) {
                            // Mossy cobblestone patches on the floor
                            if(iY == y - 1 && rand.nextInt(4) != 0) {
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
            loopLabel:
            for(int iX = 0; iX < 2; ++iX) {
                for(int iZ = 0; iZ < 3; ++iZ) {
                    int newRandX = x + rand.nextInt(randX * 2 + 1) - randX;
                    int newRandZ = z + rand.nextInt(randZ * 2 + 1) - randZ;

                    pos1.set(newRandX, y, newRandZ);

                    if(genRegion.getBlockState(pos1).isAir()) {
                        int cond = 0;

                        pos2.set(newRandX - 1, y, newRandZ);
                        if(genRegion.getBlockState(pos2).isSolid()) {
                            ++cond;
                        }

                        pos2.set(newRandX + 1, y, newRandZ);
                        if(genRegion.getBlockState(pos2).isSolid()) {
                            ++cond;
                        }

                        pos2.set(newRandX, y, newRandZ - 1);
                        if(genRegion.getBlockState(pos2).isSolid()) {
                            ++cond;
                        }

                        pos2.set(newRandX, y, newRandZ + 1);
                        if(genRegion.getBlockState(pos2).isSolid()) {
                            ++cond;
                        }

                        if(cond == 1) {
                            genRegion.setBlock(pos1, Blocks.CHEST.defaultBlockState(), 2);
                            ChestBlockEntity chest = (ChestBlockEntity)genRegion.getBlockEntity(pos1);
                            int loopAttempts = 0;

                            while(true) {
                                if(loopAttempts >= 8) {
                                    continue loopLabel;
                                }

                                ItemStack loot = pickLootItem(rand);
                                if(loot != null) {
                                    chest.setItem(rand.nextInt(chest.getContainerSize()), loot);
                                }

                                ++loopAttempts;
                            }
                        }
                    }
                }
            }

            // Place spawner
            pos1.set(x, y, z);
            genRegion.setBlock(pos1, Blocks.SPAWNER.defaultBlockState(), 3);
            SpawnerBlockEntity spawner = (SpawnerBlockEntity)genRegion.getBlockEntity(pos1);
            spawner.setEntityId(pickMobSpawner(rand), genRegion.getRandom());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Generates loot item for chest.
     *
     * @param rand Random object
     *
     * @return item stack
     */
    private static ItemStack pickLootItem(Random rand) {
        int i = rand.nextInt(11);
        return i == 0 ? new ItemStack(Items.SADDLE)
                : (i == 1 ? new ItemStack(Items.IRON_INGOT, rand.nextInt(4) + 1)
                : (i == 2 ? new ItemStack(Items.BREAD)
                : (i == 3 ? new ItemStack(Items.WHEAT, rand.nextInt(4) + 1)
                : (i == 4 ? new ItemStack(Items.GUNPOWDER, rand.nextInt(4) + 1)
                : (i == 5 ? new ItemStack(Items.STRING, rand.nextInt(4) + 1)
                : (i == 6 ? new ItemStack(Items.BUCKET)
                : (i == 7 && rand.nextInt(100) == 0 ?
                    new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)
                : (i == 8 && rand.nextInt(2) == 0 ?
                    new ItemStack(Items.REDSTONE, rand.nextInt(4) + 1)
                : (i == 9 && rand.nextInt(10) == 0 ?
                    new ItemStack(rand.nextInt(2) == 0 ? Items.MUSIC_DISC_13 : Items.MUSIC_DISC_CAT)
                : (i == 10 ? new ItemStack(Items.COCOA_BEANS) : null))))))))));
    }

    /**
     * Selects entity for mob spawner.
     *
     * @param rand item stack
     *
     * @return entity type
     */
    private static EntityType<?> pickMobSpawner(Random rand) {
        int variant = rand.nextInt(4);

        if (variant == 0) {
            return EntityType.SKELETON;
        }
        if (variant == 1) {
            return EntityType.ZOMBIE;
        }
        if (variant == 2) {
            return EntityType.ZOMBIE;
        }
        return EntityType.SPIDER;
    }
}
