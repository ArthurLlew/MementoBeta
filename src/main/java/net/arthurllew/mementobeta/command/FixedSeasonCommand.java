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

public class FixedSeasonCommand extends BetaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands
                .literal(MementoBetaDimension.DIMENSION_NAME)
                        .then(Commands.literal("fixedseason")
                                .requires((commandSourceStack)
                                        -> commandSourceStack.hasPermission(2))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("fixedseason", TimeArgument.time())
                                        .executes(FixedSeasonCommand::setFixedTime)))
                                .then(Commands.literal("query")
                                        .executes(FixedSeasonCommand::queryFixedTime))));
    }

    /**
     * Sets Beta dimension fixed season.
     *
     * @param context command context
     *
     * @return command result
     */
    private static int setFixedTime(CommandContext<CommandSourceStack> context) {
        // Verify level
        if (missesAttachment(context, MementoBetaAttachments.BETA_SEASON_ATTACHMENT))
            return -1;

        // Get season data
        BetaLevelSeasonAttachment betaLevelSeason = context.getSource().getLevel()
                .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);

        // Set value
        betaLevelSeason.setFixedSeason(IntegerArgumentType.getInteger(context, "fixedseason"));
        // Sync clients
        betaLevelSeason.syncFixedSeason(context.getSource().getLevel());

        // Return success
        return 1;
    }

    /**
     * Prints Beta dimension fixed season.
     *
     * @param context command context
     *
     * @return command result
     */
    private static int queryFixedTime(CommandContext<CommandSourceStack> context) {
        // Verify level
        if (missesAttachment(context, MementoBetaAttachments.BETA_SEASON_ATTACHMENT))
            return -1;

        // Get season data
        BetaLevelSeasonAttachment betaLevelSeason = context.getSource().getLevel()
                .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);

        // Query value
        context.getSource().sendSuccess(
                () -> Component.translatable("commands.mementobeta.fixedseason.query",
                        betaLevelSeason.getFixedSeason()),
                true);

        // Return success
        return 1;
    }
}
