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
    private final ResourceLocation betaFireTexture =
            new ResourceLocation(MementoBeta.MODID, "block/beta_fire");
    /**
     * Beta still lava texture location.
     */
    private final ResourceLocation betaLavaTexture =
            new ResourceLocation(MementoBeta.MODID, "block/beta_lava");
    /**
     * Beta flowing lava texture location.
     */
    private final ResourceLocation betaLavaFlowTexture =
            new ResourceLocation(MementoBeta.MODID, "block/beta_lava_flow");
    /**
     * Beta flowing lava texture location.
     */
    private final ResourceLocation betaPortalTexture =
            new ResourceLocation(MementoBeta.MODID, "block/beta_portal");


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
            tryReplaceSprite(betaFireTexture, new BetaFlameTexture(), true, false);

            // Beta still lava sprite
            tryReplaceSprite(betaLavaTexture, new BetaLavaTexture(false), true, false);

            BetaLavaTexture flowingLavaTexture = new BetaLavaTexture(true);

            // Beta flowing lava sprite
            tryReplaceSprite(betaLavaFlowTexture, flowingLavaTexture, true, true);

            // Beta portal sprite
            tryReplaceSprite(betaPortalTexture, flowingLavaTexture, false, false);
        }
    }

    /**
     * Tries to replace Vanilla sprite with a custom procedural version.
     * @param spriteLocation sprite resource location.
     * @param betaProceduralTexture procedural texture.
     * @param canTick whether ticker of this sprite can generate new texture frame on tick.
     * @param isFluid whether this texture belongs to fluid.
     */
    private void tryReplaceSprite(ResourceLocation spriteLocation, BetaProceduralTexture betaProceduralTexture,
                                  boolean canTick, boolean isFluid) {
        // Try to find sprite
        TextureAtlasSprite atlasSprite = preparations.regions().get(spriteLocation);
        if (atlasSprite != null) {
            // Get sprite contents
            SpriteContents spriteContents = atlasSprite.contents();

            // Create procedural sprite
            BetaProceduralSprite proceduralSprite = new BetaProceduralSprite(spriteContents.name(),
                    new FrameSize(spriteContents.width(), spriteContents.height()),
                    spriteContents.getOriginalImage(), spriteContents.byMipLevel,
                    betaProceduralTexture, canTick, isFluid);

            // Create custom texture atlas sprite
            TextureAtlasSprite atlasProceduralSprite = new TextureAtlasSpriteWrapper(spriteLocation,
                    proceduralSprite,
                    (int)((float)atlasSprite.getX() / atlasSprite.getU0()),
                    (int)((float)atlasSprite.getY() / atlasSprite.getV0()),
                    atlasSprite.getX(), atlasSprite.getY());

            // Replace value with custom texture atlas
            preparations.regions().put(spriteLocation, atlasProceduralSprite);
        }
    }
}
