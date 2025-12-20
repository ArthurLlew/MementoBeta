package net.arthurllew.mementobeta.texture;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/**
 * Unlocks constructor in {@link TextureAtlasSpriteWrapper}.
 */
public class TextureAtlasSpriteWrapper extends TextureAtlasSprite {
    /**
     * Constructor wrapper.
     */
    public TextureAtlasSpriteWrapper(ResourceLocation atlasLocation, SpriteContents contents,
                                        int originX, int originY, int x, int y) {
        super(atlasLocation, contents, originX, originY, x, y);
    }
}
