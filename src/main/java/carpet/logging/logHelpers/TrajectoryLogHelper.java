package carpet.logging.logHelpers;

import carpet.logging.Logger;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic log helper for logging the trajectory of things like blocks and throwables.
 */
public class TrajectoryLogHelper
{
    private static final int MAX_TICKS_PER_LINE = 20;

    private boolean doLog;
    private Logger logger;

    private ArrayList<Vec3d> positions = new ArrayList<>();
    private ArrayList<Vec3d> motions = new ArrayList<>();
    private ArrayList<String> collide = new ArrayList<>();
    private String hitType;
    private World world;

    public TrajectoryLogHelper(String logName)
    {
        this.logger = LoggerRegistry.getLogger(logName);
        this.doLog = this.logger.hasOnlineSubscribers();
    }

    public void onTick(double x, double y, double z, double motionX, double motionY, double motionZ)
    {
        if (!doLog) return;
        positions.add(new Vec3d(x, y, z));
        motions.add(new Vec3d(motionX, motionY, motionZ));
        collide.add("f");
    }

    public void onCollide(double x, double y, double z, String type, World worldIn)
    {
        if (!doLog) return;
        positions.add(new Vec3d(x, y, z));
        motions.add(new Vec3d(0,0,0));
        collide.add("t");
        hitType = type;
        world = worldIn;

        for (int i = 0; i < worldIn.loadedEntityList.size(); ++i){
            Entity checkentity = worldIn.loadedEntityList.get(i);
            if (checkentity.getTags().contains("TISCM_VISPROJ_LOGGER")){
                worldIn.removeEntity(checkentity);
            }
        }
    }

    public void onFinish()
    {
        if (!doLog) return;
        logger.log( (option) -> {
            List<ITextComponent> comp = new ArrayList<>();
            switch (option)
            {
                case "brief":
                    comp.add(Messenger.s(""));
                    List<String> line = new ArrayList<>();

                    for (int i = 0; i < positions.size(); i++)
                    {
                        Vec3d pos = positions.get(i);
                        Vec3d mot = motions.get(i);
                        String coll = collide.get(i);
                        line.add("w  x");
                        if (coll != "t") {
                            line.add(String.format("^w Tick: %d\nx: %f\ny: %f\nz: %f\n------------\nmx: %f\nmy: %f\nmz: %f",
                                    i, pos.x, pos.y, pos.z, mot.x, mot.y, mot.z));
                        }
                        else {
                            line.add(String.format("^w Hit:\nx: %f\ny: %f\nz: %f\n------------\nHit on: %s",
                                    pos.x, pos.y, pos.z, this.hitType));
                        }
                        if ((((i+1) % MAX_TICKS_PER_LINE)==0) || i == positions.size()-1)
                        {
                            comp.add(Messenger.c(line.toArray(new Object[0])));
                            line.clear();
                        }
                    }
                    break;
                case "full":
                    comp.add(Messenger.c("w ---------"));
                    for (int i = 0; i < positions.size(); i++)
                    {
                        Vec3d pos = positions.get(i);
                        Vec3d mot = motions.get(i);
                        comp.add(Messenger.c(
                                String.format("w tick: %3d pos",i),Messenger.dblt("w",pos.x, pos.y, pos.z),
                                "w   mot",Messenger.dblt("w",mot.x, mot.y, mot.z)));
                    }
                    break;
                case "visualize":
                    comp.add(Messenger.c("w visualize logger: visualized " + (positions.size()-1) + " tick(s)"));
                    for (int i = 0; i < positions.size(); i++)
                    {
                        Vec3d pos = positions.get(i);
                        EntitySnowball visEntity = new EntitySnowball(world, pos.x, pos.y, pos.z);
                        visEntity.setNoGravity(true);
                        if (i < positions.size() - 1) {
                            visEntity.setCustomName(new TextComponentString(i + ""));
                        }
                        else {
                            visEntity.setCustomName(new TextComponentString("Hit"));
                        }
                        visEntity.setCustomNameVisible(true);
                        visEntity.addTag("TISCM_VISPROJ_LOGGER");
                        visEntity.logHelper = null;
                        world.spawnEntity(visEntity);
                    }
                    break;
            }
            return comp.toArray(new ITextComponent[0]);
        });
        doLog = false;
    }
}

