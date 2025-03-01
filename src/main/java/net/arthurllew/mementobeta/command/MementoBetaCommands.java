package net.arthurllew.mementobeta.command;

import com.mojang.brigadier.CommandDispatcher;
import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers custom commands.
 */
@Mod.EventBusSubscriber(modid = MementoBeta.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MementoBetaCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        TimeLockCommand.register(dispatcher);
        FixedTimeCommand.register(dispatcher);
    }
}
