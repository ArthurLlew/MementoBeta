package net.arthurllew.mementobeta;

import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.block.FireBlockBootstrap;
import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.arthurllew.mementobeta.block.entity.MementoBetaBlockEntities;
import net.arthurllew.mementobeta.registry.MementoBetaFluidTypes;
import net.arthurllew.mementobeta.registry.MementoBetaFluids;
import net.arthurllew.mementobeta.registry.MementoBetaItems;
import net.arthurllew.mementobeta.registry.MementoBetaParticles;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

@Mod(MementoBeta.MODID)
public class MementoBeta {
    /**
     * Mod ID.
     */
    public static final String MODID = "mementobeta";
    /**
     * Minecraft logger.
     */
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Mod constructor. Performs basic mod init.
     */
    public MementoBeta(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for mod loading
        modEventBus.addListener(this::commonSetup);

        // Register mod attachments
        MementoBetaAttachments.ATTACHMENTS.register(modEventBus);

        // Register dimension related things
        MementoBetaDimension.POI.register(modEventBus);
        MementoBetaDimension.BETA_BIOME_SOURCES.register(modEventBus);
        MementoBetaDimension.CHUNK_GENERATORS.register(modEventBus);

        // Register mod content
        MementoBetaBlocks.BLOCKS.register(modEventBus);
        MementoBetaBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        MementoBetaItems.ITEMS.register(modEventBus);
        MementoBetaItems.CREATIVE_MODE_TABS.register(modEventBus);
        MementoBetaFluidTypes.FLUID_TYPES.register(modEventBus);
        MementoBetaFluids.FLUIDS.register(modEventBus);
        MementoBetaParticles.PARTICLE_TYPES.register(modEventBus);
    }

    /**
     * Common mod setup event handler.
     * @param event common setup event
     */
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Queue various registration work
        event.enqueueWork(() -> {
            // Bootstrap beta fire block
            FireBlockBootstrap.bootStrap();

            // Beta Lava + Water = Obsidian (Source Lava) / Cobblestone (Flowing Lava)
            FluidInteractionRegistry.addInteraction(MementoBetaFluidTypes.BETA_LAVA_TYPE.get(),
                    new FluidInteractionRegistry.InteractionInformation(Fluids.WATER.getFluidType(),
                            fluidState -> fluidState.isSource() ? Blocks.OBSIDIAN.defaultBlockState()
                                    : Blocks.COBBLESTONE.defaultBlockState()
                    ));
            // Beta Lava + Soul Soil (Below) + Blue Ice = Basalt
            FluidInteractionRegistry.addInteraction(MementoBetaFluidTypes.BETA_LAVA_TYPE.get(),
                    new FluidInteractionRegistry.InteractionInformation((level, currentPos,
                                                                         relativePos, currentState)
                            -> level.getBlockState(currentPos.below()).is(Blocks.SOUL_SOIL)
                            && level.getBlockState(relativePos).is(Blocks.BLUE_ICE),
                            Blocks.BASALT.defaultBlockState()
                    ));
        });
    }
}
