package thut.wearables.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import thut.wearables.EnumWearable;

public class InventoryWrapper implements IInventory
{
    final PlayerWearables wearable;

    public InventoryWrapper(PlayerWearables inventoryIn)
    {
        this.wearable = inventoryIn;
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
        return new TranslationTextComponent("pokecube.wearables");
    }

    @Override
    public int getSizeInventory()
    {
        return 13;
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        return removeStackFromSlot(index);
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return wearable.extractItem(index, 1, false);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        wearable.setStackInSlot(index, stack);
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
    public boolean isUsableByPlayer(PlayerEntity player)
    {
        return true;
    }

    @Override
    public void openInventory(PlayerEntity player)
    {
    }

    @Override
    public void closeInventory(PlayerEntity player)
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

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return wearable.getStackInSlot(index);
    }
}
