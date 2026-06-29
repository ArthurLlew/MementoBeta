package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.arthurllew.mementobeta.attachments.BetaLevelTimeAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;

public class FixedTimeCommand extends BetaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands
                .literal(MementoBetaDimension.DIMENSION_NAME)
                        .then(Commands.literal("fixedtime")
                                .requires(commandSourceStack
                                        -> commandSourceStack.hasPermission(2))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("time", TimeArgument.time())
                                                .executes(FixedTimeCommand::setFixedTime))
                                        .then(Commands.literal("query")
                                                .executes(FixedTimeCommand::queryFixedTime)))));
    }

    /**
     * Sets Beta dimension time.
     *
     * @param context command context
     *
     * @return command result
     */
    private static int setFixedTime(CommandContext<CommandSourceStack> context) {
        // Verify level
        if (missesAttachment(context, MementoBetaAttachments.BETA_TIME_ATTACHMENT))
            return -1;

        // Get time data
        BetaLevelTimeAttachment betaLevelTime = context.getSource().getLevel()
                .getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT);

        // Set value
        betaLevelTime.setFixedTime(IntegerArgumentType.getInteger(context, "time"));
        // Sync clients
        betaLevelTime.syncFixedTime(context.getSource().getLevel());

        // Return success
        return 1;
    }

    /**
     * Prints Beta dimension time.
     *
     * @param context command context
     *
     * @return command result
     */
    private static int queryFixedTime(CommandContext<CommandSourceStack> context) {
        // Verify level
        if (missesAttachment(context, MementoBetaAttachments.BETA_TIME_ATTACHMENT))
            return -1;

        // Get time data
        BetaLevelTimeAttachment betaLevelTime = context.getSource().getLevel()
                .getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT);

        // Query value
        context.getSource().sendSuccess(
                () -> Component.translatable("commands.mementobeta.fixedtime.query",
                        betaLevelTime.getFixedTime()),
                true);

        // Return success
        return 1;
    }
}
