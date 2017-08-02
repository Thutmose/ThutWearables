package thut.wearables.inventory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandlerModifiable;
import thut.wearables.CompatWrapper;
import thut.wearables.EnumWearable;

public class PlayerWearables implements IWearableInventory, IInventory, IItemHandlerModifiable
{
    private static class WearableSlot
    {
        final EnumWearable    type;
        final List<ItemStack> slots;

        WearableSlot(EnumWearable type)
        {
            this.type = type;
            this.slots = CompatWrapper.makeList(type.slots);
        }

        ItemStack getStack(int slot)
        {
            return slots.get(slot);
        }

        ItemStack getStack()
        {
            for (int i = 0; i < slots.size(); i++)
                if (CompatWrapper.isValid(slots.get(i))) return slots.get(i);
            return CompatWrapper.nullStack;
        }

        void setStack(int slot, ItemStack stack)
        {
            slots.set(slot, stack);
        }

        ItemStack removeStack()
        {
            for (int i = 0; i < slots.size(); i++)
                if (CompatWrapper.isValid(slots.get(i)))
                {
                    ItemStack stack = getStack(i);
                    setStack(i, CompatWrapper.nullStack);
                    return stack;
                }
            return null;
        }

        boolean addStack(ItemStack stack)
        {
            for (int i = 0; i < slots.size(); i++)
                if (!CompatWrapper.isValid(slots.get(i)))
                {
                    setStack(i, stack);
                    return true;
                }
            return false;
        }

        NBTTagCompound saveToNBT()
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("type", (byte) type.ordinal());
            for (int n = 0; n < slots.size(); n++)
            {
                ItemStack i = getStack(n);
                if (CompatWrapper.isValid(i))
                {
                    NBTTagCompound tag1 = new NBTTagCompound();
                    i.writeToNBT(tag1);
                    tag.setTag("slot" + n, tag1);
                }
            }
            return tag;
        }

        void loadFromNBT(NBTTagCompound tag)
        {
            for (int n = 0; n < slots.size(); n++)
            {
                NBTBase temp = tag.getTag("slot" + n);
                if (temp instanceof NBTTagCompound)
                {
                    NBTTagCompound tag1 = (NBTTagCompound) temp;
                    setStack(n, CompatWrapper.fromTag(tag1));
                }
            }
        }

        public ItemStack removeStack(int subIndex)
        {
            if (CompatWrapper.isValid(slots.get(subIndex)))
            {
                ItemStack stack = slots.get(subIndex);
                setStack(subIndex, CompatWrapper.nullStack);
                return stack;
            }
            return CompatWrapper.nullStack;
        }
    }

    private Map<EnumWearable, WearableSlot> slots = Maps.newHashMap();

    public PlayerWearables()
    {
        for (EnumWearable type : EnumWearable.values())
            slots.put(type, new WearableSlot(type));
    }

    @Override
    public ItemStack getWearable(EnumWearable type, int slot)
    {
        return slots.get(type).getStack(slot);
    }

    @Override
    public ItemStack getWearable(EnumWearable type)
    {
        return slots.get(type).getStack();
    }

    @Override
    public Set<ItemStack> getWearables()
    {
        Set<ItemStack> ret = Sets.newHashSet();
        for (WearableSlot slot : slots.values())
        {
            for (int i = 0; i < slot.slots.size(); i++)
            {
                if (CompatWrapper.isValid(slot.slots.get(i))) ret.add(slot.slots.get(i));
            }
        }
        return ret;
    }

    @Override
    public boolean setWearable(EnumWearable type, ItemStack stack, int slot)
    {
        WearableSlot wSlot = slots.get(type);
        if (stack == null)
        {
            if (wSlot.getStack(slot) == null) return false;
            wSlot.setStack(slot, stack);
            return true;
        }
        if (wSlot.getStack(slot) != null) return false;
        wSlot.setStack(slot, stack);
        return true;
    }

    @Override
    public boolean setWearable(EnumWearable type, ItemStack stack)
    {
        WearableSlot wSlot = slots.get(type);
        if (stack == null)
        {
            if (wSlot.getStack() == null) return false;
            wSlot.removeStack();
            return true;
        }
        return wSlot.addStack(stack);
    }

    public String dataFileName()
    {
        return "wearables";
    }

    public void writeToNBT(NBTTagCompound tag)
    {
        for (EnumWearable slot : slots.keySet())
        {
            NBTTagCompound compound = slots.get(slot).saveToNBT();
            tag.setTag(slot.ordinal() + "", compound);
        }
    }

    public void readFromNBT(NBTTagCompound tag)
    {
        for (EnumWearable type : EnumWearable.values())
            slots.put(type, new WearableSlot(type));
        for (EnumWearable slot : slots.keySet())
        {
            NBTTagCompound compound = tag.getCompoundTag(slot.ordinal() + "");
            slots.get(slot).loadFromNBT(compound);
        }
    }

    @Override
    public String getName()
    {
        return "wearables";
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentTranslation("pokecube.wearables");
    }

    @Override
    public int getSizeInventory()
    {
        return 13;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return slots.get(EnumWearable.getWearable(index)).getStack(EnumWearable.getSubIndex(index));
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        return removeStackFromSlot(index);
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return slots.get(EnumWearable.getWearable(index)).removeStack(EnumWearable.getSubIndex(index));
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        slots.get(EnumWearable.getWearable(index)).setStack(EnumWearable.getSubIndex(index), stack);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public void markDirty()
    {
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return EnumWearable.getSlot(stack) == EnumWearable.getWearable(index);
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {
    }

    // TODO find out what this is.
    public boolean func_191420_l()
    {
        return false;
    }

    @Override
    public int getSlots()
    {
        return 13;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (CompatWrapper.isValid(getStackInSlot(slot))) return stack;
        if (simulate) return CompatWrapper.nullStack;
        setStackInSlot(slot, stack);
        return CompatWrapper.nullStack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (!CompatWrapper.isValid(getStackInSlot(slot))) return CompatWrapper.nullStack;
        if (simulate) return amount > 0 ? getStackInSlot(slot) : CompatWrapper.nullStack;
        return removeStackFromSlot(slot);
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return 1;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        this.setInventorySlotContents(slot, stack);
    }

}
