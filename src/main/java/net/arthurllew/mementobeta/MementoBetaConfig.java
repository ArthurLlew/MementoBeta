package net.arthurllew.mementobeta;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Mod configuration.
 */
@Mod.EventBusSubscriber(modid = MementoBeta.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MementoBetaConfig
{
    /**
     * Config builder.
     */
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    /**
     * Whether the beta dimension generator should use world seed.
     * Config spec.
     */
    private static final ForgeConfigSpec.BooleanValue USE_WORLD_SEED = BUILDER
            .comment("Whether the beta dimension generator should use world seed.")
            .define("useWorldSeed", true);
    /**
     * The seed to use instead of world seed.
     * Config spec.
     */
    private static final ForgeConfigSpec.LongValue CUSTOM_SEED = BUILDER
            .comment("The seed to use instead of world seed.")
            .defineInRange("seed", 4098189740663496215L, Long.MIN_VALUE, Long.MAX_VALUE);
    /**
     * Config spec.
     */
    static final ForgeConfigSpec SPEC = BUILDER.build();

    /**
     * Whether the beta dimension generator should use world seed.
     */
    public static boolean useWorldSeed;
    /**
     * The seed to use instead of world seed.
     */
    public static long customSeed;

    /**
     * Loads config.
     * @param event config load event.
     */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        useWorldSeed = USE_WORLD_SEED.get();
        customSeed = CUSTOM_SEED.get();
    }
}
