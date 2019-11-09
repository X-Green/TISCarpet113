package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.BlockPos;

public class BUDLogHelper {
    private static String stage = new String();
    private static String dimension = new String();
    private static long gameTime;
    
    public static String getDimension(int dimensionID)
    {
        String dimension = new String();
        switch (dimensionID)
        {
        case 0: 
            dimension = "Overworld";
            break;
        case 1: 
            dimension = "End";
            break;
        case -1: 
            dimension = "Nether";
            break;
        }
        return dimension;
    }

    public static void setTickStage(String stage)
    {
        BUDLogHelper.stage = stage;
    }
    
    public static void setGameTime(long gameTime)
    {
        BUDLogHelper.gameTime = gameTime;
    }
    
    public static void onBlockUpdated(BlockPos pos, String type, int dimensionID)
    {
        LoggerRegistry.getLogger("bud").log( (option) -> 
        {
            return new ITextComponent[]{Messenger.c(
                    "g [" + BUDLogHelper.gameTime + "] ", 
                    "w (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ") ",
                    "t " + type + " ",
                    "g at ",
                    "y " + BUDLogHelper.stage + " ",
                    "g in ",
                    "e " + BUDLogHelper.getDimension(dimensionID))};
            
        });
    }
}
