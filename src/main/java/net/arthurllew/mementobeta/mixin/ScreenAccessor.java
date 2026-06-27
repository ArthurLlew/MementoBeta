package net.arthurllew.mementobeta.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

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
