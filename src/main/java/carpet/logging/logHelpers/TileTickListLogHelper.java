package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

public class TileTickListLogHelper {
    private static String type = new String();
    private static String dimension = new String();
    
    public static void setListInfo(String type, int dimensionID)
    {
        TileTickListLogHelper.type = type;
        switch (dimensionID)
        {
        case 0: 
            TileTickListLogHelper.dimension = "Overworld";
            break;
        case 1: 
            TileTickListLogHelper.dimension = "End";
            break;
        case -1: 
            TileTickListLogHelper.dimension = "Nether";
            break;
        }
    }
    
    public static void onTileTicked(long gameTime, int listSize, int dealt, int ticked, int skipped)
    {
        if (listSize == 0)
            return;
        LoggerRegistry.getLogger("tileticklist").log( () -> 
        {
            return new ITextComponent[]{Messenger.c(
                    "g [" + gameTime + "] ", 
                    "w Size=" + listSize + " ",
                    "w Dealt=" + dealt + " ",
                    "w Ticked=" + ticked + " ",
                    "w Skipped=" + skipped + " ",
                    "t " + TileTickListLogHelper.type + " ",
                    "g in ",
                    "e " + TileTickListLogHelper.dimension)};
            
        });
    }
}
