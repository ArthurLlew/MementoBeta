package net.arthurllew.mementobeta.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.arthurllew.mementobeta.texture.procedural.BetaProceduralTexture;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Beta 1.7.3 procedural texture sprite.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class BetaProceduralSprite extends SpriteContents {
    /**
     * Beta 1.7.3 procedural texture (like lava, fire, etc.).
     */
    protected final BetaProceduralTexture betaProceduralTexture;

    /**
     * Whether ticker of this sprite can generate new texture frame on tick.
     */
    final boolean canTick;
    /**
     * Whether this texture belongs to fluid.
     */
    final boolean isFluid;

    /**
     * Beta 1.7.3 procedural texture sprite.
     */
    public BetaProceduralSprite(ResourceLocation name, FrameSize frameSize, NativeImage originalImage,
                                NativeImage[] byMipLevel, BetaProceduralTexture betaProceduralTexture,
                                boolean canTick, boolean isFluid) {
        // Steal all sprite data
        super(name, frameSize, originalImage, AnimationMetadataSection.EMPTY, null);
        this.byMipLevel = byMipLevel;

        // Set procedural texture
        this.betaProceduralTexture = betaProceduralTexture;

        // Save settings
        this.canTick = canTick;
        this.isFluid = isFluid;
    }

    /**
     * @return custom sprite ticker.
     */
    @Override
    @Nullable
    public SpriteTicker createTicker() {
        return new BetaTicker(this, this.canTick, this.isFluid);
    }

    /**
     * @return custom class info.
     */
    @Override
    public String toString() {
        return "BetaProceduralSprite{name=" + this.name() + ", height=" + this.height() + ", width="
                + this.width() + "}";
    }

    /**
     * Custom texture ticker.
     * @param connectedSprite connected sprite.
     */
    @OnlyIn(Dist.CLIENT)
    record BetaTicker(BetaProceduralSprite connectedSprite, boolean canTick, boolean isFluid) implements SpriteTicker {
        /**
         * Ticks procedural beta texture the good old-fashion way.
         */
        @Override
        public void tickAndUpload(int x, int y) {
            // Tick procedural texture
            if (this.canTick)
                this.connectedSprite.betaProceduralTexture.tick();

            // Iterate over mip level sprites.
            for (int level = 0; level < this.connectedSprite.byMipLevel.length; ++level) {
                // Sizes
                int mipLevelHeight = this.connectedSprite.height() >> level;
                int mipLevelWidth = this.connectedSprite.width() >> level;

                // Skip uploading if mip level texture has 0 size
                if (mipLevelWidth <= 0 || mipLevelHeight <= 0)
                    break;

                // Write new texture data to image
                if (level == 0) {
                    // Choose copy
                    if (this.isFluid) {
                        this.connectedSprite.betaProceduralTexture
                                .copyToNativeImageX2(this.connectedSprite.byMipLevel[0]);
                    }
                    else {
                        this.connectedSprite.betaProceduralTexture
                                .copyToNativeImage(this.connectedSprite.byMipLevel[0]);
                    }
                }
                // Generate mip level
                else {
                    for(int w = 0; w < mipLevelWidth; ++w) {
                        for(int h = 0; h < mipLevelHeight; ++h) {
                            this.connectedSprite.byMipLevel[level]
                                    .setPixelRGBA(w, h, colorBlend(
                                            this.connectedSprite.byMipLevel[level - 1]
                                                    .getPixelRGBA(w * 2, h * 2),
                                            this.connectedSprite.byMipLevel[level - 1]
                                                    .getPixelRGBA(w * 2 + 1, h * 2),
                                            this.connectedSprite.byMipLevel[level - 1]
                                                    .getPixelRGBA(w * 2, h * 2 + 1),
                                            this.connectedSprite.byMipLevel[level - 1]
                                                    .getPixelRGBA(w * 2 + 1, h * 2 + 1)));
                        }
                    }
                }

                // Update sprite
                this.connectedSprite.byMipLevel[level].upload(level, x >> level, y >> level,
                        0, 0, mipLevelWidth, mipLevelHeight,
                        this.connectedSprite.byMipLevel.length > 1, false);
            }
        }

        /**
         * @return colors blended via averaged sum.
         */
        protected static int colorBlend(int col0, int col1, int col2, int col3) {
            return (gammaBlend(col0, col1, col2, col3, 24) << 24)
                    | (gammaBlend(col0, col1, col2, col3, 16) << 16)
                    | (gammaBlend(col0, col1, col2, col3, 8) << 8)
                    | gammaBlend(col0, col1, col2, col3, 0);
        }

        /**
         * @return colors channel blended via averaged sum.
         */
        protected static int gammaBlend(int col0, int col1, int col2, int col3, int bitOffset) {
            return (((col0 >> bitOffset) & 255)
                    + ((col1 >> bitOffset) & 255)
                    + ((col2 >> bitOffset) & 255)
                    + ((col3 >> bitOffset) & 255)) / 4;
        }

        /**
         * There is nothing to close in this ticker.
         */
        @Override
        public void close() {}
    }
}
