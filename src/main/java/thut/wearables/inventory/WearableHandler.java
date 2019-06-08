package thut.wearables.inventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.FMLCommonHandler;

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
        if (FMLCommonHandler.instance().getEffectiveSide() == Dist.DEDICATED_SERVER)
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
                    CompoundNBT CompoundNBT = CompressedStreamTools.readCompressed(fileinputstream);
                    fileinputstream.close();
                    wearables.readFromNBT(CompoundNBT.getCompound("Data"));
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
