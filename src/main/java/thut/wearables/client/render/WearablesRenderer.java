package thut.wearables.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class WearablesRenderer implements LayerRenderer<EntityPlayer>
{
    float[]                           offsetArr = { 0, 0, 0 };

    private final RenderLivingBase<?> livingEntityRenderer;

    public WearablesRenderer(RenderLivingBase<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
    }

    @Override
    public void doRenderLayer(EntityPlayer player, float f, float f1, float partialTicks, float f3, float f4, float f5,
            float scale)
    {
        ItemStack beltStack = null;
        ItemStack leftRing = null;
        ItemStack rightRing = null;
        ItemStack leftBrace = null;
        ItemStack rightBrace = null;
        ItemStack leftEar = null;
        ItemStack rightEar = null;
        ItemStack bag = null;
        ItemStack hat = null;
        ItemStack leftLeg = null;
        ItemStack rightLeg = null;
        ItemStack neck = null;
        ItemStack eyes = null;
        PlayerWearables worn = ThutWearables.getWearables(player);
        rightRing = worn.getWearable(EnumWearable.FINGER, 0);
        leftRing = worn.getWearable(EnumWearable.FINGER, 1);
        rightBrace = worn.getWearable(EnumWearable.WRIST, 0);
        leftBrace = worn.getWearable(EnumWearable.WRIST, 1);
        rightLeg = worn.getWearable(EnumWearable.ANKLE, 0);
        leftLeg = worn.getWearable(EnumWearable.ANKLE, 1);
        neck = worn.getWearable(EnumWearable.NECK);
        bag = worn.getWearable(EnumWearable.BACK);
        beltStack = worn.getWearable(EnumWearable.WAIST);
        rightEar = worn.getWearable(EnumWearable.EAR, 0);
        leftEar = worn.getWearable(EnumWearable.EAR, 1);
        eyes = worn.getWearable(EnumWearable.EYE);
        hat = worn.getWearable(EnumWearable.HAT);

        if (!(this.livingEntityRenderer.getMainModel() instanceof ModelBiped)) return;
        boolean thin = ((AbstractClientPlayer) player).getSkinType().equals("slim");
        GlStateManager.pushMatrix();
        if (rightRing != null && !ThutWearables.renderBlacklist.contains(0))
        {
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.01F);
                if ((offsetArr = ThutWearables.renderOffsetsSneak.get(0)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedRightArm.postRender(0.0625f);
            GlStateManager.translate(-0.0625F, 0.59F, 0.0625F);
            if ((offsetArr = ThutWearables.renderOffsets.get(0)) != null)
            {
                GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            }
            if (thin)
            {
                GlStateManager.translate(0.025, 0, 0);
                GlStateManager.scale(0.75, 1, 1);
            }
            render(rightRing, player, EnumWearable.FINGER, partialTicks);
            GlStateManager.popMatrix();
        }
        if (leftRing != null && !ThutWearables.renderBlacklist.contains(1))
        {
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.01F);
                if ((offsetArr = ThutWearables.renderOffsetsSneak.get(1)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedLeftArm.postRender(0.0625f);
            GlStateManager.translate(0.0625F, 0.59F, 0.0625F);
            if ((offsetArr = ThutWearables.renderOffsets.get(1)) != null)
            {
                GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            }
            if (thin)
            {
                GlStateManager.translate(-0.025, 0, 0);
                GlStateManager.scale(0.75, 1, 1);
            }
            render(leftRing, player, EnumWearable.FINGER, partialTicks);
            GlStateManager.popMatrix();
        }
        if (rightBrace != null && !ThutWearables.renderBlacklist.contains(2))
        {
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.01F);
                if ((offsetArr = ThutWearables.renderOffsetsSneak.get(2)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedRightArm.postRender(0.0625f);
            GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);
            if ((offsetArr = ThutWearables.renderOffsets.get(2)) != null)
            {
                GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            }
            if (thin)
            {
                GlStateManager.translate(0.025, 0, 0);
                GlStateManager.scale(0.75, 1, 1);
            }
            render(rightBrace, player, EnumWearable.WRIST, partialTicks);
            GlStateManager.popMatrix();
        }
        if (leftBrace != null && !ThutWearables.renderBlacklist.contains(3))
        {
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.01F);
                if ((offsetArr = ThutWearables.renderOffsetsSneak.get(3)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedLeftArm.postRender(0.0625f);
            GlStateManager.translate(0.0625F, 0.4375F, 0.0625F);
            if ((offsetArr = ThutWearables.renderOffsets.get(3)) != null)
            {
                GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            }
            if (thin)
            {
                GlStateManager.translate(-0.025, 0, 0);
                GlStateManager.scale(0.75, 1, 1);
            }
            render(leftBrace, player, EnumWearable.WRIST, partialTicks);
            GlStateManager.popMatrix();
        }
        if (rightLeg != null && !ThutWearables.renderBlacklist.contains(4))
        {
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.01F);
                if ((offsetArr = ThutWearables.renderOffsetsSneak.get(4)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedRightLeg.postRender(0.0625f);
            GlStateManager.translate(0.0F, 0.4375F, 0.0625F);
            if ((offsetArr = ThutWearables.renderOffsets.get(4)) != null)
            {
                GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            }
            render(rightLeg, player, EnumWearable.ANKLE, partialTicks);
            GlStateManager.popMatrix();
        }
        if (leftLeg != null && !ThutWearables.renderBlacklist.contains(5))
        {
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.01F);
                if ((offsetArr = ThutWearables.renderOffsetsSneak.get(5)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedLeftLeg.postRender(0.0625f);
            GlStateManager.translate(0.0F, 0.4375F, 0.0625F);
            if ((offsetArr = ThutWearables.renderOffsets.get(5)) != null)
            {
                GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            }
            render(leftLeg, player, EnumWearable.ANKLE, partialTicks);
            GlStateManager.popMatrix();
        }
        if (neck != null && !ThutWearables.renderBlacklist.contains(6))
        {
            GL11.glPushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.0F);
                if ((offsetArr = ThutWearables.renderOffsetsSneak.get(6)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedBody.postRender(0.0625F);
            if ((offsetArr = ThutWearables.renderOffsets.get(6)) != null)
            {
                GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            }
            render(neck, player, EnumWearable.NECK, partialTicks);
            GL11.glPopMatrix();
        }
        if (bag != null && !ThutWearables.renderBlacklist.contains(7))
        {
            GL11.glPushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.0F);
                if ((offsetArr = ThutWearables.renderOffsetsSneak.get(7)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedBody.postRender(0.0625F);
            if ((offsetArr = ThutWearables.renderOffsets.get(7)) != null)
            {
                GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            }
            render(bag, player, EnumWearable.BACK, partialTicks);
            GL11.glPopMatrix();
        }
        if (beltStack != null && !ThutWearables.renderBlacklist.contains(8))
        {
            // First pass of render
            GL11.glPushMatrix();
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedBody.postRender(0.0625F);
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.13125F, -0.105F);
                if ((offsetArr = ThutWearables.renderOffsetsSneak.get(8)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
            }
            if ((offsetArr = ThutWearables.renderOffsets.get(8)) != null)
            {
                GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
            }
            render(beltStack, player, EnumWearable.WAIST, partialTicks);
            GL11.glPopMatrix();
        }
        if (hat != null || leftEar != null || rightEar != null || eyes != null)
        {
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
                if ((offsetArr = ThutWearables.renderOffsetsSneak.get(9)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
            }
            if (player.isChild())
            {
                float af = 2.0F;
                float af1 = 1.4F;
                GlStateManager.translate(0.0F, 0.5F * scale, 0.0F);
                GlStateManager.scale(af1 / af, af1 / af, af1 / af);
                GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedHead.postRender(0.0625F);
            GlStateManager.translate(0, -0.25, 0);
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (rightEar != null && !ThutWearables.renderBlacklist.contains(9))
            {
                GlStateManager.pushMatrix();
                GL11.glTranslated(-0.25, -0.1, 0.0);
                GL11.glRotated(90, 0, 1, 0);
                GL11.glRotated(90, 1, 0, 0);
                if ((offsetArr = ThutWearables.renderOffsets.get(9)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
                render(rightEar, player, EnumWearable.EAR, partialTicks);
                GlStateManager.popMatrix();
            }
            if (leftEar != null && !ThutWearables.renderBlacklist.contains(10))
            {
                GlStateManager.pushMatrix();
                GL11.glTranslated(0.25, -0.1, 0.0);
                GL11.glRotated(90, 0, 1, 0);
                GL11.glRotated(90, 1, 0, 0);
                if ((offsetArr = ThutWearables.renderOffsets.get(10)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
                render(leftEar, player, EnumWearable.EAR, partialTicks);
                GlStateManager.popMatrix();
            }
            if (eyes != null && !ThutWearables.renderBlacklist.contains(11))
            {
                GlStateManager.pushMatrix();
                if ((offsetArr = ThutWearables.renderOffsets.get(11)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
                render(eyes, player, EnumWearable.EYE, partialTicks);
                GlStateManager.popMatrix();
            }
            if (hat != null && !ThutWearables.renderBlacklist.contains(12))
            {
                GlStateManager.pushMatrix();
                if ((offsetArr = ThutWearables.renderOffsets.get(12)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
                render(hat, player, EnumWearable.HAT, partialTicks);
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

    private void render(ItemStack stack, EntityPlayer player, EnumWearable slot, float partialTicks)
    {
        if (stack.getItem() instanceof IWearable)
        {
            ((IWearable) stack.getItem()).renderWearable(slot, player, stack, partialTicks);
            return;
        }
        IActiveWearable wearable;
        if ((wearable = stack.getCapability(IActiveWearable.WEARABLE_CAP, null)) != null)
        {
            wearable.renderWearable(slot, player, stack, partialTicks);
        }
    }

}
