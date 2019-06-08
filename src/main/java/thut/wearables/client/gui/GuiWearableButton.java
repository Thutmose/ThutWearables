package thut.wearables.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;

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
        if (mc.currentScreen instanceof GuiContainerCreative)
        {
            GuiContainerCreative gui = (GuiContainerCreative) mc.currentScreen;
            visible = enabled = gui.getSelectedTabIndex() == 11;
        }

        if (this.visible)
        {
            int potionShift = getPotionShift(mc);

            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(GuiWearables.background);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = xx >= this.x + potionShift && yy >= this.y && xx < this.x + this.width + potionShift
                    && yy < this.y + this.height;
            int k = this.getHoverState(this.hovered);

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