package thut.wearables.client.render;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.model.ModelBox;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class WearablesRenderer implements LayerRenderer<EntityLivingBase>
{
    float[]                           offsetArr = { 0, 0, 0 };

    private final RenderLivingBase<?> livingEntityRenderer;

    public WearablesRenderer(RenderLivingBase<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
    }

    private IWearable getWearable(ItemStack stack)
    {
        if (stack.getItem() instanceof IWearable) { return (IWearable) stack.getItem(); }
        return stack.getCapability(IActiveWearable.WEARABLE_CAP, null);
    }

    @Override
    public void doRenderLayer(EntityLivingBase wearer, float f, float f1, float partialTicks, float f3, float f4,
            float f5, float scale)
    {
        // No Render invisible.
        if (wearer.getActivePotionEffect(MobEffects.INVISIBILITY) != null) return;
        // Only applies to bipeds, anyone else needs to write their own render
        // layer.
        if (!(this.livingEntityRenderer.getMainModel() instanceof ModelBiped)) return;
        ModelBiped theModel = (ModelBiped) this.livingEntityRenderer.getMainModel();

        IWearable beltStack = null;
        IWearable leftRing = null;
        IWearable rightRing = null;
        IWearable leftBrace = null;
        IWearable rightBrace = null;
        IWearable leftEar = null;
        IWearable rightEar = null;
        IWearable bag = null;
        IWearable hat = null;
        IWearable leftLeg = null;
        IWearable rightLeg = null;
        IWearable neck = null;
        IWearable eyes = null;
        PlayerWearables worn = ThutWearables.getWearables(wearer);
        rightRing = getWearable(worn.getWearable(EnumWearable.FINGER, 0));
        leftRing = getWearable(worn.getWearable(EnumWearable.FINGER, 1));
        rightBrace = getWearable(worn.getWearable(EnumWearable.WRIST, 0));
        leftBrace = getWearable(worn.getWearable(EnumWearable.WRIST, 1));
        rightLeg = getWearable(worn.getWearable(EnumWearable.ANKLE, 0));
        leftLeg = getWearable(worn.getWearable(EnumWearable.ANKLE, 1));
        neck = getWearable(worn.getWearable(EnumWearable.NECK));
        bag = getWearable(worn.getWearable(EnumWearable.BACK));
        beltStack = getWearable(worn.getWearable(EnumWearable.WAIST));
        rightEar = getWearable(worn.getWearable(EnumWearable.EAR, 0));
        leftEar = getWearable(worn.getWearable(EnumWearable.EAR, 1));
        eyes = getWearable(worn.getWearable(EnumWearable.EYE));
        hat = getWearable(worn.getWearable(EnumWearable.HAT));

        boolean thin = false;

        if (wearer instanceof AbstractClientPlayer)
        {
            thin = ((AbstractClientPlayer) wearer).getSkinType().equals("slim");
        }
        else
        {
            ModelBox box = theModel.bipedLeftArm.cubeList.get(0);
            thin = (box.posX2 - box.posX1) != (box.posZ2 - box.posZ1);
        }

        GlStateManager.pushMatrix();
        if (rightRing != null && !ThutWearables.renderBlacklist.contains(0))
        {
            if (rightRing.customOffsets())
            {
                rightRing.renderWearable(EnumWearable.FINGER, wearer, worn.getWearable(EnumWearable.FINGER, 0),
                        partialTicks);
            }
            else
            {
                GlStateManager.pushMatrix();
                if (wearer.isSneaking())
                {
                    GlStateManager.translate(0.0F, 0.23125F, 0.01F);
                    if ((offsetArr = ThutWearables.renderOffsetsSneak.get(0)) != null)
                    {
                        GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                    }
                }
                theModel.bipedRightArm.postRender(0.0625f);
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
                rightRing.renderWearable(EnumWearable.FINGER, wearer, worn.getWearable(EnumWearable.FINGER, 0),
                        partialTicks);
                GlStateManager.popMatrix();
            }
        }
        if (leftRing != null && !ThutWearables.renderBlacklist.contains(1))
        {
            if (leftRing.customOffsets())
            {
                leftRing.renderWearable(EnumWearable.FINGER, wearer, worn.getWearable(EnumWearable.FINGER, 1),
                        partialTicks);
            }
            else
            {
                GlStateManager.pushMatrix();
                if (wearer.isSneaking())
                {
                    GlStateManager.translate(0.0F, 0.23125F, 0.01F);
                    if ((offsetArr = ThutWearables.renderOffsetsSneak.get(1)) != null)
                    {
                        GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                    }
                }
                theModel.bipedLeftArm.postRender(0.0625f);
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
                GlStateManager.scale(-1, 1, 1);
                leftRing.renderWearable(EnumWearable.FINGER, wearer, worn.getWearable(EnumWearable.FINGER, 1),
                        partialTicks);
                GlStateManager.popMatrix();
            }
        }
        if (rightBrace != null && !ThutWearables.renderBlacklist.contains(2))
        {
            if (rightBrace.customOffsets())
            {
                rightBrace.renderWearable(EnumWearable.WRIST, wearer, worn.getWearable(EnumWearable.WRIST, 0),
                        partialTicks);
            }
            else
            {
                GlStateManager.pushMatrix();
                if (wearer.isSneaking())
                {
                    GlStateManager.translate(0.0F, 0.23125F, 0.01F);
                    if ((offsetArr = ThutWearables.renderOffsetsSneak.get(2)) != null)
                    {
                        GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                    }
                }
                theModel.bipedRightArm.postRender(0.0625f);
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
                rightBrace.renderWearable(EnumWearable.WRIST, wearer, worn.getWearable(EnumWearable.WRIST, 0),
                        partialTicks);
                GlStateManager.popMatrix();
            }
        }
        if (leftBrace != null && !ThutWearables.renderBlacklist.contains(3))
        {
            if (leftBrace.customOffsets())
            {
                leftBrace.renderWearable(EnumWearable.WRIST, wearer, worn.getWearable(EnumWearable.WRIST, 1),
                        partialTicks);
            }
            else
            {
                GlStateManager.pushMatrix();
                if (wearer.isSneaking())
                {
                    GlStateManager.translate(0.0F, 0.23125F, 0.01F);
                    if ((offsetArr = ThutWearables.renderOffsetsSneak.get(3)) != null)
                    {
                        GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                    }
                }
                theModel.bipedLeftArm.postRender(0.0625f);
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
                GlStateManager.scale(-1, 1, 1);
                leftBrace.renderWearable(EnumWearable.WRIST, wearer, worn.getWearable(EnumWearable.WRIST, 1),
                        partialTicks);
                GlStateManager.popMatrix();
            }
        }
        if (rightLeg != null && !ThutWearables.renderBlacklist.contains(4))
        {
            if (rightLeg.customOffsets())
            {
                rightLeg.renderWearable(EnumWearable.ANKLE, wearer, worn.getWearable(EnumWearable.ANKLE, 0),
                        partialTicks);
            }
            else
            {
                GlStateManager.pushMatrix();
                if (wearer.isSneaking())
                {
                    GlStateManager.translate(0.0F, 0.23125F, 0.01F);
                    if ((offsetArr = ThutWearables.renderOffsetsSneak.get(4)) != null)
                    {
                        GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                    }
                }
                theModel.bipedRightLeg.postRender(0.0625f);
                GlStateManager.translate(0.0F, 0.4375F, 0.0625F);
                if ((offsetArr = ThutWearables.renderOffsets.get(4)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
                rightLeg.renderWearable(EnumWearable.ANKLE, wearer, worn.getWearable(EnumWearable.ANKLE, 0),
                        partialTicks);
                GlStateManager.popMatrix();
            }
        }
        if (leftLeg != null && !ThutWearables.renderBlacklist.contains(5))
        {
            if (leftLeg.customOffsets())
            {
                leftLeg.renderWearable(EnumWearable.ANKLE, wearer, worn.getWearable(EnumWearable.ANKLE, 1),
                        partialTicks);
            }
            else
            {
                GlStateManager.pushMatrix();
                if (wearer.isSneaking())
                {
                    GlStateManager.translate(0.0F, 0.23125F, 0.01F);
                    if ((offsetArr = ThutWearables.renderOffsetsSneak.get(5)) != null)
                    {
                        GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                    }
                }
                theModel.bipedLeftLeg.postRender(0.0625f);
                GlStateManager.translate(0.0F, 0.4375F, 0.0625F);
                if ((offsetArr = ThutWearables.renderOffsets.get(5)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
                GlStateManager.scale(-1, 1, 1);
                leftLeg.renderWearable(EnumWearable.ANKLE, wearer, worn.getWearable(EnumWearable.ANKLE, 1),
                        partialTicks);
                GlStateManager.popMatrix();
            }
        }
        if (neck != null && !ThutWearables.renderBlacklist.contains(6))
        {
            if (neck.customOffsets())
            {
                neck.renderWearable(EnumWearable.NECK, wearer, worn.getWearable(EnumWearable.NECK), partialTicks);
            }
            else
            {
                GL11.glPushMatrix();
                if (wearer.isSneaking())
                {
                    GlStateManager.translate(0.0F, 0.23125F, 0.0F);
                    if ((offsetArr = ThutWearables.renderOffsetsSneak.get(6)) != null)
                    {
                        GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                    }
                }
                theModel.bipedBody.postRender(0.0625F);
                if ((offsetArr = ThutWearables.renderOffsets.get(6)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
                neck.renderWearable(EnumWearable.NECK, wearer, worn.getWearable(EnumWearable.NECK), partialTicks);
                GL11.glPopMatrix();
            }
        }
        if (bag != null && !ThutWearables.renderBlacklist.contains(7))
        {
            if (bag.customOffsets())
            {
                bag.renderWearable(EnumWearable.BACK, wearer, worn.getWearable(EnumWearable.BACK), partialTicks);
            }
            else
            {
                GL11.glPushMatrix();
                if (wearer.isSneaking())
                {
                    GlStateManager.translate(0.0F, 0.23125F, 0.0F);
                    if ((offsetArr = ThutWearables.renderOffsetsSneak.get(7)) != null)
                    {
                        GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                    }
                }
                theModel.bipedBody.postRender(0.0625F);
                if ((offsetArr = ThutWearables.renderOffsets.get(7)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
                bag.renderWearable(EnumWearable.BACK, wearer, worn.getWearable(EnumWearable.BACK), partialTicks);
                GL11.glPopMatrix();
            }
        }
        if (beltStack != null && !ThutWearables.renderBlacklist.contains(8))
        {
            if (beltStack.customOffsets())
            {
                beltStack.renderWearable(EnumWearable.WAIST, wearer, worn.getWearable(EnumWearable.WAIST),
                        partialTicks);
            }
            else
            {
                GL11.glPushMatrix();
                theModel.bipedBody.postRender(0.0625F);
                if (wearer.isSneaking())
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
                beltStack.renderWearable(EnumWearable.WAIST, wearer, worn.getWearable(EnumWearable.WAIST),
                        partialTicks);
                GL11.glPopMatrix();
            }
        }
        if (hat != null || leftEar != null || rightEar != null || eyes != null)
        {
            if (hat != null && hat.customOffsets())
            {
                hat.renderWearable(EnumWearable.HAT, wearer, worn.getWearable(EnumWearable.HAT), partialTicks);
                hat = null;
            }
            if (eyes != null && eyes.customOffsets())
            {
                eyes.renderWearable(EnumWearable.EYE, wearer, worn.getWearable(EnumWearable.EYE), partialTicks);
                eyes = null;
            }
            if (leftEar != null && leftEar.customOffsets())
            {
                leftEar.renderWearable(EnumWearable.EAR, wearer, worn.getWearable(EnumWearable.EAR, 1), partialTicks);
                leftEar = null;
            }
            if (rightEar != null && rightEar.customOffsets())
            {
                rightEar.renderWearable(EnumWearable.EAR, wearer, worn.getWearable(EnumWearable.EAR, 0), partialTicks);
                rightEar = null;
            }

            GlStateManager.pushMatrix();
            if (wearer.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
                if ((offsetArr = ThutWearables.renderOffsetsSneak.get(9)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
            }
            if (wearer.isChild())
            {
                float af = 2.0F;
                float af1 = 1.4F;
                GlStateManager.translate(0.0F, 0.5F * scale, 0.0F);
                GlStateManager.scale(af1 / af, af1 / af, af1 / af);
                GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
            }
            theModel.bipedHead.postRender(0.0625F);
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
                rightEar.renderWearable(EnumWearable.EAR, wearer, worn.getWearable(EnumWearable.EAR, 0), partialTicks);
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
                GlStateManager.scale(-1, 1, 1);
                leftEar.renderWearable(EnumWearable.EAR, wearer, worn.getWearable(EnumWearable.EAR, 1), partialTicks);
                GlStateManager.popMatrix();
            }
            if (eyes != null && !ThutWearables.renderBlacklist.contains(11))
            {
                GlStateManager.pushMatrix();
                if ((offsetArr = ThutWearables.renderOffsets.get(11)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
                eyes.renderWearable(EnumWearable.EYE, wearer, worn.getWearable(EnumWearable.EYE), partialTicks);
                GlStateManager.popMatrix();
            }
            if (hat != null && !ThutWearables.renderBlacklist.contains(12))
            {
                GlStateManager.pushMatrix();
                if ((offsetArr = ThutWearables.renderOffsets.get(12)) != null)
                {
                    GlStateManager.translate(offsetArr[0], offsetArr[1], offsetArr[2]);
                }
                hat.renderWearable(EnumWearable.HAT, wearer, worn.getWearable(EnumWearable.HAT), partialTicks);
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

}
