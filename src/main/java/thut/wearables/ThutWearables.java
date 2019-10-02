package thut.wearables;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import thut.wearables.CompatClass.Phase;
import thut.wearables.client.gui.GuiEvents;
import thut.wearables.client.gui.GuiWearables;
import thut.wearables.client.render.WearableEventHandler;
import thut.wearables.impl.ConfigWearable;
import thut.wearables.inventory.ContainerWearables;
import thut.wearables.inventory.IWearableInventory;
import thut.wearables.inventory.PlayerWearables;
import thut.wearables.inventory.WearableHandler;
import thut.wearables.network.MouseOverPacket;
import thut.wearables.network.PacketGui;
import thut.wearables.network.PacketHandler;
import thut.wearables.network.PacketSyncWearables;

@Mod(ThutWearables.MODID)
public class ThutWearables
{
    @OnlyIn(value = Dist.CLIENT)
    public static class ClientProxy extends CommonProxy
    {
        @Override
        public boolean isClientSide()
        {
            return EffectiveSide.get() == LogicalSide.CLIENT;
        }

        @Override
        public boolean isServerSide()
        {
            return EffectiveSide.get() == LogicalSide.SERVER;
        }

        @Override
        public void setup(final FMLCommonSetupEvent event)
        {
            super.setup(event);
            GuiEvents.init();
            MinecraftForge.EVENT_BUS.register(new WearableEventHandler());
        }

        @Override
        public void setupClient(final FMLClientSetupEvent event)
        {
            final ScreenManager.IScreenFactory<ContainerWearables, GuiWearables> factory = (c, i,
                    t) -> new GuiWearables(c, i);
            ScreenManager.registerFactory(ContainerWearables.TYPE, factory);
        }
    }

    public static class CommonProxy
    {
        public void finish(final FMLLoadCompleteEvent event)
        {
            ThutWearables.doPhase(Phase.FINALIZE, event);
        }

        public boolean isClientSide()
        {
            return false;
        }

        public boolean isServerSide()
        {
            return true;
        }

        public void setup(final FMLCommonSetupEvent event)
        {
            ThutWearables.doPhase(Phase.SETUP, event);
            CapabilityManager.INSTANCE.register(IActiveWearable.class, new Capability.IStorage<IActiveWearable>()
            {
                @Override
                public void readNBT(final Capability<IActiveWearable> capability, final IActiveWearable instance,
                        final Direction side, final INBT nbt)
                {
                }

                @Override
                public INBT writeNBT(final Capability<IActiveWearable> capability, final IActiveWearable instance,
                        final Direction side)
                {
                    return null;
                }
            }, IActiveWearable.Default::new);
            CapabilityManager.INSTANCE.register(IWearableInventory.class, new Capability.IStorage<IWearableInventory>()
            {
                @Override
                public void readNBT(final Capability<IWearableInventory> capability, final IWearableInventory instance,
                        final Direction side, final INBT nbt)
                {
                    if (instance instanceof PlayerWearables && nbt instanceof CompoundNBT) ((PlayerWearables) instance)
                            .readFromNBT((CompoundNBT) nbt);
                }

                @Override
                public INBT writeNBT(final Capability<IWearableInventory> capability, final IWearableInventory instance,
                        final Direction side)
                {
                    if (instance instanceof PlayerWearables) return ((PlayerWearables) instance).writeToNBT(
                            new CompoundNBT());
                    return null;
                }
            }, PlayerWearables::new);

            ThutWearables.packets.registerMessage(PacketSyncWearables.class, PacketSyncWearables::new);
            ThutWearables.packets.registerMessage(MouseOverPacket.class, MouseOverPacket::new);
            ThutWearables.packets.registerMessage(PacketGui.class, PacketGui::new);
        }

        public void setupClient(final FMLClientSetupEvent event)
        {

        }
    }

    // You can use EventBusSubscriber to automatically subscribe events on the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ThutWearables.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
        {
            event.getRegistry().register(ContainerWearables.TYPE.setRegistryName(ThutWearables.MODID, "wearables"));
        }

        @SubscribeEvent
        public static void registerRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event)
        {
            event.getRegistry().register(RecipeDye.SERIALIZER);
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void textureStitch(final TextureStitchEvent.Pre event)
        {
            if (!event.getMap().getBasePath().equals("textures")) return;
            System.out.println("Texture Stitch " + event.getMap().getBasePath());
            for (int i = 0; i < EnumWearable.BYINDEX.length; i++)
                event.addSprite(new ResourceLocation(EnumWearable.getIcon(i)));
        }
    }

    @CapabilityInject(IActiveWearable.class)
    public static final Capability<IActiveWearable>    WEARABLE_CAP  = null;
    @CapabilityInject(IWearableInventory.class)
    public static final Capability<IWearableInventory> WEARABLES_CAP = null;

    public static final String MODID = Reference.MODID;

    public final static PacketHandler packets = new PacketHandler(new ResourceLocation(Reference.MODID, "comms"),
            Reference.NETVERSION);

    public final static CommonProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(),
            () -> () -> new CommonProxy());

    public static ThutWearables instance;

    public static Map<Integer, float[]> renderOffsets = Maps.newHashMap();

    public static Map<Integer, float[]> renderOffsetsSneak = Maps.newHashMap();
    public static int[]                 buttonPos          = { 26, 9 };
    public static boolean               hasButton          = true;
    public static Set<Integer>          renderBlacklist    = Sets.newHashSet();
    public static String                configPath;
    public static boolean               baublesCompat      = false;

    static Map<CompatClass.Phase, Set<java.lang.reflect.Method>> initMethods = Maps.newHashMap();

    private static void doPhase(final Phase pre, final Object event)
    {
        for (final java.lang.reflect.Method m : ThutWearables.initMethods.get(pre))
            try
            {
                final CompatClass comp = m.getAnnotation(CompatClass.class);
                if (comp.takesEvent()) m.invoke(null, event);
                else m.invoke(null);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
            {
                e.printStackTrace();
            }
    }

    public static PlayerWearables getWearables(final LivingEntity wearer)
    {
        PlayerWearables wearables = null;
        if (wearer instanceof PlayerEntity) wearables = WearableHandler.getPlayerData(wearer.getCachedUniqueIdString());

        final IWearableInventory inven = wearer.getCapability(WearableHandler.WEARABLES_CAP).orElse(wearables);
        if (inven instanceof PlayerWearables)
        {
            final PlayerWearables ret = (PlayerWearables) inven;
            if (wearables != null) ret.readFromNBT(wearables.writeToNBT(new CompoundNBT()));
            return ret;
        }

        return wearables;
    }

    public static void syncWearables(final LivingEntity player)
    {
        if (ThutWearables.proxy.isClientSide())
        {
            Thread.dumpStack();
            return;
        }
        ThutWearables.packets.sendToTracking(new PacketSyncWearables(player), player);
    }

    private final boolean overworldRules = true;

    Map<ResourceLocation, EnumWearable> configWearables = Maps.newHashMap();

    /** Cache of wearables for players that die when keep inventory is on. */
    Map<UUID, PlayerWearables> player_inventory_cache = Maps.newHashMap();

    Set<UUID> toKeep = Sets.newHashSet();

    public ThutWearables()
    {
        for (final Phase phase : Phase.values())
            ThutWearables.initMethods.put(phase, new HashSet<java.lang.reflect.Method>());
        CompatParser.findClasses("thut.wearables.compat", ThutWearables.initMethods);
        ThutWearables.doPhase(Phase.CONSTRUCT, null);
        ThutWearables.instance = this;

        MinecraftForge.EVENT_BUS.register(this);

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ThutWearables.proxy::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ThutWearables.proxy::setupClient);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ThutWearables.proxy::finish);
    }
    // TODO wearables drops
    // @SubscribeEvent(priority = EventPriority.HIGHEST)
    // public void dropLoot(final PlayerDropsEvent event)
    // {
    // final PlayerEntity player = event.getPlayerEntity();
    // final GameRules rules = this.overworldRules ?
    // player.getServer().getWorld(0).getGameRules()
    // : player.getEntityWorld().getGameRules();
    // final PlayerWearables cap = ThutWearables.getWearables(player);
    // if (rules.getBoolean("keepInventory")) return;
    //
    // for (int i = 0; i < 13; i++)
    // {
    // final ItemStack stack = cap.getStackInSlot(i);
    // if (stack != null)
    // {
    // EnumWearable.takeOff(player, stack, i);
    // final double d0 = player.posY - 0.3D + player.getEyeHeight();
    // final ItemEntity drop = new ItemEntity(player.getEntityWorld(),
    // player.posX, d0, player.posZ, stack);
    // final float f = player.getRNG().nextFloat() * 0.5F;
    // final float f1 = player.getRNG().nextFloat() * ((float) Math.PI * 2F);
    // drop.motionX = -MathHelper.sin(f1) * f;
    // drop.motionZ = MathHelper.cos(f1) * f;
    // drop.motionY = 0.20000000298023224D;
    // event.getDrops().add(drop);
    // cap.setStackInSlot(i, ItemStack.EMPTY);
    // }
    // }
    // ThutWearables.syncWearables(player);
    // }

    /**
     * Syncs wearables to the player when they join a world. This fixes client
     * issues when they use nether portals, etc
     *
     * @param event
     */
    @SubscribeEvent
    public void joinWorld(final EntityJoinWorldEvent event)
    {
        if (event.getWorld().isRemote) return;
        if (event.getEntity() instanceof ServerPlayerEntity) ThutWearables.packets.sendTo(new PacketSyncWearables(
                (LivingEntity) event.getEntity()), (ServerPlayerEntity) event.getEntity());
    }

    @SubscribeEvent
    public void onEntityCapabilityAttach(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof LivingEntity) event.addCapability(new ResourceLocation(ThutWearables.MODID,
                "wearables"), new PlayerWearables());
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(final AttachCapabilitiesEvent<ItemStack> event)
    {
        final ResourceLocation loc = event.getObject().getItem().getRegistryName();
        if (this.configWearables.containsKey(loc))
        {
            final EnumWearable slot = this.configWearables.get(loc);
            event.addCapability(new ResourceLocation(ThutWearables.MODID, "configwearable"), new ConfigWearable(slot));
        }
    }

    @SubscribeEvent
    public void PlayerLoggedOutEvent(final PlayerLoggedOutEvent event)
    {
        this.player_inventory_cache.remove(event.getPlayer().getUniqueID());
    }

    @SubscribeEvent
    public void playerTick(final LivingUpdateEvent event)
    {
        if (event.getEntity().getEntityWorld().isRemote) return;
        if (event.getEntity() instanceof PlayerEntity && event.getEntity().isAlive())
        {
            final PlayerEntity wearer = (PlayerEntity) event.getEntity();
            final PlayerWearables wearables = ThutWearables.getWearables(wearer);
            for (int i = 0; i < 13; i++)
                EnumWearable.tick(wearer, wearables.getStackInSlot(i), i);
            if (wearer instanceof ServerPlayerEntity) this.player_inventory_cache.put(wearer.getUniqueID(), wearables);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void preDrop(final LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayerEntity)) return;
        final PlayerEntity player = (PlayerEntity) event.getEntity();
        final GameRules rules = this.overworldRules ? player.getServer().getWorld(DimensionType.OVERWORLD)
                .getGameRules() : player.getEntityWorld().getGameRules();
        if (rules.getBoolean(GameRules.KEEP_INVENTORY))
        {
            final PlayerWearables cap = ThutWearables.getWearables(player);
            this.player_inventory_cache.put(player.getUniqueID(), cap);
            this.toKeep.add(player.getUniqueID());
            return;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void respawn(final PlayerRespawnEvent event)
    {
        final PlayerEntity wearer = event.getPlayer();
        if (wearer instanceof ServerPlayerEntity && (this.toKeep.contains(wearer.getUniqueID()) || event
                .isEndConquered()))
        {
            final CompoundNBT tag = this.player_inventory_cache.get(wearer.getUniqueID()).serializeNBT();
            final PlayerWearables wearables = ThutWearables.getWearables(wearer);
            wearables.deserializeNBT(tag);
            this.toKeep.remove(wearer.getUniqueID());
            ThutWearables.syncWearables(wearer);
        }
    }

    @SubscribeEvent
    /**
     * Register the commands.
     *
     * @param event
     */
    public void serverStarting(final FMLServerStartingEvent event)
    {
        CommandGui.register(event.getCommandDispatcher());
    }

    /**
     * Syncs wearables of other mobs to player when they start tracking them.
     *
     * @param event
     */
    @SubscribeEvent
    public void startTracking(final StartTracking event)
    {
        if (event.getTarget() instanceof LivingEntity && event.getPlayer().isServerWorld()) ThutWearables.packets
                .sendTo(new PacketSyncWearables((LivingEntity) event.getTarget()), (ServerPlayerEntity) event
                        .getPlayer());
    }
}