package net.arthurllew.mementobeta.client.event;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.attachments.MementoBetaAttachments;
import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.arthurllew.mementobeta.attachments.BetaPlayerAttachment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

@EventBusSubscriber(modid = MementoBeta.MODID, value = Dist.CLIENT)
public class OverlaysRegister {
    /**
     * Registers client overlays.
     */
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "beta_portal_overlay"),
                (gui, partialTicks) -> {
                        Minecraft minecraft = Minecraft.getInstance();
                        Window window = minecraft.getWindow();
                        LocalPlayer player = minecraft.player;

                        // Check player is valid
                        if (player != null) {
                            if (player.hasData(MementoBetaAttachments.BETA_PLAYER_ATTACHMENT)) {
                                // Render overlay
                                renderBetaPortalOverlay(gui, minecraft, window,
                                        player.getData(MementoBetaAttachments.BETA_PLAYER_ATTACHMENT), partialTicks);
                            }
                        }
                });
    }

    /**
     * Renders beta portal overlay.
     */
    private static void renderBetaPortalOverlay(GuiGraphics guiGraphics, Minecraft minecraft, Window window,
                                                BetaPlayerAttachment betaPlayer, DeltaTracker partialTicks) {
        if (minecraft.options.hideGui) return;
        // Check portal timer
        float timeInPortal = Mth.lerp(partialTicks.getGameTimeDeltaPartialTick(false),
                betaPlayer.getPrevPortalAnimTime(), betaPlayer.getPortalAnimTime());
        if (timeInPortal > 0.0F) {
            // Convert timer to opacity
            if (timeInPortal < 1.0F) {
                timeInPortal = timeInPortal * timeInPortal;
                timeInPortal = timeInPortal * timeInPortal;
                timeInPortal = timeInPortal * 0.8F + 0.2F;
            }

            // Acquire and setup rendering
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, timeInPortal);

            // Get and display texture
            TextureAtlasSprite textureAtlasSprite = minecraft.getBlockRenderer().getBlockModelShaper()
                    .getBlockModel(MementoBetaBlocks.BETA_PORTAL.get().defaultBlockState())
                    .getParticleIcon(ModelData.EMPTY);
            guiGraphics.blit(0, 0, -90,
                    guiGraphics.guiWidth(), guiGraphics.guiHeight(), textureAtlasSprite);

            // Release and default rendering
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
