package net.arthurllew.mementobeta.world.levelgen.noise;

import java.util.Random;

/**
 * Generic noise generator with permutations and noise offset
 */
public class NoiseGen {
    /**
     * Permutations table.
     */
    protected final int[] permutations = new int[512];

    // Noise offsets
    protected double offsetX;
    protected double offsetY;
    protected double offsetZ;

    /**
     * Constructor.
     * @param random random source
     */
    public NoiseGen(Random random) {
        // Setup noise offsets
        this.offsetX = random.nextDouble() * 256.0D;
        this.offsetY = random.nextDouble() * 256.0D;
        this.offsetZ = random.nextDouble() * 256.0D;

        // Fill half of permutations with indexes
        for (int i = 0; i < 256; ++i) {
            this.permutations[i] = i;
        }
        // Fisher-Yates shuffle
        for (int i = 0; i < 256; ++i) {
            // Random index in array
            int randIdx = random.nextInt(256 - i) + i;
            // Swap values
            int temp = this.permutations[i];
            this.permutations[i] = this.permutations[randIdx];
            this.permutations[randIdx] = temp;
            // Duplicate value to ths second half of permutations
            this.permutations[i + 256] = this.permutations[i];
        }
    }
}
