package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.arthurllew.mementobeta.attachments.AttachmentsHelper;
import net.arthurllew.mementobeta.attachments.BetaLevelSeasonAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public abstract class SeasonLockCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands
                .literal(MementoBetaDimension.DIMENSION_NAME)
                .then(Commands.literal("seasonlock")
                        .requires(commandSourceStack
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
        // Try to get season data
        BetaLevelSeasonAttachment betaLevelSeason = AttachmentsHelper.getAttachment(context.getSource().getLevel(),
                MementoBetaAttachments.BETA_SEASON_ATTACHMENT);
        // If it doesn't exist
        if (betaLevelSeason == null)
            return AttachmentsHelper.onNoAttachment(context);

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
        // Try to get season data
        BetaLevelSeasonAttachment betaLevelSeason = AttachmentsHelper.getAttachment(context.getSource().getLevel(),
                MementoBetaAttachments.BETA_SEASON_ATTACHMENT);
        // If it doesn't exist
        if (betaLevelSeason == null)
            return AttachmentsHelper.onNoAttachment(context);

        // Query value
        context.getSource().sendSuccess(
                () -> Component.translatable("commands.mementobeta.seasonlock.query",
                        betaLevelSeason.isSeasonLocked() ? "on" : "off"),
                true);

        // Return success
        return 1;
    }
}
