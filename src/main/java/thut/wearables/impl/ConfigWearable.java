package thut.wearables.impl;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;

public class ConfigWearable implements IActiveWearable, ICapabilityProvider
{
    final EnumWearable slot;

    public ConfigWearable(EnumWearable slot)
    {
        this.slot = slot;
    }

    @Override
    public EnumWearable getSlot(ItemStack stack)
    {
        return slot;
    }

    @Override
    public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
    {
        // TODO way to register renderers for config wearables
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == WEARABLE_CAP;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (WEARABLE_CAP != null && capability == WEARABLE_CAP) return (T) this;
        return null;
    }

    @Override
    public void onPutOn(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
        // TODO way to register an IActiveWearable for this to slave to.
    }

    @Override
    public void onTakeOff(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdate(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
        // TODO Auto-generated method stub

    }

}
