package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.arthurllew.mementobeta.attachments.BetaLevelSeasonAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;

public class SeasonCommand extends BetaCommand {
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
        // Verify level
        if (missesAttachment(context, MementoBetaAttachments.BETA_SEASON_ATTACHMENT))
            return -1;

        // Get season data
        BetaLevelSeasonAttachment betaLevelSeason = context.getSource().getLevel()
                .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);

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
        // Verify level
        if (missesAttachment(context, MementoBetaAttachments.BETA_SEASON_ATTACHMENT))
            return -1;

        // Get season data
        BetaLevelSeasonAttachment betaLevelSeason = context.getSource().getLevel()
                .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);

        // Query value
        context.getSource().sendSuccess(
                () -> Component.translatable("commands.mementobeta.season.query",
                        betaLevelSeason.getSeason()),
                true);

        // Return success
        return 1;
    }
}
