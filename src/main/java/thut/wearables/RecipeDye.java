package thut.wearables;

import java.util.List;
import java.util.Locale;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeDye implements IRecipe
{
    private ItemStack toRemove = CompatWrapper.nullStack;
    private ItemStack output   = CompatWrapper.nullStack;

    ResourceLocation  registryName;

    @Override
    public IRecipe setRegistryName(ResourceLocation name)
    {
        registryName = name;
        return this;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    @Override
    public Class<IRecipe> getRegistryType()
    {
        return IRecipe.class;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        output = CompatWrapper.nullStack;
        toRemove = CompatWrapper.nullStack;
        boolean wearable = false;
        boolean dye = false;
        ItemStack dyeStack = CompatWrapper.nullStack;
        ItemStack worn = CompatWrapper.nullStack;
        int n = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (CompatWrapper.isValid(stack))
            {
                n++;
                // TODO move dye code over to wearables itself.
                IWearable wear = stack.getCapability(IActiveWearable.WEARABLE_CAP, null);
                if (wear == null && stack.getItem() instanceof IWearable) wear = (IWearable) stack.getItem();
                if (wear != null && wear.dyeable(stack))
                {
                    if (wearable) return false;
                    wearable = true;
                    worn = stack;
                    continue;
                }
                List<ItemStack> dyes = CompatWrapper.getOres("dye");
                boolean isDye = false;
                for (ItemStack dye1 : dyes)
                {
                    if (OreDictionary.itemMatches(dye1, stack, false))
                    {
                        isDye = true;
                        break;
                    }
                }
                if (isDye)
                {
                    if (dye) return false;
                    dye = true;
                    dyeStack = stack;
                    continue;
                }
                return false;
            }
        }
        if (n > 2 || !wearable) return false;
        if (dye)
        {
            output = worn.copy();
            if (!output.hasTagCompound()) output.setTagCompound(new NBTTagCompound());
            int[] ids = OreDictionary.getOreIDs(dyeStack);
            int colour = dyeStack.getItemDamage();
            for (int i : ids)
            {
                String name = OreDictionary.getOreName(i);
                if (name.startsWith("dye") && name.length() > 3)
                {
                    String val = name.replace("dye", "").toUpperCase(Locale.ENGLISH);
                    try
                    {
                        EnumDyeColor type = EnumDyeColor.valueOf(val);
                        colour = type.getDyeDamage();
                        break;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            output.getTagCompound().setInteger("dyeColour", colour);
        }
        else
        {
            output = CompatWrapper.nullStack;
        }
        return CompatWrapper.isValid(output);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
    {
        NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack> withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            ItemStack itemstack = inv.getStackInSlot(i);
            nonnulllist.set(i, toKeep(i, itemstack, inv));
        }
        return nonnulllist;
    }

    public ItemStack toKeep(int slot, ItemStack stackIn, InventoryCrafting inv)
    {
        ItemStack stack = net.minecraftforge.common.ForgeHooks.getContainerItem(stackIn);
        if (!CompatWrapper.isValid(stack) && CompatWrapper.isValid(toRemove))
        {
            stack = toRemove;
            toRemove = CompatWrapper.nullStack;
        }
        return stack;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        return output;
    }

    @Override
    public boolean canFit(int width, int height)
    {
        return true;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return output;
    }

}
