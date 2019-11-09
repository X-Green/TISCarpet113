package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

public class NTELogHelper {
    private static String type = new String();
    private static String dimension = new String();
    
    public static void setListInfo(String type, int dimensionID)
    {
        NTELogHelper.type = type;
        switch (dimensionID)
        {
        case 0: 
            NTELogHelper.dimension = "Overworld";
            break;
        case 1: 
            NTELogHelper.dimension = "End";
            break;
        case -1: 
            NTELogHelper.dimension = "Nether";
            break;
        }
    }
    
    public static void onNTETicked(long gameTime, int listSize, int dealt, int ticked, int skipped)
    {
        if (listSize == 0)
            return;
        LoggerRegistry.getLogger("nte").log( (option, player) -> 
        {
            return new ITextComponent[]{Messenger.c(
                    "g [" + gameTime + "] ", 
                    "w Size=" + listSize + " ",
                    "w Dealt=" + dealt + " ",
                    "w Ticked=" + ticked + " ",
                    "w Skipped=" + skipped + " ",
                    "t " + NTELogHelper.type + " ",
                    "g in ",
                    "e " + NTELogHelper.dimension)};
            
        });
    }
}
