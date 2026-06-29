package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.arthurllew.mementobeta.attachments.AttachmentsHelper;
import net.arthurllew.mementobeta.attachments.BetaLevelSeasonAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;

public abstract class SeasonCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands
                .literal(MementoBetaDimension.DIMENSION_NAME)
                        .then(Commands.literal("season")
                                .requires(commandSourceStack
                                        -> commandSourceStack.hasPermission(2))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("season", TimeArgument.time())
                                        .executes(SeasonCommand::setSeason)))
                                .then(Commands.literal("query")
                                        .executes(SeasonCommand::querySeason))));
    }

    /**
     * Sets Beta dimension season.
     *
     * @param context command context
     *
     * @return command result
     */
    private static int setSeason(CommandContext<CommandSourceStack> context) {
        // Try to get season data
        BetaLevelSeasonAttachment betaLevelSeason = AttachmentsHelper.getAttachment(context.getSource().getLevel(),
                MementoBetaAttachments.BETA_SEASON_ATTACHMENT);
        // If it doesn't exist
        if (betaLevelSeason == null)
            return AttachmentsHelper.onNoAttachment(context);

        // Set value
        betaLevelSeason.setSeason(IntegerArgumentType.getInteger(context, "season"));
        // Sync clients
        betaLevelSeason.syncSeason(context.getSource().getLevel());

        // Return success
        return 1;
    }

    /**
     * Prints Beta dimension season.
     *
     * @param context command context
     *
     * @return command result
     */
    private static int querySeason(CommandContext<CommandSourceStack> context) {
        // Try to get season data
        BetaLevelSeasonAttachment betaLevelSeason = AttachmentsHelper.getAttachment(context.getSource().getLevel(),
                MementoBetaAttachments.BETA_SEASON_ATTACHMENT);
        // If it doesn't exist
        if (betaLevelSeason == null)
            return AttachmentsHelper.onNoAttachment(context);

        // Query value
        context.getSource().sendSuccess(
                () -> Component.translatable("commands.mementobeta.season.query",
                        betaLevelSeason.getSeason()),
                true);

        // Return success
        return 1;
    }
}
