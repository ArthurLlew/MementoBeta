package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.arthurllew.mementobeta.attachments.data.BetaSeasonData;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

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
        ServerLevel level = source.getLevel();
        BetaSeasonData season = level.getDataStorage().get(BetaSeasonData.FACTORY, BetaSeasonData.ID);
        if (season != null) {
            // Set value
            season.setSeason(value);
            // Sync clients
            season.syncSeason(level);

            return 1;
        }
        else {
            return -1;
        }
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
        ServerLevel level = source.getLevel();
        BetaSeasonData season = level.getDataStorage().get(BetaSeasonData.FACTORY, BetaSeasonData.ID);
        if (season != null) {
            // Notify
            source.sendSuccess(() -> Component.translatable("commands.mementobeta.season.query",
                    season.getSeason()), true);

            return 1;
        }
        else {
            return -1;
        }
    }
}
