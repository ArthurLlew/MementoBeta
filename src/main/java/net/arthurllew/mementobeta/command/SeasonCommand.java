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
 * Allows to set season value in Beta dimension.
 */
public class SeasonCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(MementoBetaDimension.DIMENSION_NAME)
                .then(Commands.literal("season").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("season", TimeArgument.time())
                                        .executes((context) -> setSeason(context.getSource(), IntegerArgumentType.getInteger(context, "season"))))
                        ).then(Commands.literal("query").executes((context) -> querySeason(context.getSource())))
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
    private static int setSeason(CommandSourceStack source, long value) {
        // Get season data
        BetaLevelSeasonAttachment betaLevelSeason = source.getLevel()
                .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);

        // Set value
        betaLevelSeason.setSeason(value);
        // Sync clients
        betaLevelSeason.syncSeason(source.getLevel());

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
    private static int querySeason(CommandSourceStack source) {
        // Get season data
        BetaLevelSeasonAttachment betaLevelSeason = source.getLevel()
                .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);

        // Query value
        source.sendSuccess(() -> Component.translatable("commands.mementobeta.season.query",
                betaLevelSeason.getSeason()), true);

        // Return success
        return 1;
    }
}
