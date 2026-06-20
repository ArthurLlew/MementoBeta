package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.arthurllew.mementobeta.attachments.BetaLevelTimeAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;

/**
 * Allows to set fixed time value in Beta dimension.
 */
public class FixedTimeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(MementoBetaDimension.DIMENSION_NAME)
                .then(Commands.literal("fixedtime").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("time", TimeArgument.time())
                                        .executes((context) -> setFixedTime(context.getSource(), IntegerArgumentType.getInteger(context, "time"))))
                        ).then(Commands.literal("query").executes((context) -> queryFixedTime(context.getSource())))
                )
        );
    }

    /**
     * Sets value.
     *
     * @param source command source
     * @param value new value
     *
     * @return command status
     */
    private static int setFixedTime(CommandSourceStack source, long value) {
        // Get time data
        BetaLevelTimeAttachment betaLevelTime = source.getLevel()
                .getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT);

        // Set value
        betaLevelTime.setFixedTime(value);
        // Sync clients
        betaLevelTime.syncFixedTime(source.getLevel());

        // Return success
        return 1;
    }

    /**
     * Prints value.
     *
     * @param source command source
     *
     * @return command status
     */
    private static int queryFixedTime(CommandSourceStack source) {
        // Get time data
        BetaLevelTimeAttachment betaLevelTime = source.getLevel()
                .getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT);

        // Query value
        source.sendSuccess(() -> Component.translatable("commands.mementobeta.fixedtime.query",
                betaLevelTime.getFixedTime()), true);

        // Return success
        return 1;
    }
}
