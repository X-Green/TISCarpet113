package carpet.utils;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.Heightmap;

public class LightInitHooks
{
    public static final String neighborLightInitsKey = "PendingNeighborLightInits";
    public static final int CHUNK_COORD_OVERFLOW_MASK = -1 << 4;

    public static void fillSkylightColumn(final Chunk chunk, final int x, final int z)
    {
        final ChunkSection[] chunkSection = chunk.getSections();

        final Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.LIGHT_BLOCKING);
        final int height = heightmap.getHeight(x, z);

        for (int j = height >> 4; j < chunkSection.length; ++j)
        {
            final ChunkSection blockStorage = chunkSection[j];

            if (blockStorage == Chunk.EMPTY_SECTION)
                continue;

            final int yMin = Math.max(j << 4, height);

            for (int y = yMin & 15; y < 16; ++y)
                blockStorage.setSkyLight(x, y, z, EnumLightType.SKY.defaultLightValue);
        }

        chunk.markDirty();
    }

    public static void initChunkLighting(final World world, final Chunk chunk)
    {
        if (chunk.getIsLightPopulated() || chunk.getPendingNeighborLightInits() != 0)
            return;

        chunk.setPendingNeighborLightInits((short)31);

        chunk.markDirty();

        final int xBase = chunk.x << 4;
        final int zBase = chunk.z << 4;

        final PooledMutableBlockPos pos = PooledMutableBlockPos.retain();

        final ChunkSection[] ChunkSection = chunk.getSections();

        for (int j = 0; j < ChunkSection.length; ++j)
        {
            final ChunkSection blockStorage = ChunkSection[j];

            if (blockStorage == Chunk.EMPTY_SECTION)
                continue;

            for (int x = 0; x < 16; ++x)
            {
                for (int z = 0; z < 16; ++z)
                {
                    for (int y = 0; y < 16; ++y)
                    {
                        if (LightUtils.getLightValue(blockStorage.get(x, y, z), world, pos.setPos(xBase + x, (j << 4) + y, zBase + z)) > 0)
                            world.checkLightFor(EnumLightType.BLOCK, pos);
                    }
                }
            }
        }

        if (world.dimension.hasSkyLight())
        {
            final Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.LIGHT_BLOCKING);
            for (int x = 0; x < 16; ++x)
            {
                for (int z = 0; z < 16; ++z)
                {
                    final int yMax = heightmap.getHeight(x, z);
                    int yMin = Math.max(yMax - 1, 0);

                    for (final EnumFacing dir : EnumFacing.Plane.HORIZONTAL)
                    {
                        final int nX = x + dir.getXOffset();
                        final int nZ = z + dir.getZOffset();

                        if (((nX | nZ) & CHUNK_COORD_OVERFLOW_MASK) != 0)
                            continue;

                        yMin = Math.min(yMin, heightmap.getHeight(nX, nZ));
                    }

                    LightUtils.scheduleRelightChecksForColumn(world, EnumLightType.SKY, xBase + x, zBase + z, yMin, yMax - 1, pos);
                }
            }
        }

        pos.close();
    }

    static void initNeighborLight(final World world, final Chunk chunk, final Chunk nChunk, final EnumFacing nDir)
    {
        final int flag = 1 << nDir.getHorizontalIndex();

        if ((chunk.getPendingNeighborLightInits() & flag) == 0)
            return;

        chunk.setPendingNeighborLightInits((short)(chunk.getPendingNeighborLightInits() ^ flag));

        chunk.markDirty();

        final int xOffset = nDir.getXOffset();
        final int zOffset = nDir.getZOffset();

        final int xMin;
        final int zMin;

        if ((xOffset | zOffset) > 0)
        {
            xMin = 0;
            zMin = 0;
        }
        else
        {
            xMin = 15 * (xOffset & 1);
            zMin = 15 * (zOffset & 1);
        }

        final int xMax = xMin + 15 * (zOffset & 1);
        final int zMax = zMin + 15 * (xOffset & 1);

        final int xBase = nChunk.x << 4;
        final int zBase = nChunk.z << 4;

        final PooledMutableBlockPos pos = PooledMutableBlockPos.retain();

        final Heightmap heightmapChunk = chunk.getHeightmap(Heightmap.Type.LIGHT_BLOCKING);
        final Heightmap heightmapNChunk = nChunk.getHeightmap(Heightmap.Type.LIGHT_BLOCKING);
        for (int x = xMin; x <= xMax; ++x)
        {
            for (int z = zMin; z <= zMax; ++z)
            {
                int yMin = heightmapChunk.getHeight((x - xOffset) & 15, (z - zOffset) & 15);

                // Restore a value <= initial height
                for (; yMin > 0; --yMin)
                {
                    if (chunk.getLight(EnumLightType.SKY, pos.setPos(xBase + x - xOffset, yMin - 1, zBase + z - zOffset), chunk.getWorld().dimension.hasSkyLight()) < EnumLightType.SKY.defaultLightValue)
                        break;
                }

                int yMax = heightmapNChunk.getHeight(x, z) - 1;

                for (final EnumFacing dir : EnumFacing.Plane.HORIZONTAL)
                {
                    final int nX = x + dir.getXOffset();
                    final int nZ = z + dir.getZOffset();

                    if (((nX | nZ) & CHUNK_COORD_OVERFLOW_MASK) != 0)
                        continue;

                    yMax = Math.min(yMax, heightmapNChunk.getHeight(nX, nZ));
                }

                LightUtils.scheduleRelightChecksForColumn(world, EnumLightType.SKY, xBase + x, zBase + z, yMin, yMax - 1, pos);
            }
        }

        pos.close();
    }

    public static void initNeighborLight(final World world, final Chunk chunk)
    {
        final IChunkProvider provider = world.getChunkProvider();

        for (final EnumFacing dir : EnumFacing.Plane.HORIZONTAL)
        {
            final Chunk nChunk = provider.getChunk(chunk.x + dir.getXOffset(), chunk.z + dir.getZOffset(), false, false);

            if (nChunk == null)
                continue;

            initNeighborLight(world, chunk, nChunk, dir);
            initNeighborLight(world, nChunk, chunk, dir.getOpposite());

            checkNeighborsLoaded(provider, nChunk);
        }

        for (int x = 0; x <= 1; ++x)
            for (int z = 0; z <= 1; ++z)
            {
                final Chunk nChunk = provider.getChunk(chunk.x + (x << 1) - 1, chunk.z + (z << 1) - 1, false, false);

                if (nChunk != null)
                    checkNeighborsLoaded(provider, nChunk);
            }

        checkNeighborsLoaded(provider, chunk);
    }

    private static void checkNeighborsLoaded(final IChunkProvider provider, final Chunk chunk)
    {
        if (chunk.getPendingNeighborLightInits() != 16)
            return;

        for (int x = -1; x <= 1; ++x)
            for (int z = -1; z <= 1; ++z)
            {
                if (x == 0 && z == 0)
                    continue;

                if (provider.getChunk(chunk.x + x, chunk.z + z, false, false) == null)
                    return;
            }

        chunk.setPendingNeighborLightInits((short)0);
        chunk.setIsLightPopulated(true);
        chunk.markDirty();
    }

    public static void writeNeighborInitsToNBT(final Chunk chunk, final NBTTagCompound nbt)
    {
        if (chunk.getPendingNeighborLightInits() != 0)
            nbt.putShort(neighborLightInitsKey, chunk.getPendingNeighborLightInits());
    }

    public static void readNeighborInitsFromNBT(final Chunk chunk, final NBTTagCompound nbt)
    {
        if (nbt.contains(neighborLightInitsKey, 2))
            chunk.setPendingNeighborLightInits(nbt.getShort(neighborLightInitsKey));
    }
}
