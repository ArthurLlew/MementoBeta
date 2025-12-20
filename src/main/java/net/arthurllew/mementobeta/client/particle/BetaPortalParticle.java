package net.arthurllew.mementobeta.client.particle;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Modified {@link PortalParticle}.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class BetaPortalParticle extends TextureSheetParticle {
    private final double xStart;
    private final double yStart;
    private final double zStart;

    protected BetaPortalParticle(ClientLevel level, double x, double y, double z,
                                 double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xStart = this.x;
        this.yStart = this.y;
        this.zStart = this.z;
        this.quadSize = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);
        float color = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = color;
        this.gCol = color * 0.3F;
        this.bCol = 0.0F;
        this.lifetime = (int)(Math.random() * 10.0D) + 40;
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    public float getQuadSize(float scaleFactor) {
        float lifeSpan = ((float)this.age + scaleFactor) / (float)this.lifetime;
        lifeSpan = 1.0F - lifeSpan;
        lifeSpan *= lifeSpan;
        lifeSpan = 1.0F - lifeSpan;
        return this.quadSize * lifeSpan;
    }

    public int getLightColor(float partialTick) {
        int light = super.getLightColor(partialTick);
        float lifeSpan = (float)this.age / (float)this.lifetime;
        lifeSpan *= lifeSpan;
        lifeSpan *= lifeSpan;
        int red = light & 255;
        int green = light >> 8 & 255;
        green += (int)(lifeSpan * 15.0F * 16.0F);
        if (green > 240) {
            green = 240;
        }

        return red | green << 16;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float lifeSpan = (float)this.age / (float)this.lifetime;
            float scaledLiferSpan = 1.0F + lifeSpan - lifeSpan * lifeSpan * 2.0F;
            this.x = this.xStart + this.xd * (double)scaledLiferSpan;
            this.y = this.yStart + this.yd * (double)scaledLiferSpan + (double)(1.0F - lifeSpan);
            this.z = this.zStart + this.zd * (double)scaledLiferSpan;
            this.setPos(this.x, this.y, this.z); // update the particle's bounding box
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprite) {
            this.sprite = pSprite;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            BetaPortalParticle portalParticle = new BetaPortalParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            portalParticle.pickSprite(this.sprite);
            return portalParticle;
        }
    }
}
