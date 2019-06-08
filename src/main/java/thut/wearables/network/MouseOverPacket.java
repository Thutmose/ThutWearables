package thut.wearables.network;

import javax.xml.ws.handler.MessageContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public class MouseOverPacket implements IMessage
{

    /** Required default constructor. */
    public MouseOverPacket()
    {
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
    }

    public static class Handler implements IMessageHandler<MouseOverPacket, IMessage>
    {

        @Override
        public IMessage onMessage(MouseOverPacket message, MessageContext ctx)
        {
            RayTraceResult pos = Minecraft.getInstance().objectMouseOver;
            if (pos != null)
            {
                if (pos.entityHit != null)
                {
                    int id = pos.entityHit.getEntityId();
                    PacketGui packet = new PacketGui();
                    packet.data.setInteger("w_open_target_", id);
                    return packet;
                }
                else
                {
                    Minecraft.getInstance().player.sendMessage(new TranslationTextComponent("wearables.other.fail"));
                }
            }
            return null;
        }
    }
}
