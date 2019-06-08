package thut.wearables;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface IActiveWearable extends IWearable
{
    @CapabilityInject(IActiveWearable.class)
    public static final Capability<IActiveWearable> WEARABLE_CAP = null;

    default void onPutOn(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
    }

    default void onTakeOff(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
    }

    default void onUpdate(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
    }

    public static class Default implements IActiveWearable
    {

        @Override
        public EnumWearable getSlot(ItemStack stack)
        {
            return null;
        }

        @Override
        public void renderWearable(EnumWearable slot, LivingEntity wearer, ItemStack stack, float partialTicks)
        {
        }

        @Override
        public void onPutOn(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
        }

        @Override
        public void onTakeOff(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
        }

        @Override
        public void onUpdate(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
        }

    }
}
