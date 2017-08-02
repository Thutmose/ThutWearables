package thut.wearables.compat.baubles;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.render.IRenderBauble;
import baubles.api.render.IRenderBauble.RenderType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.wearables.CompatClass;
import thut.wearables.CompatClass.Phase;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.IWearable;
import thut.wearables.Reference;

public class Compat
{
    private static final ResourceLocation BAUBLEWRAP   = new ResourceLocation(Reference.MODID, "baublewrap");
    private static final ResourceLocation WEARABLEWRAP = new ResourceLocation(Reference.MODID, "wearablewrap");

    @CompatClass(phase = Phase.CONSTRUCT)
    @Optional.Method(modid = "baubles")
    public static void init()
    {
        MinecraftForge.EVENT_BUS.register(new Compat());
    }

    public Compat()
    {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onItemCapabilityAttach(AttachCapabilitiesEvent<ItemStack> event)
    {
        boolean baubleContainer = false;
        boolean bauble = event.getObject().getItem() instanceof IBauble;
        boolean wearable = event.getObject().getItem() instanceof IWearable;
        IBauble baubleCap = bauble ? (IBauble) event.getObject().getItem() : null;
        IWearable wearableCap = wearable ? (IWearable) event.getObject().getItem() : null;
        for (ICapabilityProvider provider : event.getCapabilities().values())
        {
            if (provider.hasCapability(IActiveWearable.WEARABLE_CAP, null))
            {
                wearable = true;
                wearableCap = provider.getCapability(IActiveWearable.WEARABLE_CAP, null);
            }
            if (provider.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null))
            {
                bauble = true;
                baubleCap = provider.getCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null);
            }
        }
        if (bauble && !wearable)
        {
            BaubleWrapper wrapper = new BaubleWrapper(baubleCap);
            event.addCapability(BAUBLEWRAP, wrapper);
        }
        if (wearable && !baubleContainer)
        {
            WearableWrapper wrapper = new WearableWrapper(wearableCap);
            event.addCapability(WEARABLEWRAP, wrapper);
        }
    }

    public static class WearableWrapper implements IBauble, IRenderBauble, ICapabilityProvider
    {
        final IWearable       wrapped;
        final IActiveWearable active;

        public WearableWrapper(IWearable wrapped)
        {
            this.wrapped = wrapped;
            if (wrapped instanceof IActiveWearable) active = (IActiveWearable) wrapped;
            else active = null;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return hasCapability(capability, facing) ? (T) this : null;
        }

        @Override
        public BaubleType getBaubleType(ItemStack itemstack)
        {
            EnumWearable slot = wrapped.getSlot(itemstack);
            switch (slot)
            {
            case BACK:
                return BaubleType.BODY;
            case WAIST:
                return BaubleType.BELT;
            case FINGER:
                return BaubleType.RING;
            case NECK:
                return BaubleType.AMULET;
            case HAT:
                return BaubleType.HEAD;
            case EAR:
                return BaubleType.HEAD;
            case EYE:
                return BaubleType.HEAD;
            case WRIST:
                return BaubleType.RING;
            case ANKLE:
                return BaubleType.CHARM;
            }
            return null;
        }

        @Override
        /** This method is called once per tick if the bauble is being worn by a
         * player */
        public void onWornTick(ItemStack itemstack, EntityLivingBase player)
        {
            if (active != null)
            {
                // TODO maybe find the index?
                active.onUpdate(player, itemstack, active.getSlot(itemstack), 0);
            }
        }

        @Override
        /** This method is called when the bauble is equipped by a player */
        public void onEquipped(ItemStack itemstack, EntityLivingBase player)
        {
            if (active != null)
            {
                // TODO maybe find the index?
                active.onPutOn(player, itemstack, active.getSlot(itemstack), 0);
            }
        }

        @Override
        /** This method is called when the bauble is unequipped by a player */
        public void onUnequipped(ItemStack itemstack, EntityLivingBase player)
        {
            if (active != null)
            {
                // TODO maybe find the index?
                active.onTakeOff(player, itemstack, active.getSlot(itemstack), 0);
            }
        }

        @Override
        /** can this bauble be placed in a bauble slot */
        public boolean canEquip(ItemStack itemstack, EntityLivingBase player)
        {
            return wrapped.canPutOn(player, itemstack, wrapped.getSlot(itemstack), 0);
        }

        @Override
        /** Can this bauble be removed from a bauble slot */
        public boolean canUnequip(ItemStack itemstack, EntityLivingBase player)
        {
            // TODO maybe find the index?
            return wrapped.canRemove(player, itemstack, wrapped.getSlot(itemstack), 0);
        }

        @Override
        public void onPlayerBaubleRender(ItemStack arg0, EntityPlayer arg1, RenderType arg2, float arg3)
        {
            wrapped.renderWearable(wrapped.getSlot(arg0), arg1, arg0, arg3);
        }

    }

    public static class BaubleWrapper implements IActiveWearable, ICapabilityProvider
    {
        final IBauble       wrapped;
        final IRenderBauble render;

        public BaubleWrapper(IBauble wrapped)
        {
            this.wrapped = wrapped;
            if (wrapped instanceof IRenderBauble)
            {
                render = (IRenderBauble) wrapped;
            }
            else render = null;
        }

        @Override
        public EnumWearable getSlot(ItemStack stack)
        {
            BaubleType type = wrapped.getBaubleType(stack);
            switch (type)
            {
            case AMULET:
                return EnumWearable.NECK;
            case BELT:
                return EnumWearable.WAIST;
            case BODY:
                return EnumWearable.BACK;
            case HEAD:
                return EnumWearable.HAT;
            case RING:
                return EnumWearable.FINGER;
            case CHARM:
                return EnumWearable.WRIST;
            case TRINKET:// TODO better handling of trinket.
                return EnumWearable.ANKLE;
            }
            return null;
        }

        @Override
        public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
        {
            if (render != null && wearer instanceof EntityPlayer)
            {
                RenderType type = null;
                EntityPlayer player = (EntityPlayer) wearer;
                switch (slot)
                {
                case HAT:
                    type = RenderType.HEAD;
                    break;
                case EAR:
                    type = RenderType.HEAD;
                    break;
                case EYE:
                    type = RenderType.HEAD;
                    break;
                case NECK:
                    type = RenderType.BODY;
                    break;
                case BACK:
                    type = RenderType.BODY;
                    break;
                case WAIST:
                    type = RenderType.BODY;
                    break;
                default:
                    break;
                }
                if (type != null) render.onPlayerBaubleRender(stack, player, type, partialTicks);
            }
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == IActiveWearable.WEARABLE_CAP;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return hasCapability(capability, facing) ? (T) this : null;
        }

        @Override
        public void onPutOn(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
            wrapped.onEquipped(itemstack, player);
        }

        @Override
        public void onTakeOff(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
            wrapped.onUnequipped(itemstack, player);
        }

        @Override
        public void onUpdate(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
            wrapped.onWornTick(itemstack, player);
        }

    }
}
