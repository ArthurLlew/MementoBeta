package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.arthurllew.mementobeta.capabilities.BetaTimeCapability;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * Allows to set fixed time value in beta dimension.
 */
public class FixedTimeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("betaworld")
                .then(Commands.literal("fixedtime").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("time", TimeArgument.time())
                                        .executes((context) -> setFixedTime(context.getSource(), IntegerArgumentType.getInteger(context, "time"))))
                        ).then(Commands.literal("query").executes((context) -> queryFixedTime(context.getSource())))
                )
        );
    }

    /**
     * Set value.
     * @param source command source.
     * @param value new value.
     * @return command status.
     */
    private static int setFixedTime(CommandSourceStack source, long value) {
        // Check presence of correct level data
        ServerLevel level = source.getLevel();
        BetaTimeCapability.get(level).ifPresent(time -> {
            // Set value
            time.setFixedTime(value);
            // Sync clients
            time.syncFixedTime();
        });
        return 1;
    }

    /**
     * Print value.
     * @param source command source.
     * @return command status.
     */
    private static int queryFixedTime(CommandSourceStack source) {
        // Check presence of correct level data
        ServerLevel level = source.getLevel();
        BetaTimeCapability.get(level).ifPresent(time ->
                // Notify
                source.sendSuccess(() -> Component.translatable("commands.mementobeta.fixedtime.query",
                        time.getFixedTime()), true));
        return 1;
    }
}
