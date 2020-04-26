package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class AutoSaveLogHelper
{
    public static void onAutoSave(long gametime, int tickCounter)
    {
        LoggerRegistry.getLogger("autosave").log( () -> new ITextComponent[]{
                Messenger.c(
                        "g Autosave @ GameTime " + gametime + ", TickCounter " + tickCounter
                )});
    }
}
