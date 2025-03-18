package net.arthurllew.mementobeta.particle;

import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public abstract class MementoBetaParticles {
    /**
     * Deferred Register for particles.
     */
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MementoBeta.MODID);

    /**
     * Beta portal particles.
     */
    public static final RegistryObject<SimpleParticleType> BETA_PORTAL_PARTICLES =
            PARTICLE_TYPES.register("beta_portal", () -> new SimpleParticleType(false));
}
