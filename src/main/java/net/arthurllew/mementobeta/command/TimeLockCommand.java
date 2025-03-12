package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.arthurllew.mementobeta.capabilities.BetaTimeCapability;
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
        dispatcher.register(Commands.literal("betaworld")
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
        // Check presence of correct level data
        ServerLevel level = source.getLevel();
        BetaTimeCapability.get(level).ifPresent(time -> {
            // Set value
            time.setTimeLock(value);
            // Sync clients
            time.syncTimeLock();
        });
        return 1;
    }

    /**
     * Print value.
     * @param source command source.
     * @return command status.
     */
    private static int queryIsTimeLocked(CommandSourceStack source) {
        // Check presence of correct level data
        ServerLevel world = source.getLevel();
        BetaTimeCapability.get(world).ifPresent(time ->
                // Notify
                source.sendSuccess(() -> Component.translatable("commands.mementobeta.timelock.query",
                        time.isTimeLocked() ? "on" : "off"), true));
        return 1;
    }
}
