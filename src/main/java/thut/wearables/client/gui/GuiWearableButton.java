package thut.wearables.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;

public class GuiWearableButton extends GuiButton
{
    private final int guiLeft;

    public GuiWearableButton(int buttonId, int guiLeft, int guiTop, int x, int y, int width, int height,
            String buttonText)
    {
        super(buttonId, guiLeft + x, guiTop + y, width, height, buttonText);
        this.guiLeft = guiLeft;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        int potionShift = getPotionShift(mc);
        return super.mousePressed(mc, mouseX - potionShift, mouseY);
    }

    @Override
    public void drawButton(Minecraft mc, int xx, int yy, float partialTicks)
    {
        if (this.visible)
        {
            int potionShift = getPotionShift(mc);

            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(GuiWearables.background);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = xx >= this.x + potionShift && yy >= this.y
                    && xx < this.x + this.width + potionShift && yy < this.y + this.height;
            int k = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            if (k == 1)
            {
                this.drawTexturedModalRect(this.x + potionShift + 1, this.y, 0, 247, 9, 9);
            }
            else
            {
                this.drawTexturedModalRect(this.x + potionShift + 1, this.y, 9, 247, 9, 9);
                this.drawCenteredString(fontrenderer, this.displayString, this.x + 5 + potionShift,
                        this.y + this.height, this.packedFGColour);
            }

            this.mouseDragged(mc, xx, yy);

        }
    }

    private int getPotionShift(Minecraft mc)
    {
        if (mc.currentScreen instanceof GuiContainer)
        {
            GuiContainer guiContainer = (GuiContainer) mc.currentScreen;
            return this.guiLeft - guiContainer.guiLeft;
        }
        return 0;
    }
}