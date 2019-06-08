package thut.wearables;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IWearable
{
    EnumWearable getSlot(ItemStack stack);

    @SideOnly(Side.CLIENT)
    /** This is called after doing the main transforms needed to get the gl
     * calls to the correct spot.
     * 
     * @param wearer
     *            - The entity wearing the stack
     * @param stack
     *            - The stack being worn */
    void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks);

    @SideOnly(Side.CLIENT)
    /** Does this wearable handle the render offsets by itself?
     * 
     * @return */
    default boolean customOffsets()
    {
        return false;
    }

    default boolean canRemove(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
        return true;
    }

    default boolean canPutOn(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
        return true;
    }

    default boolean dyeable(ItemStack stack)
    {
        return false;
    }
}
