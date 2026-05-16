package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import net.arthurllew.mementobeta.attachments.data.BetaSeedData;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

/**
 * Displays seed of beta dimension.
 */
public class BetaSeedCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(MementoBetaDimension.DIMENSION_NAME)
                .then(Commands.literal("seed").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                        .executes((context) -> queryBetaSeed(context.getSource())))
        );
    }

    /**
     * Prints value.
     *
     * @param source command source
     *
     * @return command status
     */
    private static int queryBetaSeed(CommandSourceStack source) {
        // Get seed data
        BetaSeedData betaSeedData = source.getLevel().getDataStorage().get(BetaSeedData.FACTORY, "betaworld_seed");
        if (betaSeedData != null) {
            // Notify
            source.sendSuccess(() -> Component.translatable("commands.mementobeta.betaseed.query",
                    ComponentUtils.copyOnClickText(String.valueOf(betaSeedData.getBetaSeed()))), true);

            return 1;
        }
        else {
            return -1;
        }
    }
}
