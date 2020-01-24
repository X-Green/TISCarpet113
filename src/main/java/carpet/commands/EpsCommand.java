package carpet.commands;

import carpet.settings.CarpetSettings;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import carpet.utils.Messenger;

import static net.minecraft.command.Commands.literal;

public class EpsCommand
{
    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralArgumentBuilder<CommandSource> command = literal("epsTest").
                executes( (c) ->
                {
                    Messenger.m(c.getSource(),"[TISCM]: EPS test is now active");
                    CarpetSettings.isEpsActive = true;
                    return 1;
                });

        dispatcher.register(command);
    }
}