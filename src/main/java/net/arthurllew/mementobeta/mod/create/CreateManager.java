package net.arthurllew.mementobeta.mod.create;

import com.simibubi.create.api.contraption.train.PortalTrackProvider;
import com.simibubi.create.content.trains.track.AllPortalTracks;
import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

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
     * Registers Beta dimension portal for Create mod tracks so trains can use Beta portal.
     */
    public static void registerPortalForCreateTracks() {
        if (INSTALLED) {
            // Registry portal in Create mod
            PortalTrackProvider p = (level, face) -> PortalTrackProvider.fromPortal(level, face,
                    Level.OVERWORLD, MementoBetaDimension.BETA_DIMENSION_LEVEL, MementoBetaBlocks.BETA_PORTAL.get());
            AllPortalTracks.tryRegisterIntegration(MementoBetaBlocks.BETA_PORTAL.getId(), p);
        }
    }
}
