package net.arthurllew.mementobeta.portal;

import net.arthurllew.mementobeta.capabilities.BetaPlayerCapability;
import net.arthurllew.mementobeta.capabilities.MementoBetaCapabilities;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

public class PortalTriggerSoundInstance extends AbstractTickableSoundInstance {
    /**
     * Player who will hear the sound.
     */
    private final Player player;
    /**
     * Starting volume.
     */
    private final float startingVolume;
    /**
     * Fade timer.
     */
    private int fade;

    /**
     * Constructor.
     */
    public PortalTriggerSoundInstance(Player player, SoundEvent event, SoundSource source, float volume, float pitch,
                                      RandomSource random, boolean looping, int delay, Attenuation attenuation,
                                      double x, double y, double z, boolean relative) {
        super(event, source, random);
        this.player = player;
        this.volume = volume;
        this.startingVolume = volume;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.looping = looping;
        this.delay = delay;
        this.attenuation = attenuation;
        this.relative = relative;
    }

    /**
     * @return sound instantiated from local ambience.
     */
    public static PortalTriggerSoundInstance forLocalAmbience(Player player, SoundEvent sound, float volume,
                                                              float pitch) {
        return new PortalTriggerSoundInstance(player, sound, SoundSource.AMBIENT, pitch, volume,
                SoundInstance.createUnseededRandom(), false, 0, Attenuation.NONE,
                0.0, 0.0, 0.0, true);
    }

    /**
     * Ticks sound.
     */
    @Override
    public void tick() {
        // If player has correct capability
        LazyOptional<BetaPlayerCapability> aetherPlayer =
                player.getCapability(MementoBetaCapabilities.BETA_PLAYER_CAPABILITY);
        aetherPlayer.ifPresent((player) -> {
            if (!player.isInPortal()) {
                // Increase timer, calculate new volume and determine if sound can be stopped
                this.fade++;
                this.volume = (float) Math.exp(-(this.fade / (75 / 1.5))) - (1 - this.startingVolume);
                if (this.fade >= 75) {
                    this.stop();
                }
            }
        });
    }
}
