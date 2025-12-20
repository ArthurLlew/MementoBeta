package net.arthurllew.mementobeta.attachments;

import net.arthurllew.mementobeta.portal.PortalTriggerSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.sounds.SoundEvents;
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
     * Whether the player is in beta portal block.
     */
    private boolean isInBetaPortal = false;

    /**
     * How long did the player spent in beta portal block.
     */
    private int betaPortalTime = 0;

    // Animation related vars
    private float prevPortalAnimTime, portalAnimTime = 0.0F;

    /**
     * Player custom data.
     */
    public BetaPlayerAttachment() {

    }

    /**
     * @param inPortal whether the player is in beta portal block.
     */
    public void setInPortal(boolean inPortal) {
        this.isInBetaPortal = inPortal;
    }

    /**
     * @return whether the player is in beta portal block.
     */
    public boolean isInPortal() {
        return this.isInBetaPortal;
    }

    /**
     * @param timer how long did the player spent in beta portal block.
     */
    public void setPortalTime(int timer) {
        this.betaPortalTime = timer;
    }

    /**
     * @return how long did the player spent in beta portal block.
     */

    public int getPortalTime() {
        return this.betaPortalTime;
    }

    /**
     * @return time for portal vignette animation.
     */
    public float getPortalAnimTime() {
        return this.portalAnimTime;
    }

    /**
     * @return previous time for portal vignette animation.
     */
    public float getPrevPortalAnimTime() {
        return this.prevPortalAnimTime;
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
        if (player.level().isClientSide()) {
            this.prevPortalAnimTime = this.portalAnimTime;
            Minecraft minecraft = Minecraft.getInstance();
            if (this.isInBetaPortal) {
                if (minecraft.screen != null && !minecraft.screen.isPauseScreen()) {
                    if (minecraft.screen instanceof AbstractContainerScreen) {
                        player.closeContainer();
                    }
                    minecraft.setScreen(null);
                }

                if (this.portalAnimTime == 0.0F) {
                    this.playPortalTrigger(minecraft);
                }
            }
        }

        if (this.isInPortal()) {
            ++this.betaPortalTime;
            if (player.level().isClientSide()) {
                this.portalAnimTime += 0.0125F;
                if (this.portalAnimTime > 1.0F) {
                    this.portalAnimTime = 1.0F;
                }
            }
            this.isInBetaPortal = false;
        }
        else {
            if (player.level().isClientSide()) {
                if (this.portalAnimTime > 0.0F) {
                    this.portalAnimTime -= 0.05F;
                }

                if (this.portalAnimTime < 0.0F) {
                    this.portalAnimTime = 0.0F;
                }
            }
            if (this.getPortalTime() > 0) {
                this.betaPortalTime -= 4;
            }
        }
    }

    /**
     * Plays the portal ambient sound.
     */
    @OnlyIn(Dist.CLIENT)
    private void playPortalTrigger(Minecraft minecraft) {
        minecraft.getSoundManager().play(PortalTriggerSoundInstance.forLocalAmbience(minecraft.player,
                SoundEvents.PORTAL_TRIGGER,
                minecraft.level.getRandom().nextFloat() * 0.4F + 0.8F, 0.25F));
    }
}
