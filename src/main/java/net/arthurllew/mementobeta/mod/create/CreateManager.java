package net.arthurllew.mementobeta.mod.create;

import com.simibubi.create.api.contraption.train.PortalTrackProvider;
import com.simibubi.create.content.trains.track.AllPortalTracks;
import net.arthurllew.mementobeta.block.portal.BetaPortalBlock;
import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredBlock;

public class CreateManager {
    /**
     * Little tiles mod id.
     */
    private static final String MODID = "create";
    /**
     * Whether little tiles mod is installed.
     */
    private static final boolean INSTALLED = ModList.get().isLoaded(MODID);

    /**
     * Registers all Beta dimension portals for Create mod tracks.
     */
    public static void registerPortalsForCreateTracks() {
        if (INSTALLED) {
            registerPortalForCreateTracks(MementoBetaBlocks.BETA_PORTAL);
            registerPortalForCreateTracks(MementoBetaBlocks.BETA_PORTAL_NETHER);
        }
    }

    /**
     * Registers provided Beta dimension portal block for Create mod tracks so trains can use it.
     */
    public static void registerPortalForCreateTracks(DeferredBlock<BetaPortalBlock> betaPortal) {
        // Create and register portal provider
        AllPortalTracks.tryRegisterIntegration(betaPortal.getId(),
                (level, face) -> PortalTrackProvider.fromPortal(level, face,
                        betaPortal.get().getHomeDimension(),
                        betaPortal.get().getDestinationDimension(),
                        betaPortal.get()));
    }
}
