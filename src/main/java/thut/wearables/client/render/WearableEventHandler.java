package thut.wearables.client.render;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.java.games.input.Keyboard;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.IWearable;
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

        Map<Integer, Integer> defaults = Maps.newHashMap();
        defaults.put(7, Keyboard.KEY_E);
        defaults.put(2, Keyboard.KEY_Z);
        defaults.put(3, Keyboard.KEY_X);

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
            int key = defaults.containsKey(slot) ? defaults.get(slot) : Keyboard.KEY_NONE;
            keys[i] = defaults.containsKey(slot) ? new KeyBinding(name, key, "Wearables")
                    : new KeyBinding(name, KeyConflictContext.UNIVERSAL, KeyModifier.ALT, key, "Wearables");

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
    public void onToolTip(ItemTooltipEvent evt)
    {
        if (evt.getItemStack().hasCapability(IActiveWearable.WEARABLE_CAP, null)
                || evt.getItemStack().getItem() instanceof IWearable)
        {
            IWearable wear = evt.getItemStack().getCapability(IActiveWearable.WEARABLE_CAP, null);
            if (wear == null) wear = (IWearable) evt.getItemStack().getItem();
            EnumWearable slot = wear.getSlot(evt.getItemStack());
            String key = keys[slot.index].getDisplayName();
            String message = "";
            switch (slot.slots)
            {
            case 2:
                message = I18n.format("wearables.keyuse.left", key);
                evt.getToolTip().add(message);
                key = keys[slot.index + 1].getDisplayName();
                message = I18n.format("wearables.keyuse.right", key);
                evt.getToolTip().add(message);
                break;
            default:
                message = I18n.format("wearables.keyuse.single", key);
                evt.getToolTip().add(message);
                break;
            }
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
