package net.arthurllew.mementobeta.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.arthurllew.mementobeta.world.levelgen.util.BetaSeedHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$WorldTab")
public abstract class CreateWorldScreenWorldTabInjector {
    /**
     * Points to outer class instance.
     */
    @Shadow
    @Final
    CreateWorldScreen this$0;

    /**
     * Label above editbox.
     */
    @Unique
    private static final Component BETA_SEED_LABEL = Component
            .translatable("gui.mementobeta.label.betaseed");
    /**
     * Editbox hint.
     */
    @Unique
    private static final Component BETA_SEED_EMPTY_HINT = Component
            .translatable("gui.mementobeta.editbox.betaseed").withStyle(ChatFormatting.DARK_GRAY);

    /**
     * Injects code into {@link CreateWorldScreen} world tab constructor. Adds new editbox that allows for separate
     * beta dimension seed. Grid layouts are captured from method body to ensure correct padding.
     */
    @Inject(at = @At(value = "TAIL"), method = "<init>(Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;)V")
    public void injectConstructor(CallbackInfo info, @Local(ordinal = 0) GridLayout.RowHelper gridlayout$rowhelper) {
        // Create editbox
        EditBox betaSeedEdit = new EditBox(((ScreenAccessor) this$0).getFont(), 308, 20, Component.translatable("selectWorld.enterSeed")) {
            protected @NotNull MutableComponent createNarrationMessage() {
                return super.createNarrationMessage()
                        .append(CommonComponents.NARRATION_SEPARATOR)
                        .append(BETA_SEED_EMPTY_HINT);
            }
        };
        // Set hint
        betaSeedEdit.setHint(BETA_SEED_EMPTY_HINT);
        // Set initial value
        betaSeedEdit.setValue(this$0.getUiState().getSeed());
        // Set method to call when editbox is changed
        betaSeedEdit.setResponder(BetaSeedHolder::setSeedString);
        // Add editbox to layout
        gridlayout$rowhelper.addChild(CommonLayouts.labeledElement(((ScreenAccessor)this$0).getFont(),
                betaSeedEdit, BETA_SEED_LABEL), 2);
    }
}
