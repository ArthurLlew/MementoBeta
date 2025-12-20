package net.arthurllew.mementobeta.attachments;

import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.arthurllew.mementobeta.client.sound.PortalTriggerSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Player custom data.
 */
@ParametersAreNonnullByDefault
public class BetaPlayerAttachment {
    /**
     * Previous portal overlay intensity.
     */
    private float oldPortalIntensity;
    /**
     * Current portal overlay intensity.
     */
    private float portalIntensity;

    /**
     * Player custom data.
     */
    public BetaPlayerAttachment() {}

    /**
     * @return previous portal overlay intensity.
     */
    public float getOldPortalIntensity() {
        return this.oldPortalIntensity;
    }
    /**
     * @return Current portal overlay intensity.
     */
    public float getPortalIntensity() {
        return this.portalIntensity;
    }

    /**
     * Ticks player data.
     */
    public void onTick(Player player) {
        this.handleBetaPortal(player);
    }
    
    /**
     * Increments or decrements the Beta portal timer depending on if the player is inside a portal.
     * On the client, this also helps to set the portal overlay.
     */
    private void handleBetaPortal(Player player) {
        if (player instanceof LocalPlayer localPlayer) {
            if (!(Minecraft.getInstance().screen instanceof ReceivingLevelScreen)) {
                oldPortalIntensity = portalIntensity;
                float f = 0.0F;
                if (localPlayer.portalProcess != null && localPlayer.portalProcess.isInsidePortalThisTick()
                        && localPlayer.portalProcess.isSamePortal(MementoBetaBlocks.BETA_PORTAL.get())) {
                    if (Minecraft.getInstance().screen != null
                            && !Minecraft.getInstance().screen.isPauseScreen()
                            && !(Minecraft.getInstance().screen instanceof DeathScreen)
                            && !(Minecraft.getInstance().screen instanceof WinScreen)) {
                        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen) {
                            localPlayer.closeContainer();
                        }

                        Minecraft.getInstance().setScreen(null);
                    }

                    if (portalIntensity == 0.0F) {
                        playPortalTriggerSound();
                    }

                    f = 0.0125F;
                    localPlayer.portalProcess.setAsInsidePortalThisTick(false);
                } else if (portalIntensity > 0.0F) {
                    f = -0.05F;
                }

                portalIntensity = Mth.clamp(portalIntensity + f, 0.0F, 1.0F);
            }
        }
    }

    /**
     * Plays the portal ambient sound.
     */
    @OnlyIn(Dist.CLIENT)
    private void playPortalTriggerSound() {
        Minecraft.getInstance().getSoundManager().play(PortalTriggerSoundInstance.forLocalAmbience(
                Minecraft.getInstance().player,
                SoundEvents.PORTAL_TRIGGER,
                Minecraft.getInstance().level.getRandom().nextFloat() * 0.4F + 0.8F, 0.25F));
    }
}
