package net.arthurllew.mementobeta.texture.procedural;

import net.minecraft.util.Mth;

public class BetaFlameTexture extends BetaProceduralTexture {
    protected float[] intensities = new float[320];
    protected float[] intensities_prev = new float[320];

    public void tick() {
        // Variables pre-init in beta code style :)
        float intensity;
        int blue;
        int green;
        int red;
        int alpha;
        int i, j;

        // Calculate new intensities
        for(i = 0; i < 16; ++i) {
            for(j = 0; j < 20; ++j) {

                int k = 18;

                intensity = this.intensities[i + (j + 1) % 20 * 16] * (float)k;

                for(blue = i - 1; blue <= i + 1; ++blue) {
                    for(green = j; green <= j + 1; ++green) {
                        if(blue >= 0 && green >= 0 && blue < 16 && green < 20) {
                            intensity += this.intensities[blue + green * 16];
                        }

                        ++k;
                    }
                }

                this.intensities_prev[i + j * 16] = intensity / ((float)k * 1.06F);
                if(j >= 19) {
                    this.intensities_prev[i + j * 16] = (float)(Math.random() * Math.random() * Math.random()
                            * 4.0D + Math.random() * (double)0.1F + (double)0.2F);
                }
            }
        }

        // Swap intensities
        float[] buf = this.intensities_prev;
        this.intensities_prev = this.intensities;
        this.intensities = buf;

        // Apply new intensities
        for(i = 0; i < 256; i++) {
            intensity = this.intensities[i] * 1.8F;

            // Clamp
            intensity = Mth.clamp(intensity, 0.0F, 1.0F);

            // RGBA
            red = (int)(intensity * intensity * intensity * intensity * intensity * intensity
                    * intensity * intensity * intensity * intensity * 255.0F);
            green = (int)(intensity * intensity * 255.0F);
            blue = (int)(intensity * 155.0F + 100.0F);
            alpha = intensity < 0.5F ? 0 : 255;

            // Pack image data
            this.imageData[i] = this.packRGBA(red, green, blue, alpha);
        }
    }
}
