package thut.wearables.network;

import java.io.IOException;

import javax.xml.ws.handler.MessageContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class PacketSyncWearables implements IMessage, IMessageHandler<PacketSyncWearables, IMessage>
{
    CompoundNBT data;

    public PacketSyncWearables()
    {
        data = new CompoundNBT();
    }

    public PacketSyncWearables(LivingEntity player)
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public IMessage onMessage(final PacketSyncWearables message, MessageContext ctx)
    {
        Minecraft.getInstance().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                processMessage(message);
            }
        });
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    void processMessage(PacketSyncWearables message)
    {
        World world = Minecraft.getInstance().world;
        if (world == null) return;
        Entity p = world.getEntityByID(message.data.getInt("I"));
        if (p != null && p instanceof LivingEntity)
        {
            PlayerWearables cap = ThutWearables.getWearables((LivingEntity) p);
            cap.readFromNBT(message.data);
        }
        return;
    }

}
