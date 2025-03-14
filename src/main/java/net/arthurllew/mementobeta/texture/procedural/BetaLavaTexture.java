package net.arthurllew.mementobeta.texture.procedural;

import net.minecraft.util.Mth;

public class BetaLavaTexture extends BetaProceduralTexture {
    protected float[] intensities = new float[256];
    protected float[] intensities_prev = new float[256];
    protected float[] intensities_2 = new float[256];
    protected float[] intensities_3 = new float[256];

    /**
     * Stores flow state.
     */
    private int flowState = 0;

    /**
     * whether lava is flowing or not.
     */
    private final boolean isFlowing;

    /**
     * Constructor.
     * @param isFlowing whether lava is flowing or not.
     */
    public BetaLavaTexture(boolean isFlowing) {
        this.isFlowing = isFlowing;
    }

    public void tick() {
        // Variables pre-init in beta code style :)
        float intensity;
        int blue;
        int green;
        int red;
        int i, j;
        int k, n ,m;
        int index;

        if (this.isFlowing) {
            // Update state
            this.flowState++;
        }

        // Calculate new intensities
        for(j = 0; j < 16; ++j) {
            for(i = 0; i < 16; ++i) {
                index = i * 16 + j;

                intensity = 0.0F;

                m = (int)(Math.sin((float)i * (float)Math.PI * 2.0F / 16.0F) * 1.2F);
                blue = (int)(Math.sin((float)j * (float)Math.PI * 2.0F / 16.0F) * 1.2F);

                for(green = j - 1; green <= j + 1; ++green) {
                    for(red = i - 1; red <= i + 1; ++red) {
                        k = green + m & 15;
                        n = red + blue & 15;
                        intensity += this.intensities[k + n * 16];
                    }
                }

                this.intensities_prev[index] = intensity / 10.0F
                        + (this.intensities_2[(j + 0 & 15) + (i + 0 & 15) * 16]
                        + this.intensities_2[(j + 1 & 15) + (i + 0 & 15) * 16]
                        + this.intensities_2[(j + 1 & 15) + (i + 1 & 15) * 16]
                        + this.intensities_2[(j + 0 & 15) + (i + 1 & 15) * 16]) / 4.0F * 0.8F;

                this.intensities_2[index] += this.intensities_3[index] * 0.01F;
                if(this.intensities_2[index] < 0.0F) {
                    this.intensities_2[index] = 0.0F;
                }

                this.intensities_3[index] -= 0.06F;
                if(Math.random() < 0.005D) {
                    this.intensities_3[index] = 1.5F;
                }
            }
        }

        // Swap intensities
        float[] buf = this.intensities_prev;
        this.intensities_prev = this.intensities;
        this.intensities = buf;

        // Apply new intensities
        for(i = 0; i < 256; i++) {
            intensity = this.intensities[this.isFlowing ? (i - this.flowState / 3 * 16) & 255 : i] * 2.0F;

            // Clamp
            intensity = Mth.clamp(intensity, 0.0F, 1.0F);

            // RGBA
            red = (int)(intensity * intensity * intensity * intensity * 128.0F);
            green = (int)(intensity * intensity * 255.0F);
            blue = (int)(intensity * 100.0F + 155.0F);

            // Pack image data
            this.imageData[i] = this.packRGBA(red, green, blue, 255);
        }
    }
}
