package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.arthurllew.mementobeta.attachments.BetaLevelSeasonAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;

/**
 * Allows to set fixed season value in Beta dimension.
 */
public class FixedSeasonCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(MementoBetaDimension.DIMENSION_NAME)
                .then(Commands.literal("fixedseason").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("season", TimeArgument.time())
                                        .executes((context) -> setFixedTime(context.getSource(), IntegerArgumentType.getInteger(context, "season"))))
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
        // Get season data
        BetaLevelSeasonAttachment betaLevelSeason = source.getLevel()
                .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);

        // Set value
        betaLevelSeason.setFixedSeason(value);
        // Sync clients
        betaLevelSeason.syncFixedSeason(source.getLevel());

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
        // Get season data
        BetaLevelSeasonAttachment betaLevelSeason = source.getLevel()
                .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);

        // Query value
        source.sendSuccess(() -> Component.translatable("commands.mementobeta.fixedseason.query",
                betaLevelSeason.getFixedSeason()), true);

        // Return success
        return 1;
    }
}
