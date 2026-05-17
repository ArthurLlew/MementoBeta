package net.arthurllew.mementobeta.world.levelgen.features;

import com.mojang.serialization.Codec;
import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.Random;

public class BetaFeatures extends Feature<BetaFeaturesConfig> {
    /**
     * Constructor matching super.
     */
    public BetaFeatures(Codec<BetaFeaturesConfig> codec) {
        super(codec);
    }

    /**
     * Places the given feature at the given location.
     * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk
     * being generated, that they can safely generate into.
     *
     * @param context A context object with a reference to the level and the position the feature is being placed at
     */
    public final boolean place(FeaturePlaceContext<BetaFeaturesConfig> context) {
        // Get context data
        BlockPos.MutableBlockPos pos = context.origin().mutable();
        final WorldGenLevel genRegion = context.level();
        ChunkAccess chunk = genRegion.getChunk(pos);

        //=====================================================================================================
        // In Vanilla Beta 1.7.3 chunk decoration is done by ChunkProviderGenerate.populate(...) method.
        // Water/Lava lakes are generated first, followed by dungeons. Then mobs/trees/grass and other
        // decoration items are placed. At the end the snow layer is generated. We will only mimic water/lava
        // lakes and dungeons. Other decorations will be provided by biome. Although it will not
        // reproduce Vanilla Beta 1.7.3 foliage setup, I believe modern biome decorations are better.
        //=====================================================================================================

        // Chunk position
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // World position
        int x = chunk.getPos().x * 16;
        int z = chunk.getPos().z * 16;

        // Create random class with chunk relative seed
        Random rand = new Random(genRegion.getSeed());
        long v1 = rand.nextLong() / 2L * 2L + 1L;
        long v2 = rand.nextLong() / 2L * 2L + 1L;
        rand.setSeed((long)chunkX * v1 + (long)chunkZ * v2 ^ genRegion.getSeed());

        // Helper position variables
        int genX;
        int genY;
        int genZ;

        // Try to generate water lake
        if(rand.nextInt(4) == 0) {
            genX = x + rand.nextInt(16) + 8;
            genY = rand.nextInt(128);
            genZ = z + rand.nextInt(16) + 8;
            WorldGenLakes.generate(genRegion, rand, genX, genY, genZ, Blocks.WATER);
        }

        // Try to generate lava lake
        if(rand.nextInt(8) == 0) {
            genX = x + rand.nextInt(16) + 8;
            genY = rand.nextInt(rand.nextInt(120) + 8);
            genZ = z + rand.nextInt(16) + 8;
            if(genY < 64 || rand.nextInt(10) == 0) {
                WorldGenLakes.generate(genRegion, rand, genX, genY, genZ, MementoBetaBlocks.BETA_lAVA.get());
            }
        }

        // Try to generate dungeon
        for(int i = 0; i < 8; ++i) {
            genX = x + rand.nextInt(16) + 8;
            genY = rand.nextInt(128);
            genZ = z + rand.nextInt(16) + 8;
            WorldGenDungeons.generate(genRegion, rand, genX, genY, genZ);
        }

        // Always return true
        return true;
    }
}
