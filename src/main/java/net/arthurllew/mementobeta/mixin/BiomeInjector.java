package net.arthurllew.mementobeta.mixin;

import net.arthurllew.mementobeta.world.biome.BetaBiomeSeasonHolder;
import net.arthurllew.mementobeta.world.biome.BiomeInjectorInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = Biome.class)
public abstract class BiomeInjector implements BiomeInjectorInterface {
    /**
     * Whether biome has seasons.
     */
    @Unique
    private boolean hasSeasons = false;

    /**
     * Provides access to biome climate settings.
     */
    @Final
    @Shadow
    private Biome.ClimateSettings climateSettings;

    /**
     * Allows seasons in biome.
     */
    @Override
    public void allowSeasons() {this.hasSeasons = true;}

    /**
     * Injects code into {@link Biome}. Adds season effects to grass color calculation in Beta dimension.
     */
    @Inject(method = "getGrassColorFromTexture", at = @At("HEAD"), cancellable = true)
    private void injectGetGrassColorFromTexture(CallbackInfoReturnable<Integer> cir) {
        // If biome has seasons
        if (this.hasSeasons) {
            // Modify temperature
            float seasonTemperature = BetaBiomeSeasonHolder.seasonModifyTemperature(this.climateSettings.temperature());
            // Get grass color
            cir.setReturnValue(GrassColor.get(Mth.clamp(seasonTemperature, 0.0F, 1.0F),
                    Mth.clamp(this.climateSettings.downfall(), 0.0F, 1.0F)));
        }
    }

    /**
     * Injects code into {@link Biome}. Adds season effects to temperature calculation in Beta dimension.
     */
    @Inject(method = "getTemperature", at = @At("RETURN"), cancellable = true)
    private void injectGetTemperature(BlockPos pos, CallbackInfoReturnable<Float> cir) {
        // If biome has seasons
        if (this.hasSeasons) {
            // Modify temperature
            cir.setReturnValue(BetaBiomeSeasonHolder.seasonModifyTemperature(cir.getReturnValue()));
        }
    }
}
