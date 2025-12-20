package net.arthurllew.mementobeta.texture.procedural;

import com.mojang.blaze3d.platform.NativeImage;

public abstract class BetaProceduralTexture {
    /**
     * Generated texture data in RGBA format.
     */
    protected int[] imageData = new int[256];

    /**
     * Ticks texture data.
     */
    public abstract void tick();

    /**
     * Packs RGBA data.
     */
    protected int packRGBA(int red, int green, int blue, int alpha) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }

    /**
     * Saves current texture data to provided image.
     */
    public void copyToNativeImage(NativeImage image) {
        for (int w = 0; w < 16; w++) {
            for (int h = 0; h < 16; h++) {
                image.setPixelRGBA(w, h, imageData[h * 16 + w]);
            }
        }
    }

    /**
     * Copies current texture data to provided fluid image.
     */
    public void copyToNativeImageX2(NativeImage imageFluid) {
        for (int w = 0; w < 16; w++) {
            for (int h = 0; h < 16; h++) {
                imageFluid.setPixelRGBA(w, h, imageData[h * 16 + w]);
                imageFluid.setPixelRGBA(w + 16, h, imageData[h * 16 + w]);
                imageFluid.setPixelRGBA(w, h + 16, imageData[h * 16 + w]);
                imageFluid.setPixelRGBA(w + 16, h + 16, imageData[h * 16 + w]);
            }
        }
    }
}
