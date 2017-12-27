package thut.wearables;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface IActiveWearable extends IWearable
{
    @CapabilityInject(IActiveWearable.class)
    public static final Capability<IActiveWearable> WEARABLE_CAP = null;

    public void onPutOn(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex);

    public void onTakeOff(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex);

    public void onUpdate(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex);

    public static class Default implements IActiveWearable
    {

        @Override
        public EnumWearable getSlot(ItemStack stack)
        {
            return null;
        }

        @Override
        public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
        {
        }

        @Override
        public void onPutOn(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
        }

        @Override
        public void onTakeOff(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
        }

        @Override
        public void onUpdate(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
        }

    }
}
