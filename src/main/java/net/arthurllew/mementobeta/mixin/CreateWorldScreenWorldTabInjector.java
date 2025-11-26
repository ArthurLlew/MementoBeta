package net.arthurllew.mementobeta.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.arthurllew.mementobeta.world.levelgen.util.BetaSeedHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin modifies {@link CreateWorldScreen}.
 */
@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$WorldTab")
public abstract class CreateWorldScreenWorldTabInjector extends GridLayoutTab {
    /**
     * Points to outer class instance.
     */
    @Shadow
    @Final
    private CreateWorldScreen this$0;

    /**
     * Dummy constructor.
     */
    CreateWorldScreenWorldTabInjector(Component title) {
        super(title);
    }

    /**
     * Editbox instance
     */
    private EditBox betaSeedEdit;

    /**
     * Label above editbox.
     */
    private static final Component BETA_SEED_LABEL = Component.translatable("gui.mementobeta.label.betaseed");
    /**
     * Editbox hint.
     */
    private static final Component BETA_SEED_EMPTY_HINT = Component.translatable("gui.mementobeta.editbox.betaseed").withStyle(ChatFormatting.DARK_GRAY);

    /**
     * Injects code into {@link CreateWorldScreen} world tab constructor. Adds new editbox that allows for separate
     * beta dimension seed. Grid layouts are captured from method body to ensure correct padding.
     */
    @Inject(at = @At(value = "TAIL"), method = "<init>(Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;)V")
    public void injectConstructor(CallbackInfo info, @Local(ordinal = 0) GridLayout.RowHelper gridlayout$rowhelper,
                                  @Local(ordinal = 1) GridLayout.RowHelper gridlayout$rowhelper1) {
        // Add label
        gridlayout$rowhelper1.addChild((new StringWidget(BETA_SEED_LABEL, ((ScreenAccessor)this$0).getFont())).alignLeft());
        // Add editbox
        this.betaSeedEdit = gridlayout$rowhelper1.addChild(new EditBox(((ScreenAccessor)this$0).getFont(), 0, 0, 308, 20, Component.translatable("selectWorld.enterSeed")) {
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(CommonComponents.NARRATION_SEPARATOR).append(BETA_SEED_EMPTY_HINT);
            }
        }, gridlayout$rowhelper.newCellSettings().padding(1));
        // Set hint
        this.betaSeedEdit.setHint(BETA_SEED_EMPTY_HINT);
        // Set initial value
        this.betaSeedEdit.setValue(this$0.getUiState().getSeed());
        // Set method to call when editbox is changed
        this.betaSeedEdit.setResponder(BetaSeedHolder::setSeed);
    }

    /**
     * Injects code into {@link CreateWorldScreen} world tab tick method, so beta seed editbox will also tick.
     */
    @Inject(at = @At(value = "TAIL"), method = "tick")
    public void injectTick(CallbackInfo info) {
        this.betaSeedEdit.tick();
    }
}
