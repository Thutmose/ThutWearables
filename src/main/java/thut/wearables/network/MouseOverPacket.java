package thut.wearables.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
            RayTraceResult pos = Minecraft.getMinecraft().objectMouseOver;
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
                    Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation("wearables.other.fail"));
                }
            }
            return null;
        }
    }
}
