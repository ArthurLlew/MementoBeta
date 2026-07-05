package net.arthurllew.mementobeta.world.levelgen.features;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;

public class WorldGenClay {
    /**
     * "Generates" clay vein from Beta 1.7.3. Is only used to modify state of random source.
     */
    @SuppressWarnings("unused")
    public static void generate(WorldGenLevel genRegion, Random rand, int x, int y, int z, int veinSize) {
        if(genRegion.getBlockState(new BlockPos(x,y,z)).is(Blocks.WATER)) {
            double startY = y + rand.nextInt(3) + 2;
            double endY = y + rand.nextInt(3) + 2;

            for(int rad = 0; rad <= veinSize; ++rad) {
                double radiusNoise = rand.nextDouble() * (double) veinSize / 16.0D;
            }
        }
    }
}
