package net.arthurllew.mementobeta.client.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.fluid.BetaLavaFluidType;
import net.arthurllew.mementobeta.fluid.MementoBetaFluidTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = MementoBeta.MODID, value = Dist.CLIENT)
public class ClientExtensionsRegister {
    /**
     * Registers client extensions.
     */
    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        // Register beta lava client behaviour
        event.registerFluidType(((BetaLavaFluidType)MementoBetaFluidTypes.BETA_LAVA_TYPE.get()).getClientExtension(),
                                MementoBetaFluidTypes.BETA_LAVA_TYPE.get());
    }
}
