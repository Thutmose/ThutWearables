package thut.wearables.inventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class WearableHandler
{
    @CapabilityInject(IWearableInventory.class)
    public static final Capability<IWearableInventory> WEARABLES_CAP = null;

    public static PlayerWearables getPlayerData(String uuid)
    {
        return load(uuid);
    }

    public static PlayerWearables load(String uuid)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            PlayerWearables wearables = new PlayerWearables();
            String fileName = wearables.dataFileName();
            File file = null;
            try
            {
                file = getFileForUUID(uuid, fileName);
            }
            catch (Exception e)
            {

            }
            if (file != null && file.exists())
            {
                try
                {
                    FileInputStream fileinputstream = new FileInputStream(file);
                    NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                    fileinputstream.close();
                    wearables.readFromNBT(nbttagcompound.getCompoundTag("Data"));
                    // Cleanup the file, we don't need tihs anymore.
                    file.delete();
                    File dir = new File(file.getParentFile().getAbsolutePath());
                    if (dir.isDirectory() && dir.listFiles().length == 0)
                    {
                        dir.delete();
                    }
                    return wearables;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static File getFileForUUID(String uuid, String fileName)
    {
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
        ISaveHandler saveHandler = world.getSaveHandler();
        String seperator = System.getProperty("file.separator");
        File file = saveHandler.getMapFileFromName(uuid + seperator + fileName);
        if (!file.exists()) { return null; }
        return file;
    }
}
