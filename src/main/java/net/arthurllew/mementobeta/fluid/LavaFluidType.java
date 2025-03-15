package net.arthurllew.mementobeta.fluid;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class LavaFluidType extends FluidType {
    /**
     * Still lava texture location.
     */
    private final ResourceLocation stillTexture;
    /**
     * Flowing lava texture location.
     */
    private final ResourceLocation flowingTexture;

    /**
     * Vanilla lava fog color.
     */
    private final Vector3f lavaFogColor = new Vector3f(0.6F, 0.1F, 0.0F);

    /**
     * Constructor.
     * @param stillTexture still lava texture location.
     * @param flowingTexture flowing lava texture location.
     * @param properties fluid properties.
     */
    public LavaFluidType(final ResourceLocation stillTexture, final ResourceLocation flowingTexture,
                         final Properties properties) {
        super(properties);
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
    }

    /**
     * Vanilla lava entity motion speed.
     */
    @Override
    public double motionScale(Entity entity) {
        return entity.level().dimensionType().ultraWarm() ? 0.007 : 0.0023333333333333335;
    }

    /**
     * Vanilla lava item motion speed.
     */
    @Override
    public void setItemMovement(ItemEntity entity) {
        Vec3 vec3 = entity.getDeltaMovement();
        entity.setDeltaMovement(vec3.x * 0.949999988079071,
                vec3.y + (double)(vec3.y < 0.05999999865889549 ? 5.0E-4F : 0.0F),
                vec3.z * 0.949999988079071);
    }

    /**
     * Registers this fluid behaviour on client init.
     */
    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return stillTexture;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return flowingTexture;
            }

            /**
             * Sets fog color according to Vanilla lava (see {@link FogRenderer#setupColor}).
             */
            @Override
            public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level,
                                                    int renderDistance, float darkenWorldAmount,
                                                    Vector3f fluidFogColor) {
                return lavaFogColor;
            }

            /**
             * Sets fog rendering according to Vanilla lava (see {@link FogRenderer#setupFog}).
             */
            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance,
                                        float partialTick, float nearDistance, float farDistance, FogShape shape) {
                Entity entity = camera.getEntity();
                if (entity.isSpectator()) {
                    RenderSystem.setShaderFogStart(-8.0F);
                    RenderSystem.setShaderFogEnd(farDistance * 0.5F);
                } else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.FIRE_RESISTANCE)) {
                    RenderSystem.setShaderFogStart(0.0F);
                    RenderSystem.setShaderFogEnd(3.0F);
                } else {
                    RenderSystem.setShaderFogStart(0.25F);
                    RenderSystem.setShaderFogEnd(1.0F);
                }
            }
        });
    }
}
