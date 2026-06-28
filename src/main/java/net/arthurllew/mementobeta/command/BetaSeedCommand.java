package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.arthurllew.mementobeta.attachments.BetaLevelSeedAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

public class BetaSeedCommand extends BetaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands
                .literal(MementoBetaDimension.DIMENSION_NAME)
                        .then(Commands
                                .literal("seed")
                                .requires((commandSourceStack)
                                        -> commandSourceStack.hasPermission(2))
                                .executes(BetaSeedCommand::queryBetaSeed)));
    }

    /**
     * Prints Beta dimension seed.
     *
     * @param context command context
     *
     * @return command result
     */
    private static int queryBetaSeed(CommandContext<CommandSourceStack> context) {
        // Verify level
        if (missesAttachment(context, MementoBetaAttachments.BETA_SEED_ATTACHMENT))
            return -1;

        // Get seed data
        BetaLevelSeedAttachment betaLevelSeed = context.getSource().getLevel()
                .getData(MementoBetaAttachments.BETA_SEED_ATTACHMENT);

        // Query value
        context.getSource().sendSuccess(
                () -> Component.translatable("commands.mementobeta.betaseed.query",
                        ComponentUtils.copyOnClickText(String.valueOf(betaLevelSeed.getBetaSeed()))),
                true);

        // Return success
        return 1;
    }
}
