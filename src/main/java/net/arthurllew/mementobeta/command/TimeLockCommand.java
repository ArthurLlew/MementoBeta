package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.arthurllew.mementobeta.attachments.AttachmentsHelper;
import net.arthurllew.mementobeta.attachments.BetaLevelTimeAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public abstract class TimeLockCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands
                .literal(MementoBetaDimension.DIMENSION_NAME)
                .then(Commands.literal("timelock")
                        .requires(commandSourceStack
                                -> commandSourceStack.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("lock", BoolArgumentType.bool())
                                        .suggests((context, builder)
                                                -> SharedSuggestionProvider.suggest(
                                                        BoolArgumentType.bool().getExamples(), builder))
                                        .executes(TimeLockCommand::setTimeLocked)))
                        .then(Commands.literal("query")
                                .executes(TimeLockCommand::queryIsTimeLocked))));
    }

    /**
     * Sets Beta dimension time lock.
     *
     * @param context command context
     *
     * @return command result
     */
    private static int setTimeLocked(CommandContext<CommandSourceStack> context) {
        // Try to get time data
        BetaLevelTimeAttachment betaLevelTime = AttachmentsHelper.getAttachment(context.getSource().getLevel(),
                MementoBetaAttachments.BETA_TIME_ATTACHMENT);
        // If it doesn't exist
        if (betaLevelTime == null)
            return AttachmentsHelper.onNoAttachment(context);

        // Set value
        betaLevelTime.setTimeLock(BoolArgumentType.getBool(context, "lock"));
        // Sync clients
        betaLevelTime.syncTimeLock(context.getSource().getLevel());

        // Return success
        return 1;
    }

    /**
     * Prints Beta dimension time lock.
     *
     * @param context command context
     *
     * @return command result
     */
    private static int queryIsTimeLocked(CommandContext<CommandSourceStack> context) {
        // Try to get time data
        BetaLevelTimeAttachment betaLevelTime = AttachmentsHelper.getAttachment(context.getSource().getLevel(),
                MementoBetaAttachments.BETA_TIME_ATTACHMENT);
        // If it doesn't exist
        if (betaLevelTime == null)
            return AttachmentsHelper.onNoAttachment(context);

        // Query value
        context.getSource().sendSuccess(
                () -> Component.translatable("commands.mementobeta.timelock.query",
                        betaLevelTime.isTimeLocked() ? "on" : "off"),
                true);

        // Return success
        return 1;
    }
}
