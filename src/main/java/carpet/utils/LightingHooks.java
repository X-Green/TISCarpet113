package carpet.utils;

/*
 * Copyright PhiPro
 */

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BitArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.Heightmap;

import javax.annotation.Nullable;

public class LightingHooks
{
    public static void onLoadServer(final World world, final Chunk chunk)
    {
        LightTrackingHooks.onLoad(world, chunk);
        LightInitHooks.initChunkLighting(world, chunk);
        LightInitHooks.initNeighborLight(world, chunk);
        LightBoundaryCheckHooks.scheduleRelightChecksForChunkBoundariesServer(world, chunk);
    }

    public static void onLoadClient(final World world, final Chunk chunk)
    {
        LightTrackingHooks.onLoad(world, chunk);
        LightBoundaryCheckHooks.onLoad(world, chunk);
    }

    public static void onUnload(final World world, final Chunk chunk)
    {
        world.getLightingEngine().procLightUpdates();
        LightTrackingHooks.onUnload(world, chunk);
        LightBoundaryCheckHooks.onUnload(world, chunk);
    }

    public static void onTick(final World world, final Chunk chunk)
    {
        LightBoundaryCheckHooks.onTick(world, chunk);
    }

    public static void writeLightData(final Chunk chunk, final NBTTagCompound nbt)
    {
        LightInitHooks.writeNeighborInitsToNBT(chunk, nbt);
        LightBoundaryCheckHooks.writeNeighborLightChecksToNBT(chunk, nbt);
    }

    public static void readLightData(final Chunk chunk, final NBTTagCompound nbt)
    {
        LightInitHooks.readNeighborInitsFromNBT(chunk, nbt);
        LightBoundaryCheckHooks.readNeighborLightChecksFromNBT(chunk, nbt);
    }

    public static void initSkylightForSection(final World world, final Chunk chunk, final ChunkSection section)
    {
        if (world.dimension.hasSkyLight())
        {
            for (int x = 0; x < 16; ++x)
            {
                for (int z = 0; z < 16; ++z)
                {
                    if (chunk.getTopBlockY(Heightmap.Type.LIGHT_BLOCKING, x, z) <= section.getYLocation())
                    {
                        for (int y = 0; y < 16; ++y)
                        {
                            section.setSkyLight(x, y, z, EnumLightType.SKY.defaultLightValue);
                        }
                    }
                }
            }
        }
    }

    public static void relightSkylightColumns(final World world, final Chunk chunk, @Nullable final long[] oldHeightMap)
    {
        if (!world.dimension.hasSkyLight())
            return;

        if (oldHeightMap == null)
            return;

        final PooledMutableBlockPos pos = PooledMutableBlockPos.retain();
        final Heightmap map = new Heightmap(null, Heightmap.Type.LIGHT_BLOCKING);
        map.setDataArray(oldHeightMap);

        for (int x = 0; x < 16; ++x)
        {
            for (int z = 0; z < 16; ++z)
                relightSkylightColumn(world, chunk, x, z, map.getHeight(x, z), chunk.getHeightmap(Heightmap.Type.LIGHT_BLOCKING).getHeight(x, z), pos);
        }

        pos.close();
    }

    public static void relightSkylightColumn(final World world, final Chunk chunk, final int x, final int z, final int height1, final int height2)
    {
        final PooledMutableBlockPos pos = PooledMutableBlockPos.retain();
        relightSkylightColumn(world, chunk, x, z, height1, height2, pos);
        pos.close();
    }

    private static void relightSkylightColumn(final World world, final Chunk chunk, final int x, final int z, final int height1, final int height2, final BlockPos.MutableBlockPos pos)
    {
        final int yMin = Math.min(height1, height2);
        final int yMax = Math.max(height1, height2) - 1;

        final ChunkSection[] sections = chunk.getSections();

        final int xBase = (chunk.x << 4) + x;
        final int zBase = (chunk.z << 4) + z;

        LightUtils.scheduleRelightChecksForColumn(world, EnumLightType.SKY, xBase, zBase, yMin, yMax, pos);

        if (sections[yMin >> 4] == Chunk.EMPTY_SECTION && yMin > 0)
        {
            world.checkLightFor(EnumLightType.SKY, new BlockPos(xBase, yMin - 1, zBase));
        }

        short emptySections = 0;

        for (int sec = yMax >> 4; sec >= yMin >> 4; --sec)
        {
            if (sections[sec] == Chunk.EMPTY_SECTION)
            {
                emptySections |= 1 << sec;
            }
        }

        if (emptySections != 0)
        {
            for (final EnumFacing dir : EnumFacing.Plane.HORIZONTAL)
            {
                final int xOffset = dir.getXOffset();
                final int zOffset = dir.getZOffset();

                final boolean neighborColumnExists =
                        (((x + xOffset) | (z + zOffset)) & 16) == 0 //Checks whether the position is at the specified border (the 16 bit is set for both 15+1 and 0-1)
                                || world.getChunkProvider().getChunk(chunk.x + xOffset, chunk.z + zOffset,false, false) != null;

                if (neighborColumnExists)
                {
                    for (int sec = yMax >> 4; sec >= yMin >> 4; --sec)
                    {
                        if ((emptySections & (1 << sec)) != 0)
                        {
                            LightUtils.scheduleRelightChecksForColumn(world, EnumLightType.SKY, xBase + xOffset, zBase + zOffset, sec << 4, (sec << 4) + 15, pos);
                        }
                    }
                }
                else
                    LightBoundaryCheckHooks.flagOuterChunkBoundaryForUpdate(chunk, x, z, dir, emptySections, EnumLightType.SKY);
            }

            LightTrackingHooks.trackLightChangesHorizontal(chunk, x, z, emptySections, EnumLightType.SKY);
            LightTrackingHooks.trackLightChangesVertical(chunk, emptySections, EnumLightType.SKY);
        }
    }
}