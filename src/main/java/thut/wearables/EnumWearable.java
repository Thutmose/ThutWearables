package thut.wearables;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public enum EnumWearable
{

    FINGER(2, 0), WRIST(2, 2), ANKLE(2, 4), NECK(6), BACK(7), WAIST(8), EAR(2, 9), EYE(11), HAT(12);

    public final int             slots;
    public final int             index;
    static EnumWearable[]        BYINDEX  = new EnumWearable[13];
    static Set<IWearableChecker> checkers = Sets.newHashSet();
    static
    {
        BYINDEX[0] = FINGER;
        BYINDEX[1] = FINGER;
        BYINDEX[2] = WRIST;
        BYINDEX[3] = WRIST;
        BYINDEX[4] = ANKLE;
        BYINDEX[5] = ANKLE;
        BYINDEX[6] = NECK;
        BYINDEX[7] = BACK;
        BYINDEX[8] = WAIST;
        BYINDEX[9] = EAR;
        BYINDEX[10] = EAR;
        BYINDEX[11] = EYE;
        BYINDEX[12] = HAT;

        checkers.add(new IWearableChecker()
        {
            @Override
            public EnumWearable getSlot(ItemStack stack)
            {
                if (stack == null) return null;
                IActiveWearable wearable;
                if ((wearable = stack.getCapability(IActiveWearable.WEARABLE_CAP, null)) != null)
                    return wearable.getSlot(stack);
                if (stack.getItem() instanceof IWearable) { return ((IWearable) stack.getItem()).getSlot(stack); }
                return null;
            }

            @Override
            public void onPutOn(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
            {
                if (itemstack == null) return;
                IActiveWearable wearable;
                if ((wearable = itemstack.getCapability(IActiveWearable.WEARABLE_CAP, null)) != null)
                {
                    wearable.onPutOn(player, itemstack, slot, subIndex);
                    return;
                }
                if (itemstack.getItem() instanceof IActiveWearable)
                    ((IActiveWearable) itemstack.getItem()).onPutOn(player, itemstack, slot, subIndex);
            }

            @Override
            public void onTakeOff(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
            {
                if (itemstack == null) return;
                IActiveWearable wearable;
                if ((wearable = itemstack.getCapability(IActiveWearable.WEARABLE_CAP, null)) != null)
                {
                    wearable.onTakeOff(player, itemstack, slot, subIndex);
                    return;
                }
                if (itemstack.getItem() instanceof IActiveWearable)
                    ((IActiveWearable) itemstack.getItem()).onTakeOff(player, itemstack, slot, subIndex);
            }

            @Override
            public void onUpdate(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
            {
                if (itemstack == null) return;
                IActiveWearable wearable;
                if ((wearable = itemstack.getCapability(IActiveWearable.WEARABLE_CAP, null)) != null)
                {
                    wearable.onUpdate(player, itemstack, slot, subIndex);
                }
                if (itemstack.getItem() instanceof IActiveWearable)
                    ((IActiveWearable) itemstack.getItem()).onUpdate(player, itemstack, slot, subIndex);
                else if (player instanceof EntityPlayer)
                    itemstack.getItem().onArmorTick(player.world, (EntityPlayer) player, itemstack);
                else itemstack.getItem().onUpdate(itemstack, player.world, player, slot.index + subIndex, false);
            }

            @Override
            public void onInteract(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
            {
                if (itemstack != null && player instanceof EntityPlayer)
                {
                    CompatWrapper.rightClickWith(itemstack, (EntityPlayer) player, EnumHand.MAIN_HAND);
                }
            }

            @Override
            public boolean canRemove(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
            {
                if (itemstack == null) return true;
                IActiveWearable wearable;
                if ((wearable = itemstack.getCapability(IActiveWearable.WEARABLE_CAP, null)) != null) { return wearable
                        .canRemove(player, itemstack, slot, subIndex); }
                if (itemstack.getItem() instanceof IActiveWearable)
                    return ((IActiveWearable) itemstack.getItem()).canRemove(player, itemstack, slot, subIndex);
                return true;
            }
        });
    }

    private EnumWearable(int index)
    {
        this.index = index;
        this.slots = 1;
    }

    private EnumWearable(int slots, int index)
    {
        this.index = index;
        this.slots = slots;
    }

    public static EnumWearable getWearable(int index)
    {
        return BYINDEX[index];
    }

    public static int getSubIndex(int index)
    {
        return index - BYINDEX[index].index;
    }

    public static void registerWearableChecker(IWearableChecker checker)
    {
        checkers.add(checker);
    }

    public static EnumWearable getSlot(ItemStack item)
    {
        if (item == null || item.getItem() == null) return null;
        for (IWearableChecker checker : checkers)
        {
            EnumWearable ret = checker.getSlot(item);
            if (ret != null) return ret;
        }
        return null;
    }

    public static void interact(EntityPlayer player, ItemStack item, int index)
    {
        if (item == null) return;
        EnumWearable slot = getWearable(index);
        int subIndex = getSubIndex(index);
        for (IWearableChecker checker : checkers)
        {
            checker.onInteract(player, item, slot, subIndex);
        }
    }

    public static void putOn(EntityLivingBase wearer, ItemStack stack, int index)
    {
        if (stack == null) return;
        EnumWearable slot = getWearable(index);
        int subIndex = getSubIndex(index);
        for (IWearableChecker checker : checkers)
        {
            checker.onPutOn(wearer, stack, slot, subIndex);
        }
    }

    public static void takeOff(EntityLivingBase wearer, ItemStack stack, int index)
    {
        if (stack == null) return;
        EnumWearable slot = getWearable(index);
        int subIndex = getSubIndex(index);
        for (IWearableChecker checker : checkers)
        {
            checker.onTakeOff(wearer, stack, slot, subIndex);
        }
    }

    public static void tick(EntityLivingBase wearer, ItemStack stack, int index)
    {
        if (stack == null) return;
        EnumWearable slot = getWearable(index);
        int subIndex = getSubIndex(index);
        for (IWearableChecker checker : checkers)
        {
            checker.onUpdate(wearer, stack, slot, subIndex);
        }
    }

    public static boolean canTakeOff(EntityLivingBase wearer, ItemStack stack, int index)
    {
        if (stack == null) return true;
        EnumWearable slot = getWearable(index);
        int subIndex = getSubIndex(index);
        for (IWearableChecker checker : checkers)
        {
            if (!checker.canRemove(wearer, stack, slot, subIndex)) return false;
        }
        return true;
    }
}
