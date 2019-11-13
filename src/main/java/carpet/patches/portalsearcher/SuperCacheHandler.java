package carpet.patches.portalsearcher;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.Map;
import java.util.Set;

public class SuperCacheHandler {
    private Map<ChunkPos, Set<BlockPos>> chunkPortalMapping;

    private static SuperCacheHandler HANDLER_OVERWORLD = new SuperCacheHandler();
    private static SuperCacheHandler HANDLER_NETHER = new SuperCacheHandler();

    private SuperCacheHandler() {
        chunkPortalMapping = Maps.newHashMap();
    }

    public static SuperCacheHandler getHandlerOverworld() {
        return HANDLER_OVERWORLD;
    }

    public static SuperCacheHandler getHandlerNether() {
        return HANDLER_NETHER;
    }

    public boolean addPortal(BlockPos portalPos) {
        ChunkPos cPos = new ChunkPos(portalPos);
        if (!chunkPortalMapping.containsKey(cPos)) {
            return false;
        }
        chunkPortalMapping.get(cPos).add(new BlockPos(portalPos));
        return true;
    }

    public boolean markChunk(ChunkPos chunkPos) {
        if (!chunkPortalMapping.containsKey(chunkPos)) {
            chunkPortalMapping.put(chunkPos, Sets.newHashSet());
            return true;
        }
        return false;
    }

    public boolean removePortal(BlockPos portalPos) {
        ChunkPos cPos = new ChunkPos(portalPos);
        if (!chunkPortalMapping.containsKey(cPos)) {
            return false;
        }
        return chunkPortalMapping.get(cPos).remove(new BlockPos(portalPos));
    }

    public boolean isMarked(ChunkPos chunkPos) {
        return chunkPortalMapping.containsKey(chunkPos);
    }

    public Iterable<BlockPos> getChunkPortalIterable(ChunkPos chunkPos) {
        if (!isMarked(chunkPos)) {
            return null;
        }
        return chunkPortalMapping.get(chunkPos);
    }
}
