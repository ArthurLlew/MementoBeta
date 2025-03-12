package net.arthurllew.mementobeta.event;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.MementoBetaContent;
import net.arthurllew.mementobeta.capabilities.BetaPlayerCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MementoBeta.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class OverlaysRegister {
    /**
     * Registers client overlays.
     */
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("beta_portal_overlay", (gui, pStack, partialTicks, screenWidth, screenHeight) -> {
            Minecraft minecraft = Minecraft.getInstance();
            Window window = minecraft.getWindow();
            LocalPlayer player = minecraft.player;

            // Check player is valid
            if (player != null) {
                BetaPlayerCapability.get(player).ifPresent(handler
                        -> renderAetherPortalOverlay(pStack, minecraft, window, handler, partialTicks));
            }
        });
    }

    /**
     * Renders beta portal overlay.
     */
    private static void renderAetherPortalOverlay(GuiGraphics guiGraphics, Minecraft minecraft, Window window,
                                                  BetaPlayerCapability handler, float partialTicks) {
        // Check portal timer
        float timeInPortal = handler.getPrevPortalAnimTime() + (handler.getPortalAnimTime()
                - handler.getPrevPortalAnimTime()) * partialTicks;
        if (timeInPortal > 0.0F) {
            // Convert timer to opacity
            if (timeInPortal < 1.0F) {
                timeInPortal = timeInPortal * timeInPortal;
                timeInPortal = timeInPortal * timeInPortal;
                timeInPortal = timeInPortal * 0.8F + 0.2F;
            }

            PoseStack poseStack = guiGraphics.pose();

            // Acquire and setup rendering
            poseStack.pushPose();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);

            // Get and display texture
            TextureAtlasSprite textureAtlasSprite = minecraft.getBlockRenderer().getBlockModelShaper()
                    .getParticleIcon(MementoBetaContent.BETA_PORTAL.get().defaultBlockState());
            guiGraphics.blit(0, 0, -90, window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                    textureAtlasSprite, 1.0F, 1.0F, 1.0F, timeInPortal);

            // Release and defaul rendering
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            poseStack.popPose();
        }
    }
}
