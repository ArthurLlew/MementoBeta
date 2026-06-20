package net.arthurllew.mementobeta.mixin;

import net.arthurllew.mementobeta.world.biome.BetaBiomeSeasonHolder;
import net.arthurllew.mementobeta.world.biome.BetaBiomeSeasons;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Biome.class)
public class BiomeInjectorClient {
    @Inject(method = "getTemperature", at = @At("RETURN"), cancellable = true)
    private void injectGetTemperature(BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (Minecraft.getInstance().level != null) {
            // Biome registry on client
            Registry<Biome> biomeRegistry = Minecraft.getInstance().level
                    .registryAccess().registryOrThrow(Registries.BIOME);

            // Biome holder
            Optional<Holder.Reference<Biome>> biomeHolder = biomeRegistry
                    .getHolder(biomeRegistry.getId((Biome) (Object) this));

            // If biome has seasons
            if (biomeHolder.isPresent() && biomeHolder.get().is(BetaBiomeSeasons.BIOMES_WITH_SEASONS_TAG)) {
                // Modify temperature
                cir.setReturnValue(BetaBiomeSeasonHolder.seasonModifyTemperature(cir.getReturnValue()));
            }
        }
    }
}
