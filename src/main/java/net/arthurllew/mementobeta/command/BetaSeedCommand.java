package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import net.arthurllew.mementobeta.capabilities.BetaSeedCapability;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerLevel;

/**
 * Displays seed of beta dimension.
 */
public class BetaSeedCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("betaworld")
                .then(Commands.literal("seed").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .executes((context) -> queryBetaSeed(context.getSource())))
        );
    }

    /**
     * Print value.
     * @param source command source.
     * @return command status.
     */
    private static int queryBetaSeed(CommandSourceStack source) {
        // Check presence of correct level data
        ServerLevel level = source.getLevel();
        BetaSeedCapability.get(level).ifPresent(seedCapability ->
                // Notify
                source.sendSuccess(() -> Component.translatable("commands.mementobeta.betaseed.query",
                        ComponentUtils.copyOnClickText(
                                String.valueOf(seedCapability.getBetaSeed().orElse(level.getSeed())))),
                        true));
        return 1;
    }
}
