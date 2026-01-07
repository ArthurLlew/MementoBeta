package net.arthurllew.mementobeta.registry;

import com.mojang.brigadier.CommandDispatcher;
import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.command.BetaSeedCommand;
import net.arthurllew.mementobeta.command.FixedTimeCommand;
import net.arthurllew.mementobeta.command.TimeLockCommand;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Registers custom commands.
 */
@EventBusSubscriber(modid = MementoBeta.MODID)
public class MementoBetaCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        TimeLockCommand.register(dispatcher);
        FixedTimeCommand.register(dispatcher);
        BetaSeedCommand.register(dispatcher);
    }
}
