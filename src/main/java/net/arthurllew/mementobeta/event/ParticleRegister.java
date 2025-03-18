package net.arthurllew.mementobeta.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.particle.BetaPortalParticle;
import net.arthurllew.mementobeta.particle.MementoBetaParticles;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MementoBeta.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleRegister {
    /**
     * Registers custom particles to particle engine.
     */
    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MementoBetaParticles.BETA_PORTAL_PARTICLES.get(), BetaPortalParticle.Provider::new);
    }
}
