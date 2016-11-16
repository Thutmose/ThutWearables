package thut.wearables;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;

public class CompatWrapper
{
    public static final ItemStack nullStack = null;

    public static ItemStack fromTag(NBTTagCompound tag)
    {
        return new ItemStack(tag);
    }

    public static ItemStack copy(ItemStack in)
    {
        return in.copy();
    }

    public static ItemStack setStackSize(ItemStack stack, int amount)
    {
        if (amount <= 0) { return nullStack; }
        stack.func_190920_e(amount);
        return stack;
    }

    public static int getStackSize(ItemStack stack)
    {
        return stack.func_190916_E();
    }

    public static boolean isValid(ItemStack stack)
    {
        return !stack.func_190926_b();
    }

    public static ItemStack validate(ItemStack in)
    {
        if (!isValid(in)) return nullStack;
        return in;
    }

    public static int increment(ItemStack in, int amt)
    {
        in.func_190917_f(amt);
        return in.func_190916_E();
    }

    public static List<ItemStack> makeList(int size)
    {
        List<ItemStack> ret = Lists.newArrayList();
        for (int i = 0; i < size; i++)
            ret.add(nullStack);
        return ret;
    }

    public static void rightClickWith(ItemStack stack, EntityPlayer player, EnumHand hand)
    {
        ItemStack old = player.getHeldItem(hand);
        player.setHeldItem(hand, stack);
        stack.getItem().onItemRightClick(player.worldObj, player, hand);
        player.setHeldItem(hand, old);
    }

}
