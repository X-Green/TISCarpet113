package carpet.logging.logHelpers;

import carpet.logging.LoggerRegistry;
import carpet.settings.CarpetSettings;
import carpet.utils.Messenger;
import carpet.utils.WoolTool;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.*;
import net.minecraft.block.state.BlockState;
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
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.*;

public class MicroTickLogHelper
{
    public static MicroTickLogger logger = new MicroTickLogger();
    private static String stage;
    private static String stage_detail;
    private static String stage_extra;
    private static String[] PistonBlockEventName = new String[]{"Push", "Retract", "Drop"};
    private static Set<BlockPos> pistonBlockEventSuccessPosition = Sets.newHashSet();

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

    public static boolean loggerActivated()
    {
        return CarpetSettings.microTick && LoggerRegistry.__microtick;
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

    private static String getColorStyle(EnumDyeColor color)
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

    private static String getBooleanColor(boolean bool)
    {
        return bool ? "e" : "r";
    }

    public static String getUpdateOrderList(EnumFacing[] UPDATE_ORDER, EnumFacing skipSide)
    {
        int counter = 0;
        String updateOrder = "";
        for (EnumFacing enumfacing : UPDATE_ORDER)
        {
            if (skipSide != enumfacing)
            {
                if (counter > 0)
                {
                    updateOrder += '\n';
                }
                updateOrder += String.format("%d. %s", (++counter), enumfacing);
            }
        }
        return updateOrder;
    }
    public static String getUpdateOrderList(EnumFacing[] UPDATE_ORDER)
    {
        return getUpdateOrderList(UPDATE_ORDER, null);
    }

    private static ITextComponent getTranslatedName(Block block)
    {
        ITextComponent name = new TextComponentTranslation(block.getTranslationKey());
        name.getStyle().setColor(TextFormatting.WHITE);
        return name;
    }

    private static EnumDyeColor getWoolColor(World worldIn, BlockPos pos)
    {
        if (!loggerActivated())
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

    // called before an action is executed or done

    public static void onBlockUpdate(World worldIn, BlockPos pos, Block fromBlock, int actionType, String updateType, String updateType_extra)
    {
        if (!loggerActivated())
        {
            return;
        }
        String[] actions = new String[] {"Emitting", "Finished"};
        for (EnumFacing facing: EnumFacing.values())
        {
            BlockPos blockEndRodPos = pos.offset(facing);
            IBlockState iBlockState = worldIn.getBlockState(blockEndRodPos);
            if (iBlockState.getBlock() == Blocks.END_ROD && iBlockState.get(BlockStateProperties.FACING).getOpposite() == facing)
            {
                EnumDyeColor color = getWoolColor(worldIn, blockEndRodPos);
                if (color != null)
                {
                    logger.addMessage(color, pos, worldIn, new Object[]{
                            getTranslatedName(fromBlock),
                            String.format("q  %s", actions[actionType]),
                            String.format("c  %s", updateType),
                            String.format("^w %s", updateType_extra)
                    });
                }
            }
        }
    }
    public static void onBlockUpdate(World worldIn, BlockPos pos, Block fromBlock, int actionType, String updateType)
    {
        onBlockUpdate(worldIn, pos, fromBlock, actionType, updateType, "");
    }
    public static void onBlockUpdate(World worldIn, BlockPos pos, int stage, String updateType, String updateType_extra)
    {
        if (loggerActivated())
        {
            onBlockUpdate(worldIn, pos, worldIn.getBlockState(pos).getBlock(), stage, updateType, updateType_extra);
        }
    }
    public static void onBlockUpdate(World worldIn, BlockPos pos, int stage, String updateType)
    {
         onBlockUpdate(worldIn, pos, worldIn.getBlockState(pos).getBlock(), stage, updateType, "");
    }

    // called after an action is done

    public static void onComponentAddToTileTickList(World worldIn, BlockPos pos, int delay, TickPriority priority)
    {
        if (!loggerActivated())
        {
            return;
        }
        EnumDyeColor color = getWoolColor(worldIn, pos);
        if (color != null)
        {
            logger.addMessage(color, pos, worldIn, new Object[]{
                    getTranslatedName(worldIn.getBlockState(pos).getBlock()),
                    "q  Scheduled",
                    "c  TileTick",
                    String.format("^w Delay: %dgt\nPriority: %d (%s)", delay, priority.getPriority(), priority)
            });
        }
    }
    public static void onComponentAddToTileTickList(World worldIn, BlockPos pos, int delay)
    {
        onComponentAddToTileTickList(worldIn, pos, delay, TickPriority.NORMAL);
    }

    private static String getBlockEventMessageExtra(int eventID, int eventParam, String [] names)
    {
        return String.format("^w eventID: %d (%s)\neventParam: %d (%s)",
                eventID, names[eventID], eventParam, EnumFacing.byIndex(eventParam));
    }

    public static void onPistonAddBlockEvent(World worldIn, BlockPos pos, int eventID, int eventParam)
    {
        if (!loggerActivated())
        {
            return;
        }
        if (eventID < 0 || eventID > 2)
        {
            return;
        }
        EnumDyeColor color = getWoolColor(worldIn, pos);
        if (color != null)
        {
            logger.addMessage(color, pos, worldIn, new Object[]{
                    getTranslatedName(worldIn.getBlockState(pos).getBlock()),
                    "q  Scheduled",
                    "c  BlockEvent",
                    getBlockEventMessageExtra(eventID, eventParam, PistonBlockEventName)
            });
        }
    }

    public static void onPistonExecuteBlockEvent(World worldIn, BlockPos pos, Block block, int eventID, int eventParam, boolean success) // "block" only overwrites displayed name
    {
        if (!loggerActivated())
        {
            return;
        }
        EnumDyeColor color = getWoolColor(worldIn, pos);
        if (color != null)
        {
            if (success)
            {
                pistonBlockEventSuccessPosition.add(pos);
            }
            else if (pistonBlockEventSuccessPosition.contains(pos)) // ignore failure after a success blockevent of piston in the same gt
            {
                return;
            }
            logger.addMessage(color, pos, worldIn, new Object[]{
                    getTranslatedName(block),
                    "q  Executed",
                    String.format("c  %s", PistonBlockEventName[eventID]),
                    getBlockEventMessageExtra(eventID, eventParam, PistonBlockEventName),
                    String.format("%s  %s", getBooleanColor(success), success ? "Succeed" : "Failed")
            });
        }
    }

    public static void onComponentPowered(World worldIn, BlockPos pos, boolean poweredState)
    {
        if (!loggerActivated())
        {
            return;
        }
        EnumDyeColor color = getWoolColor(worldIn, pos);
        if (color != null)
        {
            logger.addMessage(color, pos, worldIn, new Object[]{
                    getTranslatedName(worldIn.getBlockState(pos).getBlock()),
                    String.format("c  %s", poweredState ? "Powered" : "Depowered")
            });
        }
    }

    public static void onRedstoneTorchLit(World worldIn, BlockPos pos, boolean litState)
    {
        if (!loggerActivated())
        {
            return;
        }
        EnumDyeColor color = getWoolColor(worldIn, pos);
        if (color != null)
        {
            logger.addMessage(color, pos, worldIn, new Object[]{
                    getTranslatedName(worldIn.getBlockState(pos).getBlock()),
                    String.format("c  %s", litState ? "Lit" : "Unlit")
            });
        }
    }

    public static void flushMessages(long gameTime) // needs to call at the end of a gt
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
        pistonBlockEventSuccessPosition.clear();
    }

    public static class MicroTickLogger
    {
        private List<MicroTickLogHelperMessage> messages = Lists.newArrayList();

        // #(color, pos) texts[] at stage(detail, extra, dimension)
        public int addMessage(EnumDyeColor color, BlockPos pos, int dimensionID, Object [] texts)
        {
            MicroTickLogHelperMessage message = new MicroTickLogHelperMessage(dimensionID, pos, color, texts);
            message.stage = MicroTickLogHelper.getTickStage();
            message.stage_detail = MicroTickLogHelper.getTickStageDetail();
            message.stage_extra = MicroTickLogHelper.getTickStageExtra();
            this.messages.add(message);
            return this.messages.size();
        }
        public int addMessage(EnumDyeColor color, BlockPos pos, World worldIn, Object [] texts)
        {
            return addMessage(color, pos, worldIn.getDimension().getType().getId(), texts);
        }

        private static ITextComponent getHashTag(MicroTickLogHelperMessage msg)
        {
            String text = getColorStyle(msg.color) + " # ";
            ITextComponent ret;
            if (msg.pos != null)
            {
                ret = Messenger.c(
                        text,
                        String.format("!/execute in %s run tp @s %d %d %d", getDimensionCommand(msg.dimensionID), msg.pos.getX(), msg.pos.getY(), msg.pos.getZ()),
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
            ret.add(Messenger.s(""));
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
                        if (text instanceof ITextComponent || text instanceof String)
                        {
                            line.add(text);
                        }
                    }
                    line.add("w  ");
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
