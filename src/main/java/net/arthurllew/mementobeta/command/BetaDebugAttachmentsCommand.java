package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.arthurllew.mementobeta.attachments.AttachmentsHelper;
import net.arthurllew.mementobeta.attachments.BetaLevelSeasonAttachment;
import net.arthurllew.mementobeta.attachments.BetaLevelSeedAttachment;
import net.arthurllew.mementobeta.attachments.BetaLevelTimeAttachment;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

public class BetaDebugAttachmentsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands
                .literal(MementoBetaDimension.DIMENSION_NAME)
                .then(Commands
                        .literal("debug")
                        .requires(commandSourceStack
                                -> commandSourceStack.hasPermission(2))
                        .then(Commands.literal("attachments")
                                .executes(BetaDebugAttachmentsCommand::queryBetaSeed))));
    }

    /**
     * Prints Beta dimension attachments debug.
     *
     * @param context command context
     *
     * @return command result
     */
    private static int queryBetaSeed(CommandContext<CommandSourceStack> context) {
        // Try to get attachments
        BetaLevelSeedAttachment betaLevelSeed = AttachmentsHelper.getAttachment(context.getSource().getLevel(),
                MementoBetaAttachments.BETA_SEED_ATTACHMENT);
        BetaLevelTimeAttachment betaLevelTime = AttachmentsHelper.getAttachment(context.getSource().getLevel(),
                MementoBetaAttachments.BETA_TIME_ATTACHMENT);
        BetaLevelSeasonAttachment betaLevelSeason = AttachmentsHelper.getAttachment(context.getSource().getLevel(),
                MementoBetaAttachments.BETA_SEASON_ATTACHMENT);

        // Query values
        context.getSource().sendSuccess(
                () -> Component.translatable("commands.mementobeta.debug.attachments",
                        ComponentUtils.copyOnClickText(String.valueOf(betaLevelSeed != null)),
                        ComponentUtils.copyOnClickText(String.valueOf(betaLevelTime != null)),
                        ComponentUtils.copyOnClickText(String.valueOf(betaLevelSeason != null))),
                true);

        // Return success
        return 1;
    }
}
