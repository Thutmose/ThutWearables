package thut.wearables.compat.botania;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreCriteria.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import thut.wearables.CompatClass;
import thut.wearables.CompatClass.Phase;
import thut.wearables.ThutWearables;
import vazkii.botania.api.item.IBaubleRender;
import vazkii.botania.api.item.ICosmeticAttachable;
import vazkii.botania.api.item.IPhantomInkable;

public class Compat
{
    @CompatClass(phase = Phase.INIT)
    @OnlyIn(Dist.CLIENT)
    @Optional.Method(modid = "botania")
    public static void init()
    {
        MinecraftForge.EVENT_BUS.register(new Compat());
    }

    private Set<RenderPlayer> addedBaubles = Sets.newHashSet();

    @SubscribeEvent
    public void addBaubleRender(RenderPlayerEvent.Post event)
    {
        if (addedBaubles.contains(event.getRenderer())) { return; }
        List<LayerRenderer<?>> layerRenderers = ReflectionHelper.getPrivateValue(RenderLivingBase.class,
                event.getRenderer(), "layerRenderers", "field_177097_h", "i");
        layerRenderers.add(1, new BotaniaRenderHandler());
        addedBaubles.add(event.getRenderer());
    }

    /** This is a modified version of botania's BaubleRenderHandler The original
     * header for that file is below: 
     * 
     * This class was created by <Vazkii>. It's distributed as
     * part of the Botania Mod. Get the Source Code in github:
     * https://github.com/Vazkii/Botania
     *
     * Botania is Open Source and distributed under the
     * Botania License: http://botaniamod.net/license.php
     *
     * File Created @ [Aug 27, 2014, 8:55:00 PM (GMT)]
     * 
     * */
    @OnlyIn(Dist.CLIENT)
    public static class BotaniaRenderHandler implements LayerRenderer<PlayerEntity>
    {
        @Override
        public void doRenderLayer(@Nonnull PlayerEntity player, float limbSwing, float limbSwingAmount,
                float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
        {
            if (player.getActiveEffectInstance(MobEffects.INVISIBILITY) != null) return;

            IItemHandler inv = ThutWearables.getWearables(player);

            dispatchRenders(inv, player, RenderType.BODY, partialTicks);

            float yaw = player.prevRotationYawHead
                    + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
            float yawOffset = player.prevRenderYawOffset
                    + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
            float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;

            GlStateManager.pushMatrix();
            GlStateManager.rotate(yawOffset, 0, -1, 0);
            GlStateManager.rotate(yaw - 270, 0, 1, 0);
            GlStateManager.rotate(pitch, 0, 0, 1);
            dispatchRenders(inv, player, RenderType.HEAD, partialTicks);

            GlStateManager.popMatrix();
        }

        private void dispatchRenders(IItemHandler inv, PlayerEntity player, RenderType type, float partialTicks)
        {
            for (int i = 0; i < inv.getSlots(); i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty())
                {
                    Item item = stack.getItem();

                    if (item instanceof IPhantomInkable)
                    {
                        IPhantomInkable inkable = (IPhantomInkable) item;
                        if (inkable.hasPhantomInk(stack)) continue;
                    }

                    if (item instanceof ICosmeticAttachable)
                    {
                        ICosmeticAttachable attachable = (ICosmeticAttachable) item;
                        ItemStack cosmetic = attachable.getCosmeticItem(stack);
                        if (!cosmetic.isEmpty())
                        {
                            GlStateManager.pushMatrix();
                            GL11.glColor3ub((byte) 255, (byte) 255, (byte) 255);
                            GlStateManager.color(1F, 1F, 1F, 1F);
                            ((IBaubleRender) cosmetic.getItem()).onPlayerBaubleRender(cosmetic, player, type,
                                    partialTicks);
                            GlStateManager.popMatrix();
                            continue;
                        }
                    }

                    if (item instanceof IBaubleRender)
                    {
                        GlStateManager.pushMatrix();
                        GL11.glColor3ub((byte) 255, (byte) 255, (byte) 255);
                        GlStateManager.color(1F, 1F, 1F, 1F);
                        ((IBaubleRender) stack.getItem()).onPlayerBaubleRender(stack, player, type, partialTicks);
                        GlStateManager.popMatrix();
                    }
                }
            }
        }

        @Override
        public boolean shouldCombineTextures()
        {
            return false;
        }
    }

}
