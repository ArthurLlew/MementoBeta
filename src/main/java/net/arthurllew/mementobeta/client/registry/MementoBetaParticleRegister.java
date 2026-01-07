package net.arthurllew.mementobeta.client.registry;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.client.particle.BetaPortalParticle;
import net.arthurllew.mementobeta.registry.MementoBetaParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@Mod(value = MementoBeta.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MementoBeta.MODID, value = Dist.CLIENT)
public class MementoBetaParticleRegister {
    /**
     * Registers custom particles to particle engine.
     */
    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MementoBetaParticles.BETA_PORTAL_PARTICLES.get(), BetaPortalParticle.Provider::new);
    }
}
