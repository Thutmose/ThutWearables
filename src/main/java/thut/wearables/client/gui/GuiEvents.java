package thut.wearables.client.gui;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.wearables.ThutWearables;
import thut.wearables.network.PacketGui;

public class GuiEvents
{
    public static Map<String, int[]> whitelistedGuis = Maps.newHashMap();

    static
    {
        whitelistedGuis.put("net.minecraft.client.gui.inventory.GuiInventory", new int[2]);
    }

    public static void init()
    {
        MinecraftForge.EVENT_BUS.register(new GuiEvents());
    }

    private boolean active = false;

    public GuiEvents()
    {
    }

    @OnlyIn(value = Dist.CLIENT)
    @SubscribeEvent
    public void guiPostInit(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if (!ThutWearables.hasButton) return;
        if (event.getGui() instanceof GuiInventory || event.getGui() instanceof GuiWearables)
        {
            active = event.getGui() instanceof GuiWearables;
            GuiContainer gui = (GuiContainer) event.getGui();
            GuiWearableButton button;
            event.getButtonList()
                    .add(button = new GuiWearableButton(56, gui.guiLeft, gui.guiTop, ThutWearables.buttonPos[0],
                            ThutWearables.buttonPos[1], 10, 10,
                            I18n.format(active ? "button.wearables.off" : "button.wearables.on", new Object[0])));
            button.packedFGColour = 0xFFFF00FF;
        }
        else if (event.getGui() instanceof GuiContainerCreative)
        {
            GuiContainerCreative gui = (GuiContainerCreative) event.getGui();
            active = event.getGui() instanceof GuiWearables;
            GuiWearableButton button;
            event.getButtonList().add(button = new GuiWearableButton(56, gui.guiLeft, gui.guiTop, 37, 9, 10, 10,
                    I18n.format(active ? "button.wearables.off" : "button.wearables.on", new Object[0])));
            button.packedFGColour = 0xFFFF00FF;
            button.visible = button.enabled = gui.getSelectedTabIndex() == 11;
        }
    }

    @OnlyIn(value = Dist.CLIENT)
    @SubscribeEvent
    public void guiPostAction(GuiScreenEvent.ActionPerformedEvent.Post event)
    {
        if (event.getGui() instanceof GuiInventory || event.getGui() instanceof GuiContainerCreative
                || event.getGui() instanceof GuiWearables)
        {
            if (event.getButton() instanceof GuiWearableButton)
            {
                active = event.getGui() instanceof GuiWearables;
                if (active)
                {
                    event.getGui().mc.displayGuiScreen(new GuiInventory(event.getGui().mc.player));
                }
                else
                {
                    PacketGui packet = new PacketGui();
                    ThutWearables.packetPipeline.sendToServer(packet);
                }
                active = !active;
                event.getButton().displayString = I18n.format(active ? "button.wearables.off" : "button.wearables.on",
                        new Object[0]);
                return;
            }
        }
    }
}
