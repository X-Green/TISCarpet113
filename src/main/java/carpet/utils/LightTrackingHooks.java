package carpet.utils;

import java.util.Arrays;
import java.util.Map.Entry;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

public class LightTrackingHooks
{
    private static final int NEIGHBOR_FLAG_COUNT = 12;
    private static final int PLAYER_NEIGHBOR_FLAG_COUNT = NEIGHBOR_FLAG_COUNT + 1;
    private static final int FLAG_COUNT = NEIGHBOR_FLAG_COUNT + 2;
    public static final int SYNC_FLAG_COUNT = LightBoundaryCheckHooks.FLAG_COUNT_CLIENT + NEIGHBOR_FLAG_COUNT;

    private static final int[] VERTICAL_MASKS = new int[2];

    static
    {
        VERTICAL_MASKS[0] = (1 << 16) - 2;
        VERTICAL_MASKS[1] = (1 << 15) - 1;
    }

    static int getHorizontalFlagIndex(final EnumFacing dir)
    {
        return dir.getHorizontalIndex() * 3 + 1;
    }

    public static void trackLightChange(final Chunk chunk, final BlockPos pos, final EnumLightType lightType)
    {
        trackLightChangeHorizontal(chunk, pos, lightType);
        trackLightChangeVertical(chunk, pos, lightType);
    }

    public static void trackLightChangeHorizontal(final Chunk chunk, final BlockPos pos, final EnumLightType lightType)
    {
        trackLightChangesHorizontal(chunk, pos.getX(), pos.getZ(), 1 << (pos.getY() >> 4), lightType);
    }

    public static void trackLightChangesHorizontal(final Chunk chunk, final int x, final int z, final int sectionMask, final EnumLightType lightType)
    {
        final int xRegion = LightUtils.getBoundaryRegion(x & 15);
        final int zRegion = LightUtils.getBoundaryRegion(z & 15);

        if (xRegion != 0)
        {
            EnumFacing.byHorizontalIndex(2 + xRegion);
            final EnumFacing dir = EnumFacing.byHorizontalIndex(2 + xRegion);

            final int index = getHorizontalFlagIndex(dir) - LightUtils.getBoundaryRegionSigned(zRegion, xRegion, 0);

            trackLightChanges(chunk, index, sectionMask, lightType);
        }

        if (zRegion != 0)
        {
            final EnumFacing dir = EnumFacing.byHorizontalIndex(1 - zRegion);

            final int index = getHorizontalFlagIndex(dir) - LightUtils.getBoundaryRegionSigned(xRegion, 0, zRegion);

            trackLightChanges(chunk, index, sectionMask, lightType);
        }
    }

    public static void trackLightChangeVertical(final Chunk chunk, final BlockPos pos, final EnumLightType lightType)
    {
        final int y = pos.getY();

        final int region = LightUtils.getBoundaryRegion(y & 15);

        if (region != 0)
        {
            final int dirIndex = ((region + 1) >> 1);
            final int index = NEIGHBOR_FLAG_COUNT + dirIndex;

            trackLightChanges(chunk, index, (1 << (y >> 4)) & VERTICAL_MASKS[dirIndex], lightType);
        }
    }

    public static void trackLightChangesVertical(final Chunk chunk, final int sectionMask, final EnumLightType lightType)
    {
        trackLightChanges(chunk, NEIGHBOR_FLAG_COUNT, sectionMask & VERTICAL_MASKS[0], lightType);
        trackLightChanges(chunk, NEIGHBOR_FLAG_COUNT + 1, sectionMask & VERTICAL_MASKS[1], lightType);
    }

    private static void trackLightChanges(final Chunk chunk, final int index, final int sectionMask, final EnumLightType lightType)
    {
        initLightTrackings(chunk);
        chunk.getLightTrackings()[index] |= sectionMask << (LightUtils.getIndex(lightType) << 4);
    }

    public static void onChunkReceive(final Chunk chunk, final int sectionMask)
    {
        final int flagMask = sectionMask | (sectionMask << 16);

        if (chunk.getNeighborLightTrackings() != null)
        {
            for (int i = 0; i < chunk.getNeighborLightTrackings().length; ++i)
                chunk.getNeighborLightTrackings()[i] &= ~flagMask;
        }

        final IChunkProvider provider = chunk.getWorld().getChunkProvider();

        for (final EnumFacing dir : EnumFacing.Plane.HORIZONTAL)
        {
            int index = getHorizontalFlagIndex(dir);

            final EnumFacing oppDir = dir.getOpposite();

            final Chunk nChunk = provider.getChunk(chunk.x + dir.getXOffset(), chunk.z + dir.getZOffset(), false, false);

            if (chunk.getLightTrackings() != null)
            {
                for (int offset = -1; offset <= 1; ++offset)
                {
                    final int flags = chunk.getLightTrackings()[index + offset] & flagMask;
                    chunk.getLightTrackings()[index + offset] &= ~flagMask;

                    if (nChunk != null && flags != 0)
                        LightBoundaryCheckHooks.flagHorizontalSecBoundaryForCheckClient(nChunk, oppDir, offset, flags);
                }
            }

            if (nChunk == null || nChunk.getLightTrackings() == null)
                continue;

            index = getHorizontalFlagIndex(oppDir);

            for (int offset = -1; offset <= 1; ++offset)
            {
                final int flags = nChunk.getLightTrackings()[index + offset] & flagMask;

                if (flags != 0)
                    LightBoundaryCheckHooks.flagHorizontalSecBoundaryForCheckClient(chunk, dir, offset, flags);
            }
        }

        if (chunk.getLightTrackings() == null)
            return;

        int index = LightBoundaryCheckHooks.getVerticalFlagIndex(EnumFacing.DOWN, LightBoundaryCheckHooks.EnumBoundaryFacing.OUT);

        int flags = ((chunk.getLightTrackings()[index] & flagMask) >>> 1) & ~flagMask;
        chunk.getLightTrackings()[index] &= ~flagMask;

        if (flags != 0)
            LightBoundaryCheckHooks.flagVerticalSecBoundaryForCheckClient(chunk, EnumFacing.UP, flags);

        index = LightBoundaryCheckHooks.getVerticalFlagIndex(EnumFacing.UP, LightBoundaryCheckHooks.EnumBoundaryFacing.OUT);

        flags = ((chunk.getLightTrackings()[index] & flagMask) << 1) & ~flagMask;
        chunk.getLightTrackings()[index] &= ~flagMask;

        if (flags != 0)
            LightBoundaryCheckHooks.flagVerticalSecBoundaryForCheckClient(chunk, EnumFacing.DOWN, flags);
    }

    public static void sendLightTrackings(final PlayerChunkMapEntry chunk, final int sectionMask, final PlayerChunkMap chunkMap)
    {
        for (final EntityPlayerMP player : chunk.getLightTrackings().keySet())
            sendLightTrackings(chunk, player, sectionMask, chunkMap);
    }

    public static void sendLightTrackings(
            final PlayerChunkMapEntry chunk,
            final EntityPlayerMP player,
            final PlayerChunkMap chunkMap
    )
    {
        sendLightTrackings(chunk, player, (1 << 16) - 1, chunkMap);
    }

    public static void sendLightTrackings(
            final PlayerChunkMapEntry chunk,
            final EntityPlayerMP player,
            final int sectionMask,
            final PlayerChunkMap chunkMap
    )
    {
        final int[] lightTrackings = extractSyncLightTrackings(chunk, player, sectionMask, chunkMap);

        if (lightTrackings != null)
        {
            // new SPacketLightTracking(chunk.getPos(), lightTrackings).send(player);
        }
    }

    private static @Nullable int[] extractSyncLightTrackings(
            final PlayerChunkMapEntry playerChunk,
            final EntityPlayerMP player,
            final int sectionMask,
            final PlayerChunkMap chunkMap
    )
    {
        final Chunk chunk = playerChunk.getChunk();

        if (chunk == null)
            return null;

        final int flagMask = sectionMask | (sectionMask << 16);

        applyTrackings(playerChunk, chunkMap);

        int[] ret = null;

        final int[] neighborLightTrackings = playerChunk.getNeighborLightTrackings().get(player);

        if (neighborLightTrackings != null)
        {
            for (int i = 0; i < NEIGHBOR_FLAG_COUNT; ++i)
                neighborLightTrackings[i] &= ~flagMask;
        }

        if (chunk.getNeighborLightChecks() != null)
        {
            for (int i = 0; i < LightBoundaryCheckHooks.OUT_INDEX_OFFSET; ++i)
            {
                final int flags = chunk.getNeighborLightChecks()[i] & flagMask;

                ret = addSyncLightTrackings(ret, flags, i);
            }
        }

        final int[] lightTrackings = playerChunk.getLightTrackings().get(player);

        if (lightTrackings != null)
        {
            int index = NEIGHBOR_FLAG_COUNT;

            int flags = ((lightTrackings[index] & flagMask) >>> 1) & ~flagMask;
            lightTrackings[index] &= ~flagMask;

            ret = addSyncLightTrackings(ret, flags, LightBoundaryCheckHooks.getVerticalFlagIndex(EnumFacing.UP, LightBoundaryCheckHooks.EnumBoundaryFacing.IN));

            ++index;

            flags = ((lightTrackings[index] & flagMask) << 1) & ~flagMask;
            lightTrackings[index] &= ~flagMask;

            ret = addSyncLightTrackings(ret, flags, LightBoundaryCheckHooks.getVerticalFlagIndex(EnumFacing.DOWN, LightBoundaryCheckHooks.EnumBoundaryFacing.IN));
        }

        for (final EnumFacing dir : EnumFacing.Plane.HORIZONTAL)
        {
            applyTrackings(playerChunk, dir, chunkMap);

            int index = getHorizontalFlagIndex(dir);

            final EnumFacing oppDir = dir.getOpposite();

            final PlayerChunkMapEntry nPlayerChunk = chunkMap.getEntry(chunk.x + dir.getXOffset(), chunk.z + dir.getZOffset());
            final boolean neighborLoaded = nPlayerChunk != null && nPlayerChunk.getLightTrackings().containsKey(player);
            final int[] nLightTrackings = neighborLoaded ? nPlayerChunk.getLightTrackings().get(player) : null;

            if (lightTrackings != null)
            {
                for (int offset = -1; offset <= 1; ++offset)
                {
                    if (neighborLoaded)
                    {
                        final int flags = lightTrackings[index + offset] & flagMask;

                        ret = addSyncLightTrackings(ret, flags, LightBoundaryCheckHooks.FLAG_COUNT_CLIENT + index + offset);
                    }

                    lightTrackings[index + offset] &= ~flagMask;
                }
            }

            if (nLightTrackings == null)
                continue;

            index = getHorizontalFlagIndex(oppDir);

            for (int offset = -1; offset <= 1; ++offset)
            {
                final int flags = nLightTrackings[index + offset] & flagMask;

                ret = addSyncLightTrackings(ret, flags, LightBoundaryCheckHooks.getHorizontalFlagIndex(dir, LightBoundaryCheckHooks.EnumBoundaryFacing.IN, offset));
            }
        }

        return ret;
    }

    public static void applySyncLightTrackins(final int[] lightTrackings, final int chunkX, final int chunkZ, final World world)
    {
        final IChunkProvider provider = world.getChunkProvider();

        final Chunk chunk = provider.getChunk(chunkX, chunkZ, false, false);

        if (chunk != null)
        {
            for (int i = 0; i < LightBoundaryCheckHooks.FLAG_COUNT_CLIENT; ++i)
            {
                final int flags = lightTrackings[i];

                if (flags != 0)
                    LightBoundaryCheckHooks.flagSecBoundaryForCheckClient(chunk, i, flags);
            }
        }

        for (final EnumFacing dir : EnumFacing.Plane.HORIZONTAL)
        {
            final Chunk nChunk = provider.getChunk(chunkX + dir.getXOffset(), chunkZ + dir.getZOffset(), false, false);

            if (nChunk == null)
                continue;

            final int index = LightBoundaryCheckHooks.FLAG_COUNT_CLIENT + getHorizontalFlagIndex(dir);

            final EnumFacing oppDir = dir.getOpposite();

            for (int offset = -1; offset <= 1; ++offset)
            {
                final int flags = lightTrackings[index + offset];

                if (flags != 0)
                    LightBoundaryCheckHooks.flagHorizontalSecBoundaryForCheckClient(nChunk, oppDir, offset, flags);
            }
        }
    }

    private static @Nullable int[] addSyncLightTrackings(@Nullable int[] lightTrackings, final int flags, final int index)
    {
        if (flags != 0)
        {
            if (lightTrackings == null)
                lightTrackings = new int[SYNC_FLAG_COUNT];

            lightTrackings[index] |= flags;
        }

        return lightTrackings;
    }

    public static void addPlayer(final EntityPlayerMP player, final PlayerChunkMapEntry chunk, final PlayerChunkMap chunkMap)
    {
        applyTrackings(chunk, chunkMap);

        chunk.getLightTrackings().put(player, null);

        final ChunkPos pos = chunk.getPos();

        int[] neighborLightTrackings = null;

        for (final EnumFacing dir : EnumFacing.Plane.HORIZONTAL)
        {
            final PlayerChunkMapEntry nChunk = chunkMap.getEntry(pos.x + dir.getXOffset(), pos.z + dir.getZOffset());

            if (nChunk == null || !nChunk.getLightTrackings().containsKey(player))
            {
                if (neighborLightTrackings == null)
                    neighborLightTrackings = new int[PLAYER_NEIGHBOR_FLAG_COUNT];

                neighborLightTrackings[NEIGHBOR_FLAG_COUNT] |= (1 << dir.getOpposite().getHorizontalIndex());

                applyTrackings(chunk, dir, chunkMap);
            }
            else
            {
                final int[] lightTrackings = nChunk.getNeighborLightTrackings().get(player);

                if (lightTrackings != null)
                {
                    final int dirFlag = 1 << dir.getHorizontalIndex();

                    if (!isLightTrackingsEmpty(lightTrackings, dir))
                    {
                        copyLightTrackings(lightTrackings, initPlayerLightTrackings(chunk, player), dir);
                        clearLightTrackings(lightTrackings, dir);
                    }

                    lightTrackings[NEIGHBOR_FLAG_COUNT] &= ~dirFlag;

                    if (lightTrackings[NEIGHBOR_FLAG_COUNT] == 0)
                        nChunk.getNeighborLightTrackings().remove(player);
                }
            }
        }

        if (neighborLightTrackings != null)
            chunk.getNeighborLightTrackings().put(player, neighborLightTrackings);
    }

    public static void removePlayer(final EntityPlayerMP player, final PlayerChunkMapEntry chunk, final PlayerChunkMap chunkMap)
    {
        chunk.getNeighborLightTrackings().remove(player);

        final ChunkPos pos = chunk.getPos();

        final int[] lightTrackings = chunk.getLightTrackings().get(player);

        for (final EnumFacing dir : EnumFacing.Plane.HORIZONTAL)
        {
            final PlayerChunkMapEntry nChunk = chunkMap.getEntry(pos.x + dir.getXOffset(), pos.z + dir.getZOffset());

            if (nChunk != null && nChunk.getLightTrackings().containsKey(player))
            {
                int[] neighborLightTrackings = nChunk.getNeighborLightTrackings().get(player);

                if (neighborLightTrackings == null)
                {
                    neighborLightTrackings = new int[PLAYER_NEIGHBOR_FLAG_COUNT];
                    nChunk.getNeighborLightTrackings().put(player, neighborLightTrackings);
                }

                final int dirFlag = 1 << dir.getHorizontalIndex();

                neighborLightTrackings[NEIGHBOR_FLAG_COUNT] |= dirFlag;

                if (lightTrackings != null)
                    copyLightTrackings(lightTrackings, neighborLightTrackings, dir);
            }
        }

        chunk.getLightTrackings().remove(player);
    }

    static void applyTrackings(final PlayerChunkMapEntry playerChunk, final PlayerChunkMap chunkMap)
    {
        final Chunk chunk = playerChunk.getChunk();

        if (chunk == null || chunk.getLightTrackings() == null || isLightTrackingsEmpty(chunk.getLightTrackings()))
            return;

        for (final Entry<EntityPlayerMP, int[]> item : playerChunk.getLightTrackings().entrySet())
        {
            initPlayerLightTrackings(item);
            final int[] playerLightTrackings = item.getValue();

            for (int i = 0; i < chunk.getLightTrackings().length; ++i)
                playerLightTrackings[i] |= chunk.getLightTrackings()[i];
        }

        for (final EnumFacing dir : EnumFacing.Plane.HORIZONTAL)
        {
            if (isLightTrackingsEmpty(chunk.getLightTrackings(), dir))
                continue;

            final PlayerChunkMapEntry nPlayerChunk = chunkMap.getEntry(chunk.x + dir.getXOffset(), chunk.z + dir.getZOffset());

            if (nPlayerChunk == null)
                continue;

            for (final Entry<EntityPlayerMP, int[]> item : nPlayerChunk.getNeighborLightTrackings().entrySet())
            {
                final int[] playerLightTrackings = item.getValue();

                if ((playerLightTrackings[NEIGHBOR_FLAG_COUNT] & (1 << dir.getHorizontalIndex())) != 0)
                    copyLightTrackings(chunk.getLightTrackings(), playerLightTrackings, dir);
            }
        }

        Arrays.fill(chunk.getLightTrackings(), 0);
    }

    static void applyTrackings(final PlayerChunkMapEntry playerChunk, final EnumFacing dir, final PlayerChunkMap chunkMap)
    {
        final EnumFacing oppDir = dir.getOpposite();

        final ChunkPos pos = playerChunk.getPos();

        final Chunk chunk = playerChunk.getChunk();
        final Chunk nChunk = chunkMap.getWorld().getChunkProvider().getChunk(pos.x + dir.getXOffset(), pos.z + dir.getZOffset(), false, false);

        if (nChunk != null)
        {
            if (nChunk.getLightTrackings() != null && !isLightTrackingsEmpty(nChunk.getLightTrackings(), oppDir))
            {
                final PlayerChunkMapEntry nPlayerChunk = chunkMap.getEntry(pos.x + dir.getXOffset(), pos.z + dir.getZOffset());

                if (nPlayerChunk != null)
                {
                    for (final Entry<EntityPlayerMP, int[]> item : nPlayerChunk.getLightTrackings().entrySet())
                    {
                        initPlayerLightTrackings(item);
                        final int[] playerLightTrackings = item.getValue();

                        copyLightTrackings(nChunk.getLightTrackings(), playerLightTrackings, oppDir);
                    }
                }

                for (final Entry<EntityPlayerMP, int[]> item : playerChunk.getNeighborLightTrackings().entrySet())
                {
                    final int[] playerLightTrackings = item.getValue();

                    if ((playerLightTrackings[NEIGHBOR_FLAG_COUNT] & (1 << oppDir.getHorizontalIndex())) != 0)
                        copyLightTrackings(nChunk.getLightTrackings(), playerLightTrackings, oppDir);
                }

                clearLightTrackings(nChunk.getLightTrackings(), oppDir);
            }
        }
        else if (chunk != null && chunk.getNeighborLightTrackings() != null && !isLightTrackingsEmpty(chunk.getNeighborLightTrackings(), oppDir))
        {
            for (final Entry<EntityPlayerMP, int[]> item : playerChunk.getNeighborLightTrackings().entrySet())
            {
                final int[] playerLightTrackings = item.getValue();

                copyLightTrackings(chunk.getNeighborLightTrackings(), playerLightTrackings, oppDir);
            }

            clearLightTrackings(chunk.getNeighborLightTrackings(), oppDir);
        }
    }

    public static void onLoad(final World world, final Chunk chunk)
    {
        final IChunkProvider provider = world.getChunkProvider();

        for (final EnumFacing dir : EnumFacing.Plane.HORIZONTAL)
        {
            final Chunk nChunk = provider.getChunk(chunk.x + dir.getXOffset(), chunk.z + dir.getZOffset(), false, false);

            if (nChunk != null && nChunk.getNeighborLightTrackings() != null && !isLightTrackingsEmpty(nChunk.getNeighborLightTrackings(), dir))
            {
                initLightTrackings(chunk);
                copyLightTrackings(nChunk.getNeighborLightTrackings(), chunk.getLightTrackings(), dir);
                clearLightTrackings(nChunk.getNeighborLightTrackings(), dir);
            }
        }
    }

    public static void onUnload(final World world, final Chunk chunk)
    {
        if (chunk.getLightTrackings() == null)
            return;

        final IChunkProvider provider = world.getChunkProvider();

        for (final EnumFacing dir : EnumFacing.Plane.HORIZONTAL)
        {
            final Chunk nChunk = provider.getChunk(chunk.x + dir.getXOffset(), chunk.z + dir.getZOffset(), false, false);

            if (nChunk != null && !isLightTrackingsEmpty(chunk.getLightTrackings(), dir))
            {
                initNeighborLightTrackings(nChunk);
                copyLightTrackings(chunk.getLightTrackings(), nChunk.getNeighborLightTrackings(), dir);
            }
        }
    }

    public static boolean isLightTrackingsEmpty(final int[] lightTrackings)
    {
        for (final int tracking : lightTrackings)
        {
            if (tracking != 0)
                return false;
        }

        return true;
    }

    public static boolean isLightTrackingsEmpty(final int[] lightTrackings, final EnumFacing dir)
    {
        final int index = getHorizontalFlagIndex(dir);

        for (int offset = -1; offset <= 1; ++offset)
        {
            if (lightTrackings[index + offset] != 0)
                return false;
        }

        return true;
    }

    public static void copyLightTrackings(final int[] src, final int[] dst, final EnumFacing dir)
    {
        final int index = getHorizontalFlagIndex(dir);

        for (int offset = -1; offset <= 1; ++offset)
            dst[index + offset] |= src[index + offset];
    }

    public static void clearLightTrackings(final int[] lightTrackings, final EnumFacing dir)
    {
        final int index = getHorizontalFlagIndex(dir);

        for (int offset = -1; offset <= 1; ++offset)
            lightTrackings[index + offset] = 0;
    }

    public static int[] initPlayerLightTrackings(final PlayerChunkMapEntry chunk, final EntityPlayerMP player)
    {
        int[] lightTrackings = chunk.getLightTrackings().get(player);

        if (lightTrackings == null)
        {
            lightTrackings = new int[FLAG_COUNT];
            chunk.getLightTrackings().put(player, lightTrackings);
        }

        return lightTrackings;
    }

    public static void initPlayerLightTrackings(final Entry<EntityPlayerMP, int[]> item)
    {
        if (item.getValue() == null)
            item.setValue(new int[FLAG_COUNT]);
    }

    public static void initLightTrackings(final Chunk chunk)
    {
        if (chunk.getLightTrackings() == null)
            chunk.setLightTrackings(new int[FLAG_COUNT]);
    }

    public static void initNeighborLightTrackings(final Chunk chunk)
    {
        if (chunk.getNeighborLightTrackings() == null)
            chunk.setNeighborLightTrackings(new int[NEIGHBOR_FLAG_COUNT]);
    }
}
