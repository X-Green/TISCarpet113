package carpet.helpers;

import carpet.settings.CarpetSettings;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayerMP;

public class CommandPermissionHelper
{
	public static boolean canCheat(CommandSource source, int permissionLevel)
	{
		return source.hasPermissionLevel(permissionLevel) && !((source.getEntity() instanceof EntityPlayerMP) && CarpetSettings.opPlayerNoCheat);
	}
	public static boolean canCheat(CommandSource source)
	{
		return canCheat(source, 2);
	}
}
