package net.arthurllew.mementobeta;

import com.mojang.logging.LogUtils;
import net.arthurllew.mementobeta.network.DimensionPacketHandler;
import net.arthurllew.mementobeta.world.BetaDimension;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MementoBeta.MODID)
public class MementoBeta
{
    /**
     * Mod ID.
     */
    public static final String MODID = "mementobeta";
    /**
     * Logger.
     */
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Basic mod init.
     */
    public MementoBeta()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for mod loading
        modEventBus.addListener(this::commonSetup);

        // Register dimension related things
        BetaDimension.BETA_BIOME_SOURCES.register(modEventBus);
        BetaDimension.CHUNK_GENERATORS.register(modEventBus);

        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MementoBetaConfig.SPEC);
    }

    /**
     * Common mod setup event handler.
     * @param event common setup event
     */
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("MementoBeta: COMMON SETUP");

        // Register dimension related networking
        DimensionPacketHandler.register();
    }
}
