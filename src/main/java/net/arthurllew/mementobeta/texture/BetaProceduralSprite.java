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

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class BetaProceduralSprite extends SpriteContents {
    /**
     * Beta 1.7.3 procedural texture (like lava, fire, etc.).
     */
    protected final BetaProceduralTexture betaProceduralTexture;

    /**
     * Wrap constructor.
     */
    public BetaProceduralSprite(ResourceLocation pName, FrameSize pFrameSize, NativeImage pOriginalImage,
                                NativeImage[] byMipLevel, BetaProceduralTexture betaProceduralTexture) {
        // Steal all sprite data
        super(pName, pFrameSize, pOriginalImage, AnimationMetadataSection.EMPTY, null);
        this.byMipLevel = byMipLevel;

        // Set procedural texture
        this.betaProceduralTexture = betaProceduralTexture;
    }

    /**
     * @return custom sprite ticker.
     */
    @Override
    @Nullable
    public SpriteTicker createTicker() {
        return new BetaTicker(this);
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
    record BetaTicker(BetaProceduralSprite connectedSprite) implements SpriteTicker {
        /**
         * Ticks procedural beta texture the good old-fashion way.
         */
        public void tickAndUpload(int x, int y) {
            // Tick procedural texture
            this.connectedSprite.betaProceduralTexture.tick();

            // Iterate over mip level sprites.
            for (int level = 0; level < this.connectedSprite.byMipLevel.length; ++level) {
                int mipLevelHeight = this.connectedSprite.height() >> level;
                int mipLevelWidth = this.connectedSprite.width() >> level;

                // Skip uploading if mip level texture has 0 size
                if (mipLevelWidth <= 0 || mipLevelHeight <= 0)
                    break;

                // Write new texture data to image
                if (level == 0) {
                    this.connectedSprite.betaProceduralTexture
                            .saveToNativeImage(this.connectedSprite.byMipLevel[level]);
                }

                // Update sprite
                this.connectedSprite.byMipLevel[level].upload(level, x >> level, y >> level,
                        0, 0, mipLevelWidth, mipLevelHeight,
                        this.connectedSprite.byMipLevel.length > 1, false);
            }
        }

        /**
         * There is nothing to close in this ticker.
         */
        public void close() {}
    }
}
