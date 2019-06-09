package thut.wearables.network;

import java.io.IOException;

import javax.xml.ws.handler.MessageContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;

public class PacketGui implements IMessage, IMessageHandler<PacketGui, IMessage>
{
    public CompoundNBT data;

    public PacketGui()
    {
        data = new CompoundNBT();
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        PacketBuffer buf = new PacketBuffer(buffer);
        buf.writeCompoundTag(data);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        PacketBuffer buf = new PacketBuffer(buffer);
        try
        {
            data = buf.readCompoundTag();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public IMessage onMessage(final PacketGui message, final MessageContext ctx)
    {
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                processMessage(ctx.getServerHandler().player, message);
            }
        });
        return null;
    }

    static void processMessage(ServerPlayerEntity player, PacketGui message)
    {
        if (message.data.hasNoTags())
        {
            player.openGui(ThutWearables.instance, -1, player.getEntityWorld(), 0, 0, 0);
            return;
        }
        if (message.data.hasKey("w_open_target_"))
        {
            player.openGui(ThutWearables.instance, message.data.getInt("w_open_target_"), player.getEntityWorld(),
                    0, 0, 0);
            return;
        }
        byte slot = message.data.getByte("S");
        ItemStack stack = ThutWearables.getWearables(player).getStackInSlot(slot);
        if (stack != null) EnumWearable.interact(player, stack, slot);
    }
}