package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.arthurllew.mementobeta.attachments.BetaLevelSeasonAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class SeasonLockCommand extends BetaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands
                .literal(MementoBetaDimension.DIMENSION_NAME)
                        .then(Commands.literal("seasonlock")
                                .requires((commandSourceStack)
                                        -> commandSourceStack.hasPermission(2))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("lock", BoolArgumentType.bool())
                                        .suggests((context, builder)
                                                -> SharedSuggestionProvider.suggest(
                                                        BoolArgumentType.bool().getExamples(), builder))
                                        .executes(SeasonLockCommand::setTimeLocked)))
                                .then(Commands.literal("query")
                                        .executes(SeasonLockCommand::queryIsTimeLocked))));
    }

    /**
     * Sets Beta dimension season lock.
     *
     * @param context command context
     *
     * @return command result
     */
    private static int setTimeLocked(CommandContext<CommandSourceStack> context) {
        // Verify level
        if (missesAttachment(context, MementoBetaAttachments.BETA_SEASON_ATTACHMENT))
            return -1;

        // Get season data
        BetaLevelSeasonAttachment betaLevelSeason = context.getSource().getLevel()
                .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);

        // Set value
        betaLevelSeason.setSeasonLock(BoolArgumentType.getBool(context, "lock"));
        // Sync clients
        betaLevelSeason.syncSeasonLock(context.getSource().getLevel());

        // Return success
        return 1;
    }

    /**
     * Prints Beta dimension season lock.
     *
     * @param context command context
     *
     * @return command result
     */
    private static int queryIsTimeLocked(CommandContext<CommandSourceStack> context) {
        // Verify level
        if (missesAttachment(context, MementoBetaAttachments.BETA_SEASON_ATTACHMENT))
            return -1;

        // Get season data
        BetaLevelSeasonAttachment betaLevelSeason = context.getSource().getLevel()
                .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);

        // Query value
        context.getSource().sendSuccess(
                () -> Component.translatable("commands.mementobeta.seasonlock.query",
                        betaLevelSeason.isSeasonLocked() ? "on" : "off"),
                true);

        // Return success
        return 1;
    }
}
