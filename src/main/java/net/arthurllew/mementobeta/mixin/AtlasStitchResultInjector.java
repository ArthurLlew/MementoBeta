package net.arthurllew.mementobeta.mixin;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.texture.BetaProceduralSprite;
import net.arthurllew.mementobeta.texture.TextureAtlasSpriteWrapper;
import net.arthurllew.mementobeta.texture.procedural.BetaFlameTexture;
import net.arthurllew.mementobeta.texture.procedural.BetaLavaTexture;
import net.arthurllew.mementobeta.texture.procedural.BetaProceduralTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Modifies {@link AtlasSet.StitchResult} behaviour.
 */
@Mixin(AtlasSet.StitchResult.class)
public class AtlasStitchResultInjector {
    /**
     * Beta fire texture location.
     */
    private final ResourceLocation betaFireTexture = new ResourceLocation(MementoBeta.MODID, "block/beta_fire");

    /**
     * Beta lava texture location.
     */
    private final ResourceLocation betaLavaTexture = new ResourceLocation(MementoBeta.MODID, "block/beta_lava_flow");


    /**
     * Texture atlas field.
     */
    @Final
    @Shadow
    private TextureAtlas atlas;

    /**
     * Stitch preparations field.
     */
    @Final
    @Shadow
    private SpriteLoader.Preparations preparations;

    /**
     * Injects code into {@link AtlasSet} constructor. Introduces new texture atlas.
     */
    @Inject(at = @At("HEAD"), method = "upload")
    public void injectGetBlastResistance(CallbackInfo ci) {
        // Filter block texture atlas
        if (atlas.location().getPath().equals("textures/atlas/blocks.png")) {
            // Beta fire sprite
            tryReplaceSprite(betaFireTexture, new BetaFlameTexture());

            // Beta lava sprite
            tryReplaceSprite(betaLavaTexture, new BetaLavaTexture(true));
        }
    }

    /**
     * Tries to replace Vanilla sprite with a custom procedural version.
     * @param spriteLocation sprite resource location.
     * @param betaProceduralTexture procedural texture.
     */
    private void tryReplaceSprite(ResourceLocation spriteLocation, BetaProceduralTexture betaProceduralTexture) {
        // Try to find sprite
        TextureAtlasSprite atlasSprite = preparations.regions().get(spriteLocation);
        if (atlasSprite != null) {
            // Get sprite contents
            SpriteContents spriteContents = atlasSprite.contents();

            // Create procedural sprite
            BetaProceduralSprite proceduralSprite = new BetaProceduralSprite(spriteContents.name(),
                    new FrameSize(spriteContents.width(), spriteContents.height()),
                    spriteContents.getOriginalImage(), spriteContents.byMipLevel,
                    betaProceduralTexture);

            // Create custom texture atlas sprite
            TextureAtlasSprite atlasProceduralSprite = new TextureAtlasSpriteWrapper(spriteLocation,
                    proceduralSprite,
                    (int)(atlasSprite.getX() * atlasSprite.getU0()),
                    (int)(atlasSprite.getY() * atlasSprite.getU1()),
                    atlasSprite.getX(), atlasSprite.getY());

            // Replace value with custom texture atlas
            preparations.regions().put(spriteLocation, atlasProceduralSprite);
        }
    }
}
