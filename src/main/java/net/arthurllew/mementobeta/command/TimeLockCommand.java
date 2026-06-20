package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.arthurllew.mementobeta.attachments.BetaLevelTimeAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

/**
 * Allows to lock/unlock time ticking in Beta dimension.
 */
public class TimeLockCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(MementoBetaDimension.DIMENSION_NAME)
                .then(Commands.literal("timelock").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("option", BoolArgumentType.bool())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(BoolArgumentType.bool().getExamples(), builder))
                                        .executes((context) -> setTimeLocked(context.getSource(), BoolArgumentType.getBool(context, "option"))))
                        ).then(Commands.literal("query").executes((context) -> queryIsTimeLocked(context.getSource())))
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
    private static int setTimeLocked(CommandSourceStack source, boolean value) {
        // Get time data
        BetaLevelTimeAttachment betaLevelTime = source.getLevel()
                .getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT);

        // Set value
        betaLevelTime.setTimeLock(value);
        // Sync clients
        betaLevelTime.syncTimeLock(source.getLevel());

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
    private static int queryIsTimeLocked(CommandSourceStack source) {
        // Get time data
        BetaLevelTimeAttachment betaLevelTime = source.getLevel()
                .getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT);

        // Query value
        source.sendSuccess(() -> Component.translatable("commands.mementobeta.timelock.query",
                betaLevelTime.isTimeLocked() ? "on" : "off"), true);

        // Return success
        return 1;
    }
}
