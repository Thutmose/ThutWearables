package thut.wearables.inventory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import thut.wearables.CompatWrapper;
import thut.wearables.EnumWearable;

public class PlayerWearables
        implements IWearableInventory, IItemHandlerModifiable, ICapabilitySerializable<NBTTagCompound>
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
            return ItemStack.EMPTY;
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
                    setStack(i, ItemStack.EMPTY);
                    return stack;
                }
            return ItemStack.EMPTY;
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
                    setStack(n, new ItemStack(tag1));
                }
            }
        }

        public ItemStack removeStack(int subIndex)
        {
            if (CompatWrapper.isValid(slots.get(subIndex)))
            {
                ItemStack stack = slots.get(subIndex);
                setStack(subIndex, ItemStack.EMPTY);
                return stack;
            }
            return ItemStack.EMPTY;
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

    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        for (EnumWearable slot : slots.keySet())
        {
            NBTTagCompound compound = slots.get(slot).saveToNBT();
            tag.setTag(slot.ordinal() + "", compound);
        }
        return tag;
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
    public ItemStack getStackInSlot(int index)
    {
        return slots.get(EnumWearable.getWearable(index)).getStack(EnumWearable.getSubIndex(index));
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
        if (simulate) return ItemStack.EMPTY;
        setStackInSlot(slot, stack);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (!CompatWrapper.isValid(getStackInSlot(slot))) return ItemStack.EMPTY;
        if (simulate) return amount > 0 ? getStackInSlot(slot) : ItemStack.EMPTY;
        return slots.get(EnumWearable.getWearable(slot)).removeStack(EnumWearable.getSubIndex(slot));
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return 1;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        slots.get(EnumWearable.getWearable(slot)).setStack(EnumWearable.getSubIndex(slot), stack);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == WearableHandler.WEARABLES_CAP;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (hasCapability(capability, facing)) return WearableHandler.WEARABLES_CAP.cast(this);
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        readFromNBT(nbt );
    }

}
