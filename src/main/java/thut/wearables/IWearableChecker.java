package thut.wearables;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface IWearableChecker
{
    EnumWearable getSlot(ItemStack stack);

    public void onPutOn(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex);

    public void onTakeOff(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex);

    public void onUpdate(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex);

    public void onInteract(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex);

    public boolean canRemove(LivingEntity player, ItemStack itemstack, EnumWearable slot, int subIndex);
}
