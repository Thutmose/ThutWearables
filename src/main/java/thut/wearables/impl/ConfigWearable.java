package thut.wearables.impl;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;

public class ConfigWearable implements IActiveWearable, ICapabilityProvider
{
    EnumWearable slot;

    public ConfigWearable(EnumWearable slot)
    {
        this.slot = slot;
    }

    @Override
    public EnumWearable getSlot(ItemStack stack)
    {
        if (slot == null && stack.hasTag() && stack.getTag().hasKey("wslot"))
        {
            slot = EnumWearable.valueOf(stack.getTag().getString("wslot"));
        }
        return slot;
    }

    @Override
    public void renderWearable(EnumWearable slot, LivingEntity wearer, ItemStack stack, float partialTicks)
    {
        // TODO way to register renderers for config wearables

        // This is for items that should just be directly rendered
        if (stack.hasTag() && stack.getTag().hasKey("wslot"))
        {

            GlStateManager.pushMatrix();

            GlStateManager.rotate(180, 0, 0, 1);

            if (stack.getTag().hasKey("winfo"))
            {
                CompoundNBT info = stack.getTag().getCompound("winfo");
                if (info.hasKey("scale"))
                {
                    float scale = info.getFloat("scale");
                    GlStateManager.scale(scale, scale, scale);
                }
                if (info.hasKey("shiftx"))
                {
                    float shift = info.getFloat("shiftx");
                    GlStateManager.translate(shift, 0, 0);
                }
                if (info.hasKey("shifty"))
                {
                    float shift = info.getFloat("shifty");
                    GlStateManager.translate(0, shift, 0);
                }
                if (info.hasKey("shiftz"))
                {
                    float shift = info.getFloat("shiftz");
                    GlStateManager.translate(0, 0, shift);
                }
                if (info.hasKey("rotx"))
                {
                    float shift = info.getFloat("rotx");
                    GlStateManager.rotate(shift, 1, 0, 0);
                }
                if (info.hasKey("roty"))
                {
                    float shift = info.getFloat("roty");
                    GlStateManager.rotate(shift, 0, 1, 0);
                }
                if (info.hasKey("rotz"))
                {
                    float shift = info.getFloat("rotz");
                    GlStateManager.rotate(shift, 0, 0, 1);
                }

            }

            GlStateManager.translate(-0.25, 0, 0);
            Minecraft.getInstance().getItemRenderer().renderItem(wearer, stack, TransformType.NONE);
            GlStateManager.popMatrix();
        }

    }

    @Override
    public boolean hasCapability(Capability<?> capability, Direction facing)
    {
        return capability == WEARABLE_CAP;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing)
    {
        return hasCapability(capability, facing) ? IActiveWearable.WEARABLE_CAP.cast(this) : null;
    }

    @Override
    public boolean dyeable(ItemStack stack)
    {
        // TODO see if this should be appled here.
        return false;
    }

}
