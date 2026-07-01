package net.arthurllew.mementobeta.attachments;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class AttachmentsHelper {
    /**
     * Tries to get attachment from level.
     * @param level level
     * @param type attachment
     * @return attachment or {@code null} if attachment does not exist
     * @param <T> attachment class
     */
    public static <T> @Nullable T getAttachment(Level level, Supplier<AttachmentType<T>> type) {
        return level.hasData(type) ? level.getData(type) : null;
    }

    /**
     * Common actions when command context level has no required attachments.
     * @param context command context
     * @return error code
     */
    public static int onNoAttachment(CommandContext<CommandSourceStack> context) {
        // Show error message
        context.getSource().sendFailure(Component.translatable("commands.mementobeta.not_beta_dimentsion"));
        // Provide error code
        return -1;
    }

    /**
     * @param value value
     * @param min left boundary
     * @param max right boundary
     * @return cycled value (value or min if out of bounds)
     */
    public static long cycleValue(long value, long min, long max) {
        return value < min || value > max ? min : value;
    }
}
