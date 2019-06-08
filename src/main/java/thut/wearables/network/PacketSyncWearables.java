package thut.wearables.network;

import java.io.IOException;

import javax.xml.ws.handler.MessageContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class PacketSyncWearables implements IMessage, IMessageHandler<PacketSyncWearables, IMessage>
{
    NBTTagCompound data;

    public PacketSyncWearables()
    {
        data = new NBTTagCompound();
    }

    public PacketSyncWearables(EntityLivingBase player)
    {
        this();
        data.setInteger("I", player.getEntityId());
        PlayerWearables cap = ThutWearables.getWearables(player);
        cap.writeToNBT(data);
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        new PacketBuffer(buffer).writeCompoundTag(data);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        try
        {
            data = new PacketBuffer(buffer).readCompoundTag();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(final PacketSyncWearables message, MessageContext ctx)
    {
        Minecraft.getMinecraft().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                processMessage(message);
            }
        });
        return null;
    }

    @SideOnly(Side.CLIENT)
    void processMessage(PacketSyncWearables message)
    {
        World world = Minecraft.getMinecraft().world;
        if (world == null) return;
        Entity p = world.getEntityByID(message.data.getInteger("I"));
        if (p != null && p instanceof EntityLivingBase)
        {
            PlayerWearables cap = ThutWearables.getWearables((EntityLivingBase) p);
            cap.readFromNBT(message.data);
        }
        return;
    }

}
