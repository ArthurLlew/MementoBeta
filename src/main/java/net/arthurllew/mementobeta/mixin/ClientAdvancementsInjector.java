package net.arthurllew.mementobeta.mixin;

import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ClientAdvancements.class)
public class ClientAdvancementsInjector {
    /**
     * Injects code into {@link ClientAdvancements#update}. Allows to modify advancement nodes positions on client.
     * Note: negative positions are not supported by scrolling mechanics of {@link AdvancementTab@scroll}.
     */
    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdatePacketReceived(CallbackInfo ci) {
        // Cast to target class
        ClientAdvancements manager = (ClientAdvancements) (Object) this;

        // Modify positions of all custom nodes
        modifyAdvancementNodePosition(manager, "reached_sky", 1.0F, 0.0F);
        modifyAdvancementNodePosition(manager, "achievements", -1.0F, 8.0F);
        modifyAdvancementNodePosition(manager, "reached_bedrock", 1.0F, 16.0F);
    }

    /**
     * Modifies position of advancement node.
     * @param manager nodes manager
     * @param id node resource location
     * @param x X position
     * @param y Y position
     */
    @Unique
    private static void modifyAdvancementNodePosition(ClientAdvancements manager, String id, float x, float y) {
        AdvancementHolder nodeHolder = manager.get(ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, id));
        if (nodeHolder != null) {
            nodeHolder.value().display().ifPresent(displayInfo -> displayInfo.setLocation(x, y));
        }
    }
}
