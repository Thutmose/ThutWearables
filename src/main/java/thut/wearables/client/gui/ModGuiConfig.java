package thut.wearables.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import thut.wearables.ThutWearables;

public class ModGuiConfig extends GuiConfig
{
    public ModGuiConfig(GuiScreen guiScreen)
    {
        super(guiScreen, getConfigElements(), ThutWearables.MODID, false, false,
                GuiConfig.getAbridgedConfigPath(ThutWearables.configPath));
    }

    public static List<IConfigElement> getConfigElements()
    {
        List<IConfigElement> list = new ArrayList<>();
        for (String cat : ThutWearables.config.getCategoryNames())
        {
            ConfigCategory cc = ThutWearables.config.getCategory(cat);
            if (!cc.isChild())
            {
                ConfigElement ce = new ConfigElement(cc);
                list.add(ce);
            }
        }
        return list;
    }
}
