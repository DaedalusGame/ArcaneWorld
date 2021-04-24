package party.lemons.arcaneworld.item.impl;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import party.lemons.arcaneworld.ArcaneWorld;
import party.lemons.arcaneworld.item.ArcaneWorldItems;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Sam on 19/09/2018.
 */
public class ItemRecaller extends Item
{
    public ItemRecaller()
    {
        this.setMaxDamage(75);
        this.setMaxStackSize(1);
    }

    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = player.getHeldItem(hand);
        if (!recallIf(player, world, stack))
            setPosition(stack, world, pos.offset(facing));

        return EnumActionResult.SUCCESS;
    }

    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);
        return new ActionResult<>(recallIf(player, world, stack) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, stack);
    }

    private boolean recallIf(EntityPlayer player, World world, ItemStack stack) {
        if (getPosition(stack) != null && getDimension(stack) != Integer.MAX_VALUE) {
            recall(player, world, stack);
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        if (getPosition(stack) != null && getDimension(stack) == worldIn.provider.getDimension())
        {
            BlockPos pos = getPosition(stack);
            tooltip.add(TextFormatting.GOLD + "X: " + pos.getX());
            tooltip.add(TextFormatting.GOLD + "Y: " + pos.getY());
            tooltip.add(TextFormatting.GOLD + "Z: " + pos.getZ());
        }
    }

    public void recall(EntityPlayer player, World world, ItemStack stack)
    {
        if (world.isRemote || getDimension(stack) != world.provider.getDimension())
            return;

        BlockPos pos = getPosition(stack);
        ((WorldServer)world).spawnParticle(EnumParticleTypes.PORTAL, player.posX , player.posY + (player.height / 2), player.posZ,40, 0, 0, 0, 1D);
        player.setPositionAndUpdate(pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F);
        ((WorldServer)world).spawnParticle(EnumParticleTypes.PORTAL, player.posX , player.posY + (player.height / 2), player.posZ ,40, 0, 0, 0, 5D);

        stack.damageItem(1, player);
        setPosition(stack, world, null);
        player.getCooldownTracker().setCooldown(this, 25);

    }


    public static BlockPos getPosition(ItemStack stack)
    {
        if (!stack.hasTagCompound())
            return null;

        NBTTagCompound tags = stack.getTagCompound();
        if (tags.hasKey("position"))
            return NBTUtil.getPosFromTag(tags.getCompoundTag("position"));

        return null;
    }

    public static int getDimension(ItemStack stack)
    {
        if (!stack.hasTagCompound())
            return Integer.MAX_VALUE;

        NBTTagCompound tags = stack.getTagCompound();
        if (tags.hasKey("dim"))
            return tags.getInteger("dim");

        return Integer.MAX_VALUE;
    }

    public static void setPosition(ItemStack stack, World world, BlockPos pos)
    {
        if (world.isRemote)
            return;

        NBTTagCompound tags;
        if (!stack.hasTagCompound())
            tags = new NBTTagCompound();
        else
            tags = stack.getTagCompound();

        if (pos == null)
        {
            tags.removeTag("position");
            tags.removeTag("dim");
        }
        else
        {
            tags.setTag("position", NBTUtil.createPosTag(pos));
            tags.setInteger("dim", world.provider.getDimension());
        }

        stack.setTagCompound(tags);
    }

    @Mod.EventBusSubscriber(modid = ArcaneWorld.MODID, value = Side.CLIENT)
    public static class RecallerModelRegister
    {
        @SubscribeEvent
        public static void onRegisterModel(ModelRegistryEvent event)
        {
            ModelResourceLocation recaller_off = new ModelResourceLocation(ArcaneWorldItems.RECALLER.getRegistryName() + "_off", "inventory");
            ModelResourceLocation recaller_on = new ModelResourceLocation(ArcaneWorldItems.RECALLER.getRegistryName() + "_on", "inventory");
            ModelBakery.registerItemVariants(ArcaneWorldItems.RECALLER, recaller_off, recaller_on);
            ModelLoader.setCustomMeshDefinition(ArcaneWorldItems.RECALLER, s ->
            {
                if (s.hasTagCompound() && s.getTagCompound().hasKey("position"))
                    return recaller_on;

                return recaller_off;
            });
        }
    }
}
