package net.arthurllew.mementobeta;

import com.mojang.logging.LogUtils;
import net.arthurllew.mementobeta.block.FireBlockBootstrap;
import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.arthurllew.mementobeta.block.entity.MementoBetaBlockEntities;
import net.arthurllew.mementobeta.fluid.MementoBetaFluidTypes;
import net.arthurllew.mementobeta.fluid.MementoBetaFluids;
import net.arthurllew.mementobeta.item.MementoBetaItems;
import net.arthurllew.mementobeta.network.MementoBetaPacketHandler;
import net.arthurllew.mementobeta.particle.MementoBetaParticles;
import net.arthurllew.mementobeta.world.BetaDimension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fml.common.Mod;
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
        BetaDimension.POI.register(modEventBus);
        BetaDimension.BETA_BIOME_SOURCES.register(modEventBus);
        BetaDimension.CHUNK_GENERATORS.register(modEventBus);

        // Register mod content
        MementoBetaBlocks.BLOCKS.register(modEventBus);
        MementoBetaBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        MementoBetaItems.ITEMS.register(modEventBus);
        MementoBetaFluidTypes.FLUID_TYPES.register(modEventBus);
        MementoBetaFluids.FLUIDS.register(modEventBus);
        MementoBetaParticles.PARTICLE_TYPES.register(modEventBus);
        MementoBetaItems.CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Common mod setup event handler.
     * @param event common setup event
     */
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("MementoBeta: COMMON SETUP");

        // Register dimension related networking
        MementoBetaPacketHandler.register();

        // Molten mantle brewing recipe(s)
        BrewingRecipeRegistry.addRecipe(Ingredient.of(Items.DRAGON_BREATH),
                Ingredient.of(Items.BLAZE_POWDER), new ItemStack(MementoBetaItems.HEATED_DRAGON_BREATH.get()));
        BrewingRecipeRegistry.addRecipe(Ingredient.of(MementoBetaItems.HEATED_DRAGON_BREATH.get()),
                Ingredient.of(Items.MAGMA_CREAM), new ItemStack(MementoBetaItems.MOLTEN_MANTLE.get()));

        // Bootstrap beta fire block
        FireBlockBootstrap.bootStrap();

        // Beta Lava + Water = Obsidian (Source Lava) / Cobblestone (Flowing Lava)
        FluidInteractionRegistry.addInteraction(MementoBetaFluidTypes.BETA_LAVA_TYPE.get(),
                new FluidInteractionRegistry.InteractionInformation(ForgeMod.WATER_TYPE.get(),
                fluidState -> fluidState.isSource() ? Blocks.OBSIDIAN.defaultBlockState()
                        : Blocks.COBBLESTONE.defaultBlockState()
        ));
        // Beta Lava + Soul Soil (Below) + Blue Ice = Basalt
        FluidInteractionRegistry.addInteraction(MementoBetaFluidTypes.BETA_LAVA_TYPE.get(),
                new FluidInteractionRegistry.InteractionInformation((level, currentPos, relativePos, currentState)
                        -> level.getBlockState(currentPos.below()).is(Blocks.SOUL_SOIL)
                        && level.getBlockState(relativePos).is(Blocks.BLUE_ICE),
                Blocks.BASALT.defaultBlockState()
        ));
    }
}
