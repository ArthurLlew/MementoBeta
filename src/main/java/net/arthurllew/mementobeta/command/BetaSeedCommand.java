package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import net.arthurllew.mementobeta.attachments.BetaLevelSeedAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

/**
 * Displays seed of Beta dimension.
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
        BetaLevelSeedAttachment betaLevelSeed = source.getLevel()
                .getData(MementoBetaAttachments.BETA_SEED_ATTACHMENT);

        // Query value
        source.sendSuccess(() -> Component.translatable("commands.mementobeta.betaseed.query",
                ComponentUtils.copyOnClickText(String.valueOf(betaLevelSeed.getBetaSeed()))), true);

        // Return success
        return 1;
    }
}
