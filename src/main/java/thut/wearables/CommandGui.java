package thut.wearables;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import thut.wearables.network.MouseOverPacket;

public class CommandGui extends CommandBase
{
    public static String PERMWEARABLESCMD = "wearables.open.other.command";
    static
    {
        PermissionAPI.registerNode(PERMWEARABLESCMD, DefaultPermissionLevel.OP,
                "Whether the player can open the wearables gui of others via the command.");
    }

    @Override
    public String getName()
    {
        return "wearables";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/wearables (while looking at target) or /wearables @p";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayerMP user = getCommandSenderAsPlayer(sender);
        if (!PermissionAPI.hasPermission(user, PERMWEARABLESCMD))
            throw new CommandException("wearables.command.fail.noperms");
        if (args.length == 0) ThutWearables.packetPipeline.sendTo(new MouseOverPacket(), user);
        else
        {
            Entity target = getEntity(server, sender, args[0]);
            if (target.hasCapability(IActiveWearable.WEARABLE_CAP, null) || target instanceof EntityPlayerMP)
            {

                user.openGui(ThutWearables.instance, target.getEntityId(), user.getEntityWorld(), 0, 0, 0);
            }
            else throw new CommandException("wearables.command.fail.targetted");
        }

    }

}
