package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.function.Supplier;

public abstract class BetaCommand {
    /**
     * Checks whether command context level has required attachments.
     * @param context command context
     * @param attachment attachment to check
     *
     * @return whether attachment is present
     *
     * @param <T> attachment type
     */
    protected static <T> boolean missesAttachment(CommandContext<CommandSourceStack> context,
                                                  Supplier<AttachmentType<T>> attachment) {
        // Check attachment
        if (context.getSource().getLevel().hasData(attachment)) {
            return false;
        }
        else {
            // Show error message
            context.getSource().sendFailure(Component.translatable("commands.mementobeta.not_beta_dimentsion"));

            return true;
        }
    }
}
