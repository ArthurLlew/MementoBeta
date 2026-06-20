package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.arthurllew.mementobeta.attachments.BetaLevelSeasonAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

/**
 * Allows to lock/unlock season ticking in Beta dimension.
 */
public class SeasonLockCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(MementoBetaDimension.DIMENSION_NAME)
                .then(Commands.literal("seasonlock").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
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
        // Get season data
        BetaLevelSeasonAttachment betaLevelSeason = source.getLevel()
                .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);

        // Set value
        betaLevelSeason.setSeasonLock(value);
        // Sync clients
        betaLevelSeason.syncSeasonLock(source.getLevel());

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
        // Get season data
        BetaLevelSeasonAttachment betaLevelSeason = source.getLevel()
                .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);

        // Query value
        source.sendSuccess(() -> Component.translatable("commands.mementobeta.seasonlock.query",
                betaLevelSeason.isSeasonLocked() ? "on" : "off"), true);

        // Return success
        return 1;
    }
}
