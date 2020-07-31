package carpet.logging.logHelpers;

import carpet.CarpetServer;
import carpet.logging.Logger;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class AutoSaveLogHelper
{
    /*
    public static void onAutoSave(long gametime, int tickCounter)
    {
        Logger logger = LoggerRegistry.getLogger("autosave");
        logger.log( () -> new ITextComponent[]{
                Messenger.c(
                        "g Autosave @ GameTime " + gametime + ", TickCounter " + tickCounter
                )});
    }
     */

    public static ITextComponent [] send_hud_info()
    {
        int tickCounter = CarpetServer.minecraft_server.getTickCounter();
        int toAutoSave = 900 - tickCounter % 900;
        return new ITextComponent[]{
                Messenger.c(
                        "g Autosave in ",
                        String.format("%s %d", toAutoSave <= 60 ? "r" : "g", toAutoSave),
                        "g  gt"
                )
        };
    }
}
