package net.crashcraft.crashclaim;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.lib.PaperLib;
import net.crashcraft.crashclaim.api.CrashClaimAPI;
import net.crashcraft.crashclaim.commands.CommandManager;
import net.crashcraft.crashclaim.commands.claiming.ClaimCommand;
import net.crashcraft.crashclaim.config.ConfigManager;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.listeners.PacketEventsListener;
import net.crashcraft.crashclaim.payment.PaymentProcessor;
import net.crashcraft.crashclaim.payment.PaymentProvider;
import net.crashcraft.crashclaim.payment.ProcessorManager;
import net.crashcraft.crashclaim.payment.ProviderInitializationException;
import net.crashcraft.crashclaim.crashutils.CrashUtils;
import net.crashcraft.crashclaim.crashutils.menusystem.GUI;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.data.MaterialName;
import net.crashcraft.crashclaim.listeners.PlayerListener;
import net.crashcraft.crashclaim.listeners.WorldListener;
import net.crashcraft.crashclaim.localization.LocalizationLoader;
import net.crashcraft.crashclaim.migration.MigrationManager;
import net.crashcraft.crashclaim.packet.PacketHandler;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.pluginsupport.PluginSupport;
import net.crashcraft.crashclaim.pluginsupport.PluginSupportManager;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CrashClaim extends JavaPlugin {
    private static CrashClaim plugin;

    private boolean dataLoaded = false;

    private CrashClaimAPI api;

    private PacketHandler handler;
    private PluginSupportManager pluginSupport;

    private ClaimDataManager manager;
    private VisualizationManager visualizationManager;
    private CrashUtils crashUtils;
    private MaterialName materialName;
    private PaymentProcessor payment;
    private CommandManager commandManager;
    private MigrationManager migrationManager;
    private BukkitAudiences adventure;

    @Override
    public void onLoad() {
        plugin = this;

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        //Are all listeners read only?
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(false)
                .bStats(true);
        PacketEvents.getAPI().load();


        this.crashUtils = new CrashUtils(this);
        this.pluginSupport = new PluginSupportManager(this); // Enable plugin support
        this.pluginSupport.onLoad(); // Load all plugin support
    }

    @Override
    public void onEnable() {
        List<String> supportedVersions = Arrays.asList("1.20.4", "1.20.5", "1.20.6", "1.21"); //order from min to max
        if (!isServerSupported(supportedVersions)) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getPluginManager().registerEvents(pluginSupport, this);

        taskChainFactory = BukkitTaskChainFactory.create(this);
        this.adventure = BukkitAudiences.create(this);

        loadConfigs();

        handler = new PacketHandler(); // Find and fetch version wrapper

        getLogger().info("Loading language file");
        LocalizationLoader.initialize(); // Init and reload localization
        getLogger().info("Finished loading language file");

        crashUtils.setupMenuSubSystem();
        crashUtils.setupTextureCache();

        payment = setupPaymentProvider(this, GlobalConfig.paymentProvider).getProcessor();

        this.visualizationManager = new VisualizationManager(this);
        this.manager = new ClaimDataManager(this);
        this.materialName = new MaterialName();

        this.dataLoaded = true;

        new PermissionHelper(manager);

        this.migrationManager = new MigrationManager(this);
        commandManager = new CommandManager(this);

        Bukkit.getPluginManager().registerEvents(new WorldListener(manager, visualizationManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(manager, visualizationManager), this);

        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventsListener(plugin, new ClaimCommand(getDataManager(), getVisualizationManager())),
                PacketListenerPriority.LOW);
        PacketEvents.getAPI().init();

        pluginSupport.onEnable();
        LocalizationLoader.register(); // Register PlaceHolders

        Bukkit.getServicesManager().register(PaymentProvider.class, payment.getProvider(), plugin, ServicePriority.Normal);

        if (GlobalConfig.useStatistics) {
            getLogger().info("Enabling Statistics");
            Metrics metrics = new Metrics(this, 12015);
            metrics.addCustomChart(new SimplePie("used_language", () -> GlobalConfig.locale));
        }

        this.api = new CrashClaimAPI(this); // Enable api last as it might require some instances before to function properly.
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this); // Stop saving tasks

        if (dataLoaded) {
            manager.saveClaimsSync(); // Do a force save to make sure all claims are saved as we just unregistered the save task
            manager.cleanupAndClose(); // freezes claim saving and cleans up memory references to claims
        }

        //Unregister all user facing things
        HandlerList.unregisterAll(this);
        if (commandManager != null) commandManager.getCommandManager().unregisterCommands();
        for (Player player : Bukkit.getOnlinePlayers()){
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof GUI){
                player.closeInventory();
            }
        }

        pluginSupport.onDisable();

        //Null all references just to be sure, manager will still hold them but this stops this class from being referenced for anything
        dataLoaded = false;
        plugin = null;
        api = null;
        manager = null;
        visualizationManager = null;
        crashUtils = null;
        materialName = null;
        payment = null;
        commandManager = null;
        migrationManager = null;
        adventure = null;
    }
    public ProcessorManager setupPaymentProvider(JavaPlugin plugin){
        return setupPaymentProvider(plugin, "");
    }
    public ProcessorManager setupPaymentProvider(JavaPlugin plugin, String providerOverride){
        try {
            return new ProcessorManager(plugin, providerOverride);
        } catch (ProviderInitializationException e){
            e.printStackTrace();
        }
        return null;
    }

    public void loadConfigs(){
        File dataFolder = plugin.getDataFolder();

        if (dataFolder.mkdirs()){
            getLogger().info("Created data directory");
        }

        try {
            getLogger().info("Loading configs");
            if (!new File(dataFolder, "lookup.yml").exists()) {
                plugin.saveResource("lookup.yml", false);
            }
            ConfigManager.initConfig(new File(dataFolder, "config.yml"), GlobalConfig.class);

            getLogger().info("Finished loading base configs");
        } catch (Exception ex){
            ex.printStackTrace();
            getLogger().severe("Could not load configuration properly. Stopping server");
            plugin.getServer().shutdown();
        }
    }

    private boolean isServerSupported(List<String> supportedVersions) {
        if (!PaperLib.isPaper()) {
            getLogger().severe("CrashClaim requires Paper to run.");
            PaperLib.suggestPaper(this);
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        String minecraftVersion = Bukkit.getMinecraftVersion();
        int minecraftVersionInt = versionStringToInt(minecraftVersion);
        int minSupportedVersionInt = versionStringToInt(supportedVersions.get(0));
        int maxSupportedVersionInt = versionStringToInt(supportedVersions.get(supportedVersions.size() - 1));

        if (minecraftVersionInt < minSupportedVersionInt) {
            getLogger().severe("Your server's version is older than CrashClaim's minimum supported version, which is " + supportedVersions.get(0) +
                    ". The plugin will not attempt loading. Your server is currently running version " + minecraftVersion + ".");
            return false;
        } else if (minecraftVersionInt > maxSupportedVersionInt) {
            getLogger().warning("Your server's version is newer than CrashClaim's maximum supported version, which is " + supportedVersions.get(supportedVersions.size() - 1) +
                    ". The plugin will still attempt to load, but issues may arise. " +
                    "Please check if the plugin has newer versions available. Your server is currently running version " + minecraftVersion + ".");
        }

        return true;
    }

    private int versionStringToInt(String version) {
        String[] parts = version.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        return major * 10000 + minor * 100 + patch;
    }

    public void disablePlugin(String error){
        getLogger().severe(error);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    private static TaskChainFactory taskChainFactory;
    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }
    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }

    public static CrashClaim getPlugin() {
        return plugin;
    }

    public ClaimDataManager getDataManager() {
        return manager;
    }

    public VisualizationManager getVisualizationManager() {
        return visualizationManager;
    }

    public CrashUtils getCrashUtils() {
        return crashUtils;
    }

    public PaymentProcessor getPayment() {
        return payment;
    }

    public MaterialName getMaterialName() {
        return materialName;
    }

    public CrashClaimAPI getApi() {
        return api;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }


    public MigrationManager getMigrationManager() {
        return migrationManager;
    }

    public BukkitAudiences getAdventure() {
        return adventure;
    }

    public PacketHandler getHandler() {
        return handler;
    }

    public PluginSupport getPluginSupport(){
        return pluginSupport.getSupportDistributor();
    }

    public PluginSupportManager getPluginSupportManager() {
        return pluginSupport;
    }
}
