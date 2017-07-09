package thut.wearables;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import thut.wearables.CompatClass.Phase;
import thut.wearables.client.gui.GuiEvents;
import thut.wearables.client.gui.GuiWearables;
import thut.wearables.client.render.WearableEventHandler;
import thut.wearables.impl.ConfigWearable;
import thut.wearables.inventory.ContainerWearables;
import thut.wearables.inventory.PlayerWearables;
import thut.wearables.inventory.WearableHandler;
import thut.wearables.network.PacketGui;
import thut.wearables.network.PacketSyncWearables;

@Mod(modid = ThutWearables.MODID, name = "Thut Wearables", version = ThutWearables.VERSION, guiFactory = ThutWearables.CONFIGGUI)
public class ThutWearables
{
    public static final String MODID     = Reference.MODID;
    public static final String VERSION   = Reference.VERSION;
    public static final String CONFIGGUI = "thut.wearables.client.gui.ModGuiFactory";

    public static PlayerWearables getWearables(EntityLivingBase wearer)
    {
        return WearableHandler.getInstance().getPlayerData(wearer.getCachedUniqueIdString());
    }

    public static void saveWearables(EntityLivingBase wearer)
    {
        WearableHandler.getInstance().save(wearer.getCachedUniqueIdString());
    }

    public static SimpleNetworkWrapper                    packetPipeline     = new SimpleNetworkWrapper(MODID);

    @SidedProxy
    public static CommonProxy                             proxy;
    @Instance(value = MODID)
    public static ThutWearables                           instance;

    private boolean                                       overworldRules     = true;
    Map<CompatClass.Phase, Set<java.lang.reflect.Method>> initMethods        = Maps.newHashMap();

    Map<ResourceLocation, EnumWearable>                   configWearables    = Maps.newHashMap();

    public static Map<Integer, float[]>                   renderOffsets      = Maps.newHashMap();
    public static Map<Integer, float[]>                   renderOffsetsSneak = Maps.newHashMap();
    public static Set<Integer>                            renderBlacklist    = Sets.newHashSet();
    public static String                                  configPath;
    public static Configuration                           config;

    public ThutWearables()
    {
        for (Phase phase : Phase.values())
        {
            initMethods.put(phase, new HashSet<java.lang.reflect.Method>());
        }
    }

    public void init(FMLInitializationEvent evt)
    {
        doPhase(Phase.INIT, evt);
    }

    private void doPhase(Phase pre, Object event)
    {
        for (java.lang.reflect.Method m : initMethods.get(pre))
        {
            try
            {
                CompatClass comp = m.getAnnotation(CompatClass.class);
                if (comp.takesEvent()) m.invoke(null, event);
                else m.invoke(null);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void handleConfig(boolean load)
    {
        if (load) config.load();
        renderOffsets.clear();
        renderBlacklist.clear();
        renderOffsetsSneak.clear();
        overworldRules = config.getBoolean("overworldGamerules", "general", overworldRules,
                "whether to use overworld gamerules for keep inventory");
        String[] otherWearables = config.getStringList("customWearables", "general",
                new String[] { "wearablebackpacks:backpack>BACK" }, "Other mod's items that can be worn.");
        for (String s : otherWearables)
        {
            try
            {
                String[] args = s.split(">");
                ResourceLocation resource = new ResourceLocation(args[0]);
                EnumWearable slot = EnumWearable.valueOf(args[1]);
                configWearables.put(resource, slot);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
        for (int i = 0; i < EnumWearable.BYINDEX.length; i++)
        {
            float[] offset = new float[3];
            float[] offsetSneak = new float[3];
            try
            {
                boolean blacklist = config.getBoolean("noRender_" + i, "client", false,
                        "Do not render " + EnumWearable.BYINDEX[i].name());
                if (blacklist) renderBlacklist.add(i);
                String[] offsetArr = config
                        .getString("offset_" + i, "client", "0,0,0", "Offset for " + EnumWearable.BYINDEX[i].name())
                        .split(",");
                offset[0] = Float.parseFloat(offsetArr[0]);
                offset[1] = Float.parseFloat(offsetArr[1]);
                offset[2] = Float.parseFloat(offsetArr[2]);
                if (offset[0] == 0 && offset[1] == 0 && offset[2] == 0) offset = null;
                if (i < 9)
                {
                    offsetArr = config.getString("offset_sneaking_" + i, "client", "0,0,0",
                            "Offset for when sneaking for " + EnumWearable.BYINDEX[i].name()).split(",");
                    offsetSneak[0] = Float.parseFloat(offsetArr[0]);
                    offsetSneak[1] = Float.parseFloat(offsetArr[1]);
                    offsetSneak[2] = Float.parseFloat(offsetArr[2]);
                }
                else if (i == 9)
                {
                    offsetArr = config.getString("offset_sneaking_" + i, "client", "0,0,0",
                            "Offset for when sneaking for things on head").split(",");
                    offsetSneak[0] = Float.parseFloat(offsetArr[0]);
                    offsetSneak[1] = Float.parseFloat(offsetArr[1]);
                    offsetSneak[2] = Float.parseFloat(offsetArr[2]);
                }
                if (offsetSneak[0] == 0 && offsetSneak[1] == 0 && offsetSneak[2] == 0) offsetSneak = null;
            }
            catch (Exception e1)
            {
                offset = null;
                e1.printStackTrace();
            }
            if (FMLCommonHandler.instance().getSide() == Side.CLIENT && offset != null) renderOffsets.put(i, offset);
            if (FMLCommonHandler.instance().getSide() == Side.CLIENT && offsetSneak != null)
                renderOffsetsSneak.put(i, offsetSneak);
        }

        config.save();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        proxy.preInit(e);
        config = new Configuration(e.getSuggestedConfigurationFile());
        configPath = config.getConfigFile().getAbsolutePath();
        handleConfig(true);
        packetPipeline.registerMessage(PacketGui.class, PacketGui.class, 1, Side.SERVER);
        packetPipeline.registerMessage(PacketSyncWearables.class, PacketSyncWearables.class, 2, Side.CLIENT);
        MinecraftForge.EVENT_BUS.register(this);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
        CapabilityManager.INSTANCE.register(IActiveWearable.class, new Capability.IStorage<IActiveWearable>()
        {
            @Override
            public NBTBase writeNBT(Capability<IActiveWearable> capability, IActiveWearable instance, EnumFacing side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<IActiveWearable> capability, IActiveWearable instance, EnumFacing side,
                    NBTBase nbt)
            {
            }
        }, new IActiveWearable()
        {
            @Override
            public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
            {
            }

            @Override
            public EnumWearable getSlot(ItemStack stack)
            {
                return null;
            }

            @Override
            public void onUpdate(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
            {
            }

            @Override
            public void onTakeOff(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
            {
            }

            @Override
            public void onPutOn(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
            {
            }
        }.getClass());
    }

    static HashSet<UUID> syncSchedule = new HashSet<UUID>();

    @SubscribeEvent
    public void PlayerLoggedInEvent(PlayerLoggedInEvent event)
    {
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.SERVER)
        {
            syncSchedule.add(event.player.getUniqueID());
        }
    }

    @SubscribeEvent
    public void startTracking(StartTracking event)
    {
        if (event.getTarget() instanceof EntityPlayer && event.getEntityPlayer().isServerWorld())
        {
            packetPipeline.sendTo(new PacketSyncWearables((EntityPlayer) event.getTarget()),
                    (EntityPlayerMP) event.getEntityPlayer());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void dropLoot(PlayerDropsEvent event)
    {
        EntityPlayer player = event.getEntityPlayer();
        GameRules rules = overworldRules ? player.getServer().getWorld(0).getGameRules()
                : player.getEntityWorld().getGameRules();
        if (rules.getBoolean("keepInventory")) return;
        PlayerWearables cap = ThutWearables.getWearables(player);
        for (int i = 0; i < 13; i++)
        {
            ItemStack stack = cap.getStackInSlot(i);
            if (stack != null)
            {
                player.dropItem(stack.copy(), true, false);
                EnumWearable.takeOff(player, stack, i);
                cap.setInventorySlotContents(i, CompatWrapper.nullStack);
            }
        }
        syncWearables(player);
    }

    @SubscribeEvent
    public void playerTick(LivingUpdateEvent event)
    {
        if (event.getEntity() instanceof EntityPlayer)
        {
            EntityPlayer wearer = (EntityPlayer) event.getEntity();
            PlayerWearables wearables = getWearables(wearer);
            for (int i = 0; i < 13; i++)
            {
                EnumWearable.tick(wearer, wearables.getStackInSlot(i), i);
            }
        }
        if (event.getEntityLiving().world.isRemote) return;
        if (event.getEntity() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.getEntity();

            if (!syncSchedule.isEmpty() && syncSchedule.contains(player.getUniqueID()) && player.ticksExisted > 20)
            {
                syncWearables(player);
                for (EntityPlayer player2 : event.getEntity().world.playerEntities)
                {
                    packetPipeline.sendTo(new PacketSyncWearables(player2), (EntityPlayerMP) player);
                }
                syncSchedule.remove(player.getUniqueID());
            }
        }
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(AttachCapabilitiesEvent<ItemStack> event)
    {
        ResourceLocation loc = event.getObject().getItem().getRegistryName();
        EnumWearable slot = configWearables.get(loc);
        if (slot != null)
        {
            event.addCapability(new ResourceLocation(MODID, "configwearable"), new ConfigWearable(slot));
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
    {
        if (eventArgs.getModID().equals(Reference.MODID))
        {
            handleConfig(false);
        }
    }

    public static void syncWearables(EntityPlayer player)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            Thread.dumpStack();
            return;
        }
        packetPipeline.sendToAll(new PacketSyncWearables(player));
        saveWearables(player);
    }

    public static class CommonProxy implements IGuiHandler
    {
        public void preInit(FMLPreInitializationEvent event)
        {
        }

        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return new ContainerWearables(player);
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return null;
        }
    }

    public static class ServerProxy extends CommonProxy
    {
        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return super.getServerGuiElement(ID, player, world, x, y, z);
        }
    }

    public static class ClientProxy extends CommonProxy
    {
        @Override
        public void preInit(FMLPreInitializationEvent event)
        {
            GuiEvents.init();
            MinecraftForge.EVENT_BUS.register(new WearableEventHandler());
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return new GuiWearables(player);
        }
    }
}
