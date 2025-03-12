package net.arthurllew.mementobeta.capabilities;

import net.arthurllew.mementobeta.portal.PortalTriggerSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;

public class BetaPlayerCapability implements INBTSerializable<CompoundTag> {
    /**
     * Tries to find this capability in player.
     * @return {@link Optional} from this class.
     */
    public static LazyOptional<BetaPlayerCapability> get(Player player) {
        return player.getCapability(MementoBetaCapabilities.BETA_PLAYER_CAPABILITY);
    }

    /**
     * Player to which this data is attached.
     */
    private final Player player;

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
     * Constructor.
     */
    public BetaPlayerCapability(Player player) {
        this.player = player;
    }

    /**
     * @return player to which this data is attached.
     */
    public Player getPlayer() {
        return this.player;
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
    public void onTick() {
        this.handleBetaPortal();
    }
    
    /**
     * Increments or decrements the Beta portal timer depending on if the player is inside a portal.
     * On the client, this also helps to set the portal overlay.
     */
    private void handleBetaPortal() {
        if (this.getPlayer().level().isClientSide()) {
            this.prevPortalAnimTime = this.portalAnimTime;
            Minecraft minecraft = Minecraft.getInstance();
            if (this.isInBetaPortal) {
                if (minecraft.screen != null && !minecraft.screen.isPauseScreen()) {
                    if (minecraft.screen instanceof AbstractContainerScreen) {
                        this.getPlayer().closeContainer();
                    }
                    minecraft.setScreen(null);
                }

                if (this.portalAnimTime == 0.0F) {
                    this.playPortalSound(minecraft);
                }
            }
        }

        if (this.isInPortal()) {
            ++this.betaPortalTime;
            if (this.getPlayer().level().isClientSide()) {
                this.portalAnimTime += 0.0125F;
                if (this.portalAnimTime > 1.0F) {
                    this.portalAnimTime = 1.0F;
                }
            }
            this.isInBetaPortal = false;
        }
        else {
            if (this.getPlayer().level().isClientSide()) {
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
     * Plays the portal entry sound on the client.
     */
    @OnlyIn(Dist.CLIENT)
    private void playPortalSound(Minecraft minecraft) {
        minecraft.getSoundManager().play(PortalTriggerSoundInstance.forLocalAmbience(player,
                SoundEvents.PORTAL_TRIGGER,
                player.getRandom().nextFloat() * 0.4F + 0.8F, 0.25F));
    }


    /**
     * Saves player data in the world save file.
     * @return NBT compound.
     */
    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    /**
     * Restores player data from the world save file.
     * @param compound NBT compound.
     */
    @Override
    public void deserializeNBT(CompoundTag compound) {
    }
}
