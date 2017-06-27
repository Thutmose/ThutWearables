package thut.wearables.impl;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;

public class DefaultActiveWearable implements IActiveWearable
{

    public DefaultActiveWearable()
    {
    }

    @Override
    public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
    {
    }

    @Override
    public EnumWearable getSlot(ItemStack stack)
    {
        return null;
    }

    @Override
    public void onUpdate(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
    }

    @Override
    public void onTakeOff(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
    }

    @Override
    public void onPutOn(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
    }

}
