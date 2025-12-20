package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.arthurllew.mementobeta.attachments.data.BetaTimeData;
import net.arthurllew.mementobeta.world.BetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * Allows to lock/unlock time ticking in beta dimension.
 */
public class TimeLockCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(BetaDimension.DIMENSION_NAME)
                .then(Commands.literal("timelock").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("option", BoolArgumentType.bool())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(BoolArgumentType.bool().getExamples(), builder))
                                        .executes((context) -> setTimeLocked(context.getSource(), BoolArgumentType.getBool(context, "option"))))
                        ).then(Commands.literal("query").executes((context) -> queryIsTimeLocked(context.getSource())))
                )
        );
    }

    /**
     * Set value.
     * @param source command source.
     * @param value new value.
     * @return command status.
     */
    private static int setTimeLocked(CommandSourceStack source, boolean value) {
        // Get time data
        ServerLevel level = source.getLevel();
        BetaTimeData time = level.getDataStorage().get(BetaTimeData.FACTORY, "betaworld_time");
        if (time != null) {
            // Set value
            time.setTimeLock(value);
            // Sync clients
            time.syncTimeLock(level);

            return 1;
        }
        else {
            return -1;
        }
    }

    /**
     * Print value.
     * @param source command source.
     * @return command status.
     */
    private static int queryIsTimeLocked(CommandSourceStack source) {
        // Get time data
        ServerLevel level = source.getLevel();
        BetaTimeData time = level.getDataStorage().get(BetaTimeData.FACTORY, "betaworld_time");
        if (time != null) {
            // Notify
            source.sendSuccess(() -> Component.translatable("commands.mementobeta.timelock.query",
                    time.isTimeLocked() ? "on" : "off"), true);

            return 1;
        }
        else {
            return -1;
        }
    }
}
