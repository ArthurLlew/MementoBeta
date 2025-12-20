package net.arthurllew.mementobeta.client.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.particle.BetaPortalParticle;
import net.arthurllew.mementobeta.particle.MementoBetaParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = MementoBeta.MODID, value = Dist.CLIENT)
public class ParticleRegister {
    /**
     * Registers custom particles to particle engine.
     */
    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MementoBetaParticles.BETA_PORTAL_PARTICLES.get(), BetaPortalParticle.Provider::new);
    }
}
