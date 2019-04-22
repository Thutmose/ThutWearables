package thut.wearables;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.GameData;
import thut.wearables.CompatClass.Phase;
import thut.wearables.client.gui.GuiEvents;
import thut.wearables.client.gui.GuiWearables;
import thut.wearables.client.render.WearableEventHandler;
import thut.wearables.impl.ConfigWearable;
import thut.wearables.inventory.ContainerWearables;
import thut.wearables.inventory.IWearableInventory;
import thut.wearables.inventory.PlayerWearables;
import thut.wearables.inventory.WearableHandler;
import thut.wearables.network.PacketGui;
import thut.wearables.network.PacketSyncWearables;

@Mod(modid = ThutWearables.MODID, name = "Thut Wearables", acceptableRemoteVersions = Reference.MINVERSION, version = ThutWearables.VERSION, guiFactory = ThutWearables.CONFIGGUI)
public class ThutWearables
{
    public static final String MODID     = Reference.MODID;
    public static final String VERSION   = Reference.VERSION;
    public static final String CONFIGGUI = "thut.wearables.client.gui.ModGuiFactory";

    public static PlayerWearables getWearables(EntityLivingBase wearer)
    {
        PlayerWearables wearables = WearableHandler.getPlayerData(wearer.getCachedUniqueIdString());
        if (wearer.hasCapability(WearableHandler.WEARABLES_CAP, null))
        {
            IWearableInventory inven = wearer.getCapability(WearableHandler.WEARABLES_CAP, null);
            if (inven instanceof PlayerWearables)
            {
                PlayerWearables ret = (PlayerWearables) inven;
                if (wearables != null) ret.readFromNBT(wearables.writeToNBT(new NBTTagCompound()));
                return ret;
            }
        }
        return wearables;
    }

    public static SimpleNetworkWrapper                    packetPipeline         = new SimpleNetworkWrapper(MODID);

    @SidedProxy
    public static CommonProxy                             proxy;
    @Instance(value = MODID)
    public static ThutWearables                           instance;

    private boolean                                       overworldRules         = true;
    Map<CompatClass.Phase, Set<java.lang.reflect.Method>> initMethods            = Maps.newHashMap();

    Map<ResourceLocation, EnumWearable>                   configWearables        = Maps.newHashMap();

    public static Map<Integer, float[]>                   renderOffsets          = Maps.newHashMap();
    public static Map<Integer, float[]>                   renderOffsetsSneak     = Maps.newHashMap();
    public static int[]                                   buttonPos              = { 26, 9 };
    public static boolean                                 hasButton              = true;
    public static Set<Integer>                            renderBlacklist        = Sets.newHashSet();
    public static String                                  configPath;
    public static Configuration                           config;
    public static boolean                                 baublesCompat          = true;

    /** Cache of wearables for players that die when keep inventory is on. */
    Map<UUID, PlayerWearables>                            player_inventory_cache = Maps.newHashMap();
    Set<UUID>                                             toKeep                 = Sets.newHashSet();

    public ThutWearables()
    {
        for (Phase phase : Phase.values())
        {
            initMethods.put(phase, new HashSet<java.lang.reflect.Method>());
        }
        CompatParser.findClasses("thut.wearables.compat", initMethods);
        doPhase(Phase.CONSTRUCT, null);
    }

    @EventHandler
    public void init(FMLInitializationEvent evt)
    {
        doPhase(Phase.INIT, evt);
    }

    @EventHandler
    public void init(FMLPostInitializationEvent evt)
    {
        doPhase(Phase.POST, evt);
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
        baublesCompat = config.getBoolean("baublesCompat", "compat", baublesCompat,
                "Should thutwearables attempt to make baubles wearables.");
        String[] otherWearables = config.getStringList("customWearables", "general",
                new String[] { "wearablebackpacks:backpack>BACK" }, "Other mod's items that can be worn.");
        for (String s : otherWearables)
        {
            try
            {
                String[] args = s.split(">");
                ResourceLocation resource = new ResourceLocation(args[0]);
                EnumWearable slot = null;
                try
                {
                    slot = EnumWearable.valueOf(args[1]);
                }
                catch (IllegalArgumentException e)
                {
                    // Ignore this, we put null in the map fine.
                    System.out.println(resource
                            + " has been added as a config wearable which requires an NBT tag to specify which slot it fits in.");
                }
                configWearables.put(resource, slot);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
        buttonPos = config.get("general", "buttonPos", buttonPos, "Position of the button on the inventory screen.")
                .getIntList();
        hasButton = config.get("general", "hasButton", hasButton, "if false, there will be no button for gui.")
                .getBoolean(true);
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
        doPhase(Phase.PRE, e);
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
        }, IActiveWearable.Default::new);
        CapabilityManager.INSTANCE.register(IWearableInventory.class, new Capability.IStorage<IWearableInventory>()
        {
            @Override
            public NBTBase writeNBT(Capability<IWearableInventory> capability, IWearableInventory instance,
                    EnumFacing side)
            {
                if (instance instanceof PlayerWearables)
                    return ((PlayerWearables) instance).writeToNBT(new NBTTagCompound());
                return null;
            }

            @Override
            public void readNBT(Capability<IWearableInventory> capability, IWearableInventory instance, EnumFacing side,
                    NBTBase nbt)
            {
                if (instance instanceof PlayerWearables && nbt instanceof NBTTagCompound)
                    ((PlayerWearables) instance).readFromNBT((NBTTagCompound) nbt);
            }
        }, PlayerWearables::new);
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
    public void PlayerLoggedOutEvent(PlayerLoggedOutEvent event)
    {
        syncSchedule.remove(event.player.getUniqueID());
        player_inventory_cache.remove(event.player.getUniqueID());
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void preDrop(LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof EntityPlayerMP)) return;
        EntityPlayer player = (EntityPlayer) event.getEntity();
        GameRules rules = overworldRules ? player.getServer().getEntityWorld().getGameRules()
                : player.getEntityWorld().getGameRules();
        if (rules.getBoolean("keepInventory"))
        {
            PlayerWearables cap = ThutWearables.getWearables(player);
            player_inventory_cache.put(player.getUniqueID(), cap);
            toKeep.add(player.getUniqueID());
            return;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void dropLoot(PlayerDropsEvent event)
    {
        EntityPlayer player = event.getEntityPlayer();
        GameRules rules = overworldRules ? player.getServer().getWorld(0).getGameRules()
                : player.getEntityWorld().getGameRules();
        PlayerWearables cap = ThutWearables.getWearables(player);
        if (rules.getBoolean("keepInventory")) return;

        for (int i = 0; i < 13; i++)
        {
            ItemStack stack = cap.getStackInSlot(i);
            if (stack != null)
            {
                EnumWearable.takeOff(player, stack, i);
                double d0 = player.posY - 0.3D + player.getEyeHeight();
                EntityItem drop = new EntityItem(player.getEntityWorld(), player.posX, d0, player.posZ, stack);
                float f = player.getRNG().nextFloat() * 0.5F;
                float f1 = player.getRNG().nextFloat() * ((float) Math.PI * 2F);
                drop.motionX = (double) (-MathHelper.sin(f1) * f);
                drop.motionZ = (double) (MathHelper.cos(f1) * f);
                drop.motionY = 0.20000000298023224D;
                event.getDrops().add(drop);
                cap.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        syncWearables(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void respawn(PlayerRespawnEvent event)
    {
        EntityPlayer wearer = (EntityPlayer) event.player;
        if (wearer instanceof EntityPlayerMP && (toKeep.contains(wearer.getUniqueID()) || event.isEndConquered()))
        {
            NBTTagCompound tag = player_inventory_cache.get(wearer.getUniqueID()).serializeNBT();
            PlayerWearables wearables = getWearables(wearer);
            wearables.deserializeNBT(tag);
            toKeep.remove(wearer.getUniqueID());
            syncWearables(wearer);
        }
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
            if (wearer instanceof EntityPlayerMP) player_inventory_cache.put(wearer.getUniqueID(), wearables);
        }
        if (event.getEntityLiving().getEntityWorld().isRemote) return;
        if (event.getEntity() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            if (!syncSchedule.isEmpty() && syncSchedule.contains(player.getUniqueID()) && player.ticksExisted > 20)
            {
                syncWearables(player);
                for (EntityPlayer player2 : event.getEntity().getEntityWorld().playerEntities)
                {
                    packetPipeline.sendTo(new PacketSyncWearables(player2), (EntityPlayerMP) player);
                }
                syncSchedule.remove(player.getUniqueID());
            }
        }
    }

    @SubscribeEvent
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof EntityLivingBase)
        {
            event.addCapability(new ResourceLocation(MODID, "wearables"), new PlayerWearables());
        }
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(AttachCapabilitiesEvent<ItemStack> event)
    {
        ResourceLocation loc = event.getObject().getItem().getRegistryName();
        if (configWearables.containsKey(loc))
        {
            EnumWearable slot = configWearables.get(loc);
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

    @SubscribeEvent
    public void initRecipes(RegistryEvent.Register<IRecipe> evt)
    {
        IRecipe recipe = new RecipeDye().setRegistryName(new ResourceLocation(MODID, "dye"));
        GameData.register_impl(recipe);
    }

    public static void syncWearables(EntityPlayer player)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            Thread.dumpStack();
            return;
        }
        packetPipeline.sendToAll(new PacketSyncWearables(player));
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