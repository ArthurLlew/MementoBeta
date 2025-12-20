package net.arthurllew.mementobeta.particle;

import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public abstract class MementoBetaParticles {
    /**
     * Deferred Register for particles.
     */
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, MementoBeta.MODID);

    /**
     * Beta portal particles.
     */
    public static final Supplier<SimpleParticleType> BETA_PORTAL_PARTICLES =
            PARTICLE_TYPES.register("beta_portal", () -> new SimpleParticleType(false));
}
