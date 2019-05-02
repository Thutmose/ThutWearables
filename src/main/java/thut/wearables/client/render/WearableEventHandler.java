package thut.wearables.client.render;

import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Sets;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.network.PacketGui;

public class WearableEventHandler
{
    private Set<RenderLivingBase<?>> addedBaubles = Sets.newHashSet();
    KeyBinding                       toggleGui;
    KeyBinding[]                     keys         = new KeyBinding[13];

    public WearableEventHandler()
    {
        toggleGui = new KeyBinding("Toggle Wearables Gui", Keyboard.KEY_NONE, "Wearables");
        ClientRegistry.registerKeyBinding(toggleGui);
        for (int i = 0; i < 13; i++)
        {
            EnumWearable slot = EnumWearable.getWearable(i);
            int subIndex = EnumWearable.getSubIndex(i);
            String name = "Activate ";
            if (slot.slots == 1)
            {
                name = name + " " + slot;
            }
            else
            {
                name = name + " " + slot + " " + subIndex;
            }
            int key = slot == EnumWearable.BACK ? Keyboard.KEY_B : Keyboard.KEY_NONE;
            keys[i] = new KeyBinding(name, key, "Wearables");
            ClientRegistry.registerKeyBinding(keys[i]);
        }
    }

    @SubscribeEvent
    public void keyPress(KeyInputEvent event)
    {
        for (byte i = 0; i < 13; i++)
        {
            KeyBinding key = keys[i];
            if (key.isPressed())
            {
                PacketGui packet = new PacketGui();
                packet.data.setByte("S", i);
                ThutWearables.packetPipeline.sendToServer(packet);
            }
        }
        if (toggleGui.isPressed())
        {
            PacketGui packet = new PacketGui();
            ThutWearables.packetPipeline.sendToServer(packet);
        }
    }

    @SubscribeEvent
    public void addWearableRenderLayer(RenderLivingEvent.Post<?> event)
    {
        // Only apply to model bipeds.
        if (!(event.getRenderer().getMainModel() instanceof ModelBiped)) return;
        // Only one layer per renderer.
        if (addedBaubles.contains(event.getRenderer())) { return; }

        // Add the layer.
        List<LayerRenderer<?>> layerRenderers = ReflectionHelper.getPrivateValue(RenderLivingBase.class,
                event.getRenderer(), "layerRenderers", "field_177097_h", "i");
        int index = Math.min(1, layerRenderers.size());
        layerRenderers.add(index, new WearablesRenderer(event.getRenderer()));
        addedBaubles.add(event.getRenderer());
    }
}
