package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.settings.CarpetSettings;
import carpet.utils.Messenger;
import carpet.utils.WoolTool;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;

import java.util.*;

public class MicroTickLogHelper
{
    public static MicroTickLogger logger = new MicroTickLogger();
    private static String stage;
    private static String stage_detail;
    private static String stage_extra;

    public static String getDimension(int dimensionID)
    {
        String dimension = null;
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

    public static String getDimensionCommand(int dimensionID)
    {
        String dimension = null;
        switch (dimensionID)
        {
            case 0:
                dimension = "minecraft:overworld";
                break;
            case 1:
                dimension = "minecraft:the_end";
                break;
            case -1:
                dimension = "minecraft:the_nether";
                break;
        }
        return dimension;
    }

    // called before action is done
    // [stage][detail]^[extra]

    public static void setTickStage(String stage)
    {
        MicroTickLogHelper.stage = stage;
    }
    public static String getTickStage()
    {
        return MicroTickLogHelper.stage;
    }
    public static void setTickStageDetail(String stage)
    {
        MicroTickLogHelper.stage_detail = stage;
    }
    public static String getTickStageDetail()
    {
        return MicroTickLogHelper.stage_detail;
    }
    public static void setTickStageExtra(String extra)
    {
        MicroTickLogHelper.stage_extra = extra;
    }
    public static String getTickStageExtra()
    {
        return MicroTickLogHelper.stage_extra;
    }

    private static String getColorStype(EnumDyeColor color)
    {
        switch (color)
        {
            case WHITE:
                return "w";
            case ORANGE:
                return "d";
            case MAGENTA:
                return "m";
            case LIGHT_BLUE:
                return "c";
            case YELLOW:
                return "y";
            case LIME:
                return "l";
            case PINK:
                return "r";
            case GRAY:
                return "f";
            case LIGHT_GRAY:
                return "g";
            case CYAN:
                return "q";
            case PURPLE:
                return "p";
            case BLUE:
                return "v";
            case BROWN:
                return "n";
            case GREEN:
                return "e";
            case RED:
                return "r";
            case BLACK:
                return "k";
        }
        return "w";
    }

    private static ITextComponent getTranslatedName(Block block)
    {
        ITextComponent name = new TextComponentTranslation(block.getTranslationKey());
        name.getStyle().setColor(TextFormatting.WHITE);
        return name;
    }

    private static EnumDyeColor getWoolColor(World worldIn, BlockPos pos)
    {
        if (!CarpetSettings.microTick || !LoggerRegistry.__microtick || worldIn.isRemote())
        {
            return null;
        }
        IBlockState state = worldIn.getBlockState(pos);
        Block block = state.getBlock();
        BlockPos woolPos = pos;

        if (block == Blocks.OBSERVER || block == Blocks.END_ROD ||
                block instanceof BlockPistonBase || block instanceof BlockPistonMoving)
        {
            woolPos = pos.offset(state.get(BlockStateProperties.FACING).getOpposite());
        }
        else if (block instanceof BlockButton || block instanceof BlockLever)
        {
            EnumFacing facing;
            if (state.get(BlockStateProperties.FACE) == AttachFace.FLOOR)
            {
                facing = EnumFacing.UP;
            }
            else if (state.get(BlockStateProperties.FACE) == AttachFace.CEILING)
            {
                facing = EnumFacing.DOWN;
            }
            else
            {
                facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
            }
            woolPos = pos.offset(facing.getOpposite());
        }
        else if (block == Blocks.REDSTONE_WALL_TORCH || block == Blocks.TRIPWIRE_HOOK)
        {
            woolPos = pos.offset(state.get(BlockHorizontal.HORIZONTAL_FACING).getOpposite());
        }
        else if (block instanceof BlockRailPowered ||
                block == Blocks.REPEATER || block == Blocks.COMPARATOR || block == Blocks.REDSTONE_TORCH ||
                block instanceof BlockBasePressurePlate)  // on block
        {
            woolPos = pos.down();
        }
        else
        {
            return null;
        }

        return WoolTool.getWoolColorAtPosition(worldIn.getWorld(), woolPos);
    }

    // called after action is done

    public static void onBlockUpdated(World worldIn, BlockPos pos, String type)
    {
        EnumDyeColor color = getWoolColor(worldIn, pos);
        if (color != null)
        {
            logger.addMessage(color, pos, worldIn, new Object[]{"w Block Update", "c " + type});
        }
    }

    public static void onComponentAddToTileTickList(World worldIn, BlockPos pos, int length, TickPriority priority)
    {
        EnumDyeColor color = getWoolColor(worldIn, pos);
        if (color != null)
        {
            logger.addMessage(color, pos, worldIn, new Object[]{getTranslatedName(worldIn.getBlockState(pos).getBlock()),
                    "c  Added +" + length + "gt TileTick." + priority.getPriority()});
        }
    }
    public static void onComponentAddToTileTickList(World worldIn, BlockPos pos, int length)
    {
        onComponentAddToTileTickList(worldIn, pos, length, TickPriority.NORMAL);
    }

    public static void onComponentAddToBlockEvent(World worldIn, BlockPos pos, int eventID, int eventParam)
    {
        EnumDyeColor color = getWoolColor(worldIn, pos);
        if (color != null)
        {
            logger.addMessage(color, pos, worldIn, new Object[]{getTranslatedName(worldIn.getBlockState(pos).getBlock()),
                    "c  Added BlockEvent(" + eventID + ", " + eventParam + ")"});
        }
    }

    public static void onPistonMove(World worldIn, BlockPos pos, Block block, int id) // "block" only overwrites displayed name
    {
        EnumDyeColor color = getWoolColor(worldIn, pos);
        if (color != null)
        {
            String types[] = new String[]{"Pushed", "Pulled", "Dropped"};
            logger.addMessage(color, pos, worldIn, new Object[]{getTranslatedName(block),
                    "c  " + types[id]});
        }
    }
    public static void onPistonMove(World worldIn, BlockPos pos, int id)
    {
        onPistonMove(worldIn, pos, worldIn.getBlockState(pos).getBlock(), id);
    }

    public static void onComponentPowered(World worldIn, BlockPos pos, boolean poweredState)
    {
        EnumDyeColor color = getWoolColor(worldIn, pos);
        if (color != null)
        {
            logger.addMessage(color, pos, worldIn, new Object[]{getTranslatedName(worldIn.getBlockState(pos).getBlock()),
                    "c  " + (poweredState ? "Powered" : "Depowered")});
        }
    }

    public static void onRedstoneTorchLit(World worldIn, BlockPos pos, boolean litState)
    {
        EnumDyeColor color = getWoolColor(worldIn, pos);
        if (color != null)
        {
            logger.addMessage(color, pos, worldIn, new Object[]{getTranslatedName(worldIn.getBlockState(pos).getBlock()),
                    "c  " + (litState ? "Lit" : "Unlit")});
        }
    }

    public static void flushMessages(long gameTime)
    {
        if (logger.messages.isEmpty())
        {
            return;
        }
        LoggerRegistry.getLogger("microtick").log( (option) ->
        {
            boolean uniqueOnly = option.equals("unique");
            List<ITextComponent> msg = logger.flushMessages(gameTime, uniqueOnly);
            return msg.toArray(new ITextComponent[0]);
        });
        logger.clearMessages();
    }

    public static class MicroTickLogger
    {
        private List<MicroTickLogHelperMessage> messages = Lists.newArrayList();

        public void addMessage(EnumDyeColor color, BlockPos pos, int dimensionID, Object [] texts)
        {
            MicroTickLogHelperMessage message = new MicroTickLogHelperMessage(dimensionID, pos, color, texts);
            message.stage = MicroTickLogHelper.getTickStage();
            message.stage_detail = MicroTickLogHelper.getTickStageDetail();
            message.stage_extra = MicroTickLogHelper.getTickStageExtra();
            this.messages.add(message);
        }
        public void addMessage(EnumDyeColor color, BlockPos pos, World worldIn, Object [] texts)
        {
            addMessage(color, pos, worldIn.getDimension().getType().getId(), texts);
        }

        private static ITextComponent getHashTag(MicroTickLogHelperMessage msg)
        {
            String text = getColorStype(msg.color) + " # ";
            ITextComponent ret;
            if (msg.pos != null)
            {
                ret = Messenger.c(
                        text,
                        String.format("!/execute in " + getDimensionCommand(msg.dimensionID) + " run tp @s %d %d %d", msg.pos.getX(), msg.pos.getY(), msg.pos.getZ()),
                        String.format("^w [ %d, %d, %d ]", msg.pos.getX(), msg.pos.getY(), msg.pos.getZ())
                );
            }
            else
            {
                ret = Messenger.c(text);
            }
            return ret;
        }

        private static ITextComponent getStage(MicroTickLogHelperMessage msg)
        {
           return Messenger.c(
                    "g at ",
                    "y " + msg.stage + msg.stage_detail,
                    String.format("^w %sWorld: %s", msg.stage_extra, getDimension(msg.dimensionID))
            );
        }

        private List<ITextComponent> flushMessages(long gameTime, boolean uniqueOnly)
        {
            if (this.messages.isEmpty())
            {
                return null;
            }
            Set<MicroTickLogHelperMessage> messageHashSet = Sets.newHashSet();
            Iterator<MicroTickLogHelperMessage> iterator = this.messages.iterator();
            List<ITextComponent> ret = new ArrayList<>();
            ret.add(Messenger.c("f [GameTime ", "g " + gameTime, "f ] ---------------------------"));
            while (iterator.hasNext())
            {
                MicroTickLogHelperMessage message = iterator.next();

                boolean flag = !uniqueOnly;
                if (!messageHashSet.contains(message))
                {
                    messageHashSet.add(message);
                    flag = true;
                }
                if (flag)
                {
                    if (message.stage_extra == null)
                    {
                        message.stage_extra = "";
                    }
                    else
                    {
                        message.stage_extra += "\n";
                    }
                    if (message.stage_detail == null)
                    {
                        message.stage_detail = "";
                    }

                    List<Object> line = new ArrayList<>();
                    line.add(getHashTag(message));
                    for (Object text: message.texts)
                    {
                        if (text instanceof ITextComponent)
                        {
                            line.add(text);
                        }
                        else if (text instanceof String)
                        {
                            line.add(text + " ");
                        }
                    }
                    line.add(getStage(message));
                    ret.add(Messenger.c(line.toArray(new Object[0])));
                }
            }
            return ret;
        }

        private void clearMessages()
        {
            this.messages.clear();
        }

        private static class MicroTickLogHelperMessage
        {
            int dimensionID;
            BlockPos pos;
            EnumDyeColor color;
            String stage, stage_detail, stage_extra;
            Object [] texts;

            private MicroTickLogHelperMessage(int dimensionID, BlockPos pos, EnumDyeColor color, Object [] texts)
            {
                this.dimensionID = dimensionID;
                this.pos = pos.toImmutable();
                this.color = color;
                this.texts = texts;
                this.stage = this.stage_detail = this.stage_extra = null;
            }

            public boolean equals(Object obj)
            {
                if (this == obj)
                {
                    return true;
                }
                if (!(obj instanceof MicroTickLogHelperMessage))
                {
                    return false;
                }

                MicroTickLogHelperMessage o = (MicroTickLogHelperMessage) obj;
                boolean ret = this.dimensionID == o.dimensionID && this.pos.equals(o.pos) && this.color.equals(o.color) && this.stage.equals(o.stage);
                ret |= this.texts.length == o.texts.length;
                if (!ret)
                {
                    return ret;
                }
                for (int i = 0; i < this.texts.length; i++)
                    if (this.texts[i] instanceof String && !this.texts[i].equals(o.texts[i]))
                        return false;
                return ret;
            }

            public int hashCode()
            {
                return dimensionID + pos.hashCode() * 2 + color.hashCode();
            }
        }
    }
}
