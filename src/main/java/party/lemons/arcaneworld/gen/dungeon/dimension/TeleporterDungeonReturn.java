package party.lemons.arcaneworld.gen.dungeon.dimension;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * Created by Sam on 22/09/2018.
 */
public class TeleporterDungeonReturn extends Teleporter
{

    public TeleporterDungeonReturn(WorldServer worldIn)
    {
        super(worldIn);
    }

    @Override
    public void placeEntity(World world, Entity entity, float yaw)
    {
        if(world.isRemote)
            return;

        BlockPos returnPos = world.getSpawnPoint();
        if(entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entity;
            boolean hasBed = true;

            BlockPos bedLocation = player.getBedLocation();
            if (bedLocation == null || player.getBedSpawnLocation(world, bedLocation, false) == null)
                hasBed = false;

            if (!hasBed)
            {
                BlockPos blockpos = world.provider.getRandomizedSpawnPoint();
                returnPos = world.getTopSolidOrLiquidBlock(blockpos);
            }
            else
            {
                returnPos = player.getBedSpawnLocation(world, bedLocation, false);
            }
        }

        entity.setLocationAndAngles(returnPos.getX(), returnPos.getY(), returnPos.getZ(), entity.rotationYaw, 0.0F);
        entity.motionX = 0.0D;
        entity.motionY = 0.0D;
        entity.motionZ = 0.0D;
    }

    public void placeInPortal(Entity entity, float rotationYaw)
    {

    }
}
