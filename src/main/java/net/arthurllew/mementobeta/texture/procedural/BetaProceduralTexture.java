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
    public void saveToNativeImage(NativeImage image) {
        for (int h = 0; h < 16; h++) {
            for (int w = 0; w < 16; w++) {
                image.setPixelRGBA(w, h, imageData[h * 16 + w]);
            }
        }
    }
}
