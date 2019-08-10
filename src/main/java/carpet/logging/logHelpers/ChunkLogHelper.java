package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;

public class ChunkLogHelper {

    public static void onChunkNewState(int chunkx, int chunkz, String state){
        LoggerRegistry.getLogger("chunkdebug").log( () -> new ITextComponent[]{
                Messenger.c("t "+"ChunkX:" + chunkx + " ChunkZ:" + chunkz + " " + state)});
    }
}