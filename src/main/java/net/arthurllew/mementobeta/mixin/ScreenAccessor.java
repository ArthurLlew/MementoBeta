package net.arthurllew.mementobeta.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * This mixin grants access to the "font" field in {@link Screen}.
 */
@Mixin(Screen.class)
public interface ScreenAccessor {
    /**
     * Getter.
     *
     * @return font
     */
    @Accessor("font")
    Font getFont();
}
