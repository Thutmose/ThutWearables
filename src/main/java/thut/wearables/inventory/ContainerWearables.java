package thut.wearables.inventory;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.wearables.CompatWrapper;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;

public class ContainerWearables extends Container
{
    private static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EntityEquipmentSlot[] {
            EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET };

    public static class ArmourInventory extends InventoryBasic
    {
        final EntityLivingBase mob;

        public ArmourInventory(EntityLivingBase mob)
        {
            super("Armour Slots", true, 4);
            this.mob = mob;
            // Field index 2 is the non null list for this inventory.
            ReflectionHelper.setPrivateValue(InventoryBasic.class, this, mob.getArmorInventoryList(), 2);
        }

        @Override
        public ItemStack addItem(ItemStack stack)
        {
            // TODO Auto-generated method stub
            return super.addItem(stack);
        }

        @Override
        public ItemStack decrStackSize(int index, int count)
        {
            // TODO Auto-generated method stub
            return super.decrStackSize(index, count);
        }

        @Override
        public ItemStack getStackInSlot(int index)
        {
            // TODO Auto-generated method stub
            return super.getStackInSlot(index);
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack)
        {
            // TODO Auto-generated method stub
            super.setInventorySlotContents(index, stack);
        }

        @Override
        public ItemStack removeStackFromSlot(int index)
        {
            // TODO Auto-generated method stub
            return super.removeStackFromSlot(index);
        }

        @Override
        public void clear()
        {
            // TODO Auto-generated method stub
            super.clear();
        }
    }

    public static class WornSlot extends Slot
    {
        final EntityLivingBase wearer;
        final EnumWearable     slot;
        final InventoryWrapper slots;
        private boolean        init = false;

        public WornSlot(EntityLivingBase player, InventoryWrapper inventoryIn, int index, int xPosition, int yPosition)
        {
            super(inventoryIn, index, xPosition, yPosition);
            this.slot = EnumWearable.getWearable(index);
            this.slots = inventoryIn;
            this.wearer = player;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public net.minecraft.client.renderer.texture.TextureAtlasSprite getBackgroundSprite()
        {
            if (!init)
            {
                String tex = null;
                switch (slot)
                {
                case ANKLE:
                    tex = ThutWearables.MODID + ":textures/items/empty_ankle_"
                            + (EnumWearable.getSubIndex(getSlotIndex()) == 0 ? "left" : "right");
                    break;
                case BACK:
                    tex = ThutWearables.MODID + ":textures/items/empty_back";
                    break;
                case EAR:
                    tex = ThutWearables.MODID + ":textures/items/empty_ear_"
                            + (EnumWearable.getSubIndex(getSlotIndex()) == 0 ? "left" : "right");
                    break;
                case EYE:
                    tex = ThutWearables.MODID + ":textures/items/empty_eye";
                    break;
                case FINGER:
                    tex = ThutWearables.MODID + ":textures/items/empty_finger_"
                            + (EnumWearable.getSubIndex(getSlotIndex()) == 0 ? "left" : "right");
                    break;
                case HAT:
                    tex = ThutWearables.MODID + ":textures/items/empty_hat";
                    break;
                case NECK:
                    tex = ThutWearables.MODID + ":textures/items/empty_neck";
                    break;
                case WAIST:
                    tex = ThutWearables.MODID + ":textures/items/empty_waist";
                    break;
                case WRIST:
                    tex = ThutWearables.MODID + ":textures/items/empty_wrist_"
                            + (EnumWearable.getSubIndex(getSlotIndex()) == 0 ? "left" : "right");
                    break;
                default:
                    break;
                }
                if (tex != null)
                {
                    this.setBackgroundName(tex);
                    tex = tex + ".png";
                    this.setBackgroundLocation(new ResourceLocation(tex));
                    TextureAtlasSprite sprite = getBackgroundMap().getTextureExtry(getSlotTexture());
                    if (sprite == null)
                    {
                        getBackgroundMap().registerSprite(getBackgroundLocation());
                        sprite = super.getBackgroundSprite();
                        sprite.setIconHeight(16);
                        sprite.setIconWidth(16);
                        sprite.initSprite(16, 16, 0, 0, false);
                    }
                }
                init = true;
            }
            TextureAtlasSprite sprite = super.getBackgroundSprite();
            return sprite;
        }

        @Override
        /** Check if the stack is a valid item for this slot. Always true beside
         * for the armor slots. */
        public boolean isItemValid(@Nullable ItemStack stack)
        {
            return slots.isItemValidForSlot(getSlotIndex(), stack);
        }

        @Override
        public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack)
        {
            if (!wearer.getEntityWorld().isRemote)
            {
                EnumWearable.takeOff(thePlayer, stack, getSlotIndex());
            }
            return super.onTake(thePlayer, stack);
        }

        @Override
        public void putStack(ItemStack stack)
        {
            if (!wearer.getEntityWorld().isRemote) EnumWearable.putOn(wearer, stack, getSlotIndex());
            super.putStack(stack);
        }

        @Override
        /** Return whether this slot's stack can be taken from this slot. */
        public boolean canTakeStack(EntityPlayer playerIn)
        {
            return EnumWearable.canTakeOff(wearer, getStack(), getSlotIndex());
        }
    }

    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 2, 2);
    public IInventory        craftResult = new InventoryCraftResult();
    /** The crafting matrix inventory. */
    public PlayerWearables   slots;
    /** Determines if inventory manipulation should be handled. */
    final EntityLivingBase   wearer;
    final boolean            hasPlayerSlots;

    public ContainerWearables(EntityPlayer player)
    {
        this(player, player);
    }

    public ContainerWearables(EntityLivingBase wearer, EntityPlayer player)
    {
        this.wearer = wearer;
        slots = ThutWearables.getWearables(wearer);
        int xOffset = 116;
        int yOffset = 8;
        int xWidth = 18;
        int yHeight = 18;
        InventoryWrapper wrapper = new InventoryWrapper(slots);

        // First row of ear - hat - ear
        this.addSlotToContainer(new WornSlot(wearer, wrapper, 9, xOffset, yOffset));
        this.addSlotToContainer(new WornSlot(wearer, wrapper, 12, xOffset + xWidth, yOffset));
        this.addSlotToContainer(new WornSlot(wearer, wrapper, 10, xOffset + 2 * xWidth, yOffset));

        // Second row of arm - eye - arm
        this.addSlotToContainer(new WornSlot(wearer, wrapper, 2, xOffset, yOffset + yHeight));
        this.addSlotToContainer(new WornSlot(wearer, wrapper, 11, xOffset + xWidth, yOffset + yHeight));
        this.addSlotToContainer(new WornSlot(wearer, wrapper, 3, xOffset + 2 * xWidth, yOffset + yHeight));

        // Third row of finger - neck - finger
        this.addSlotToContainer(new WornSlot(wearer, wrapper, 0, xOffset, yOffset + yHeight * 2));
        this.addSlotToContainer(new WornSlot(wearer, wrapper, 6, xOffset + xWidth, yOffset + yHeight * 2));
        this.addSlotToContainer(new WornSlot(wearer, wrapper, 1, xOffset + 2 * xWidth, yOffset + yHeight * 2));

        // Fourth row of ankle - waist - ankle
        this.addSlotToContainer(new WornSlot(wearer, wrapper, 4, xOffset, yOffset + yHeight * 3));
        this.addSlotToContainer(new WornSlot(wearer, wrapper, 8, xOffset + xWidth, yOffset + yHeight * 3));
        this.addSlotToContainer(new WornSlot(wearer, wrapper, 5, xOffset + 2 * xWidth, yOffset + yHeight * 3));

        // back slot
        this.addSlotToContainer(new WornSlot(wearer, wrapper, 7, xOffset - xWidth, yOffset + yHeight * 3));

        hasPlayerSlots = player != null;
        if (hasPlayerSlots) bindVanillaInventory(player.inventory);
    }

    private void bindVanillaInventory(InventoryPlayer playerInventory)
    {
        IInventory armour = new ArmourInventory(wearer);

        // Player armour slots.
        for (int k = 0; k < 4; ++k)
        {
            final EntityEquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[k];
            int index = 36 + (3 - k);

            index = 3 - k;

            this.addSlotToContainer(new Slot(armour, index, 8, 8 + k * 18)
            {
                /** Returns the maximum stack size for a given slot (usually the
                 * same as getInventoryStackLimit(), but 1 in the case of armor
                 * slots) */
                @Override
                public int getSlotStackLimit()
                {
                    return 1;
                }

                /** Check if the stack is allowed to be placed in this slot,
                 * used for armor slots as well as furnace fuel. */
                @Override
                public boolean isItemValid(ItemStack stack)
                {
                    return stack.getItem().isValidArmor(stack, entityequipmentslot, wearer);
                }

                /** Return whether this slot's stack can be taken from this
                 * slot. */
                @Override
                public boolean canTakeStack(EntityPlayer playerIn)
                {
                    ItemStack itemstack = this.getStack();
                    return !itemstack.isEmpty() && !playerIn.isCreative()
                            && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.canTakeStack(playerIn);
                }

                @Override
                @Nullable
                @SideOnly(Side.CLIENT)
                public String getSlotTexture()
                {
                    return ItemArmor.EMPTY_SLOT_NAMES[entityequipmentslot.getIndex()];
                }
            });
        }

        // Main player inventory
        for (int l = 0; l < 3; ++l)
        {
            for (int j1 = 0; j1 < 9; ++j1)
            {
                this.addSlotToContainer(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        // Player hotbar
        for (int i1 = 0; i1 < 9; ++i1)
        {
            this.addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
        }

        // Offhand slot
        this.addSlotToContainer(new Slot(playerInventory, 40, 77, 62)
        {
            @Override
            @Nullable
            @SideOnly(Side.CLIENT)
            public String getSlotTexture()
            {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });
    }

    /** Called when the container is closed. */
    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);
        if (!player.world.isRemote)
        {
            ThutWearables.syncWearables(wearer);
        }
    }

    /** Called when a player shift-clicks on a slot. You must override this or
     * you will crash when someone does that. */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            int numRows = hasPlayerSlots ? 3 : 0;
            if (index < numRows * 9)
            {
                if (!this.mergeItemStack(itemstack1, numRows * 9, this.inventorySlots.size(),
                        false)) { return ItemStack.EMPTY; }
            }
            else if (!this.mergeItemStack(itemstack1, 0, numRows * 9, false)) { return ItemStack.EMPTY; }

            if (!CompatWrapper.isValid(itemstack1))
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

}
