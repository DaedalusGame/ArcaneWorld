package party.lemons.arcaneworld.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import party.lemons.arcaneworld.config.ArcaneWorldConfig;
import party.lemons.arcaneworld.gen.dungeon.dimension.TeleporterDungeonReturn;
import party.lemons.arcaneworld.util.capabilities.RitualCoordinateProvider;
import party.lemons.lemonlib.item.IItemModel;

import java.util.Random;

/**
 * Created by Sam on 22/09/2018.
 */
public class BlockReturnPortal extends BlockPortal implements IItemModel
{
    public BlockReturnPortal()
    {
        super();
    }
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote)
        {
            worldIn.setBlockState(pos, state.withProperty(AXIS, EnumFacing.Axis.Z));
        }

        return true;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {

    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
    }

    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entity)
    {
        if (worldIn.provider.getDimension() != ArcaneWorldConfig.DUNGEONS.DIM_ID)
            return;

        if (!worldIn.isRemote && entity instanceof EntityPlayer)
        {
            entity.timeUntilPortal = entity.getPortalCooldown();
            int returnDim = entity.getCapability(RitualCoordinateProvider.RITUAL_COORDINATE_CAPABILITY,null).getDim();
            entity.changeDimension(returnDim, new TeleporterDungeonReturn((WorldServer) worldIn));
            return;
        }
    }
}
