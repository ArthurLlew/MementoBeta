package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.arthurllew.mementobeta.attachments.data.BetaSeasonData;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

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
        ServerLevel level = source.getLevel();
        BetaSeasonData season = level.getDataStorage().get(BetaSeasonData.FACTORY, BetaSeasonData.ID);
        if (season != null) {
            // Set value
            season.setSeasonLock(value);
            // Sync clients
            season.syncSeasonLock(level);

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
    private static int queryIsTimeLocked(CommandSourceStack source) {
        // Get season data
        ServerLevel level = source.getLevel();
        BetaSeasonData season = level.getDataStorage().get(BetaSeasonData.FACTORY, BetaSeasonData.ID);
        if (season != null) {
            // Notify
            source.sendSuccess(() -> Component.translatable("commands.mementobeta.seasonlock.query",
                    season.isSeasonLocked() ? "on" : "off"), true);

            return 1;
        }
        else {
            return -1;
        }
    }
}
