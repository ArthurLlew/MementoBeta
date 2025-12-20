package net.arthurllew.mementobeta.world.levelgen.placement;

import com.mojang.serialization.MapCodec;
import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public abstract class MementoBetaPlacements {
    /**
     * Clamps feature height between two values. Produces empty position if conditions are not met.
     */

    public static PlacementModifierType<HeightClamp> HEIGHT_CLAMP;

    /**
     * Is used to pull this class into action.
     */
    public static void bootstrap() {
        HEIGHT_CLAMP = register("height_clamp", HeightClamp.CODEC);
    }

    /**
     * @return registered placement modifier.
     */
    private static <P extends PlacementModifier> PlacementModifierType<P> register(String name, MapCodec<P> codec) {
        return Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE,
                ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, name),
                () -> codec);
    }
}
