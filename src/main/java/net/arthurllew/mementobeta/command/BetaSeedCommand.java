package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.arthurllew.mementobeta.attachments.AttachmentsHelper;
import net.arthurllew.mementobeta.attachments.BetaLevelSeedAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

public abstract class BetaSeedCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands
                .literal(MementoBetaDimension.DIMENSION_NAME)
                        .then(Commands
                                .literal("seed")
                                .requires(commandSourceStack
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
        // Try to get seed data
        BetaLevelSeedAttachment betaLevelSeed = AttachmentsHelper.getAttachment(context.getSource().getLevel(),
                MementoBetaAttachments.BETA_SEED_ATTACHMENT);
        // If it doesn't exist
        if (betaLevelSeed == null)
            return AttachmentsHelper.onNoAttachment(context);

        // Query value
        context.getSource().sendSuccess(
                () -> Component.translatable("commands.mementobeta.betaseed.query",
                        ComponentUtils.copyOnClickText(String.valueOf(betaLevelSeed.getBetaSeed()))),
                true);

        // Return success
        return 1;
    }
}
