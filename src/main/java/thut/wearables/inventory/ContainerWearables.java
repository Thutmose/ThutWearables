package thut.wearables.inventory;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.wearables.CompatWrapper;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;

public class ContainerWearables extends Container
{
    private static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EntityEquipmentSlot[] {
            EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET };

    public static class WornSlot extends Slot
    {
        final EntityPlayer     wearer;
        final EnumWearable     slot;
        final InventoryWrapper slots;
        private boolean        init = false;

        public WornSlot(EntityPlayer player, InventoryWrapper inventoryIn, int index, int xPosition, int yPosition)
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
            if (!wearer.world.isRemote)
            {
                EnumWearable.takeOff(thePlayer, stack, getSlotIndex());
            }
            return super.onTake(thePlayer, stack);
        }

        @Override
        public void putStack(ItemStack stack)
        {
            if (!wearer.world.isRemote) EnumWearable.putOn(wearer, stack, getSlotIndex());
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
    final EntityPlayer       thePlayer;

    public ContainerWearables(EntityPlayer player)
    {
        this.thePlayer = player;
        slots = ThutWearables.getWearables(player);
        int xOffset = 116;
        int yOffset = 8;
        int xWidth = 18;
        int yHeight = 18;
        InventoryWrapper wrapper = new InventoryWrapper(slots);

        // First row of ear - hat - ear
        this.addSlotToContainer(new WornSlot(player, wrapper, 9, xOffset, yOffset));
        this.addSlotToContainer(new WornSlot(player, wrapper, 12, xOffset + xWidth, yOffset));
        this.addSlotToContainer(new WornSlot(player, wrapper, 10, xOffset + 2 * xWidth, yOffset));

        // Second row of arm - eye - arm
        this.addSlotToContainer(new WornSlot(player, wrapper, 2, xOffset, yOffset + yHeight));
        this.addSlotToContainer(new WornSlot(player, wrapper, 11, xOffset + xWidth, yOffset + yHeight));
        this.addSlotToContainer(new WornSlot(player, wrapper, 3, xOffset + 2 * xWidth, yOffset + yHeight));

        // Third row of finger - neck - finger
        this.addSlotToContainer(new WornSlot(player, wrapper, 0, xOffset, yOffset + yHeight * 2));
        this.addSlotToContainer(new WornSlot(player, wrapper, 6, xOffset + xWidth, yOffset + yHeight * 2));
        this.addSlotToContainer(new WornSlot(player, wrapper, 1, xOffset + 2 * xWidth, yOffset + yHeight * 2));

        // Fourth row of ankle - waist - ankle
        this.addSlotToContainer(new WornSlot(player, wrapper, 4, xOffset, yOffset + yHeight * 3));
        this.addSlotToContainer(new WornSlot(player, wrapper, 8, xOffset + xWidth, yOffset + yHeight * 3));
        this.addSlotToContainer(new WornSlot(player, wrapper, 5, xOffset + 2 * xWidth, yOffset + yHeight * 3));

        // back slot
        this.addSlotToContainer(new WornSlot(player, wrapper, 7, xOffset - xWidth, yOffset + yHeight * 3));

        bindVanillaInventory(player.inventory);
    }

    private void bindVanillaInventory(InventoryPlayer playerInventory)
    {

        // Player armour slots.
        for (int k = 0; k < 4; ++k)
        {
            final EntityEquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[k];
            this.addSlotToContainer(new Slot(playerInventory, 36 + (3 - k), 8, 8 + k * 18)
            {
                /** Returns the maximum stack size for a given slot (usually the
                 * same as getInventoryStackLimit(), but 1 in the case of armor
                 * slots) */
                public int getSlotStackLimit()
                {
                    return 1;
                }

                /** Check if the stack is allowed to be placed in this slot,
                 * used for armor slots as well as furnace fuel. */
                public boolean isItemValid(ItemStack stack)
                {
                    return stack.getItem().isValidArmor(stack, entityequipmentslot, playerInventory.player);
                }

                /** Return whether this slot's stack can be taken from this
                 * slot. */
                public boolean canTakeStack(EntityPlayer playerIn)
                {
                    ItemStack itemstack = this.getStack();
                    return !itemstack.isEmpty() && !playerIn.isCreative()
                            && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.canTakeStack(playerIn);
                }

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
            ThutWearables.syncWearables(player);
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

            int numRows = 3;
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
