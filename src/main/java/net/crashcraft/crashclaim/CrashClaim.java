package net.crashcraft.crashclaim;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import dev.whip.crashutils.CrashUtils;
import dev.whip.crashutils.menusystem.GUI;
import io.papermc.lib.PaperLib;
import net.crashcraft.crashclaim.api.CrashClaimAPI;
import net.crashcraft.crashclaim.commands.CommandManager;
import net.crashcraft.crashclaim.compatability.CompatabilityManager;
import net.crashcraft.crashclaim.compatability.CompatabilityWrapper;
import net.crashcraft.crashclaim.config.ConfigManager;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.data.MaterialName;
import net.crashcraft.crashclaim.listeners.PaperListener;
import net.crashcraft.crashclaim.listeners.PlayerListener;
import net.crashcraft.crashclaim.listeners.WorldListener;
import net.crashcraft.crashclaim.localization.LocalizationLoader;
import net.crashcraft.crashclaim.migration.MigrationManager;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.pluginsupport.PluginSupport;
import net.crashcraft.crashclaim.pluginsupport.PluginSupportManager;
import net.crashcraft.crashclaim.update.UpdateManager;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.PaymentProcessor;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class CrashClaim extends JavaPlugin {
    private static CrashClaim plugin;

    private boolean dataLoaded = false;

    private CrashClaimAPI api;

    private CompatabilityWrapper wrapper;
    private PluginSupportManager pluginSupport;

    private ClaimDataManager manager;
    private VisualizationManager visualizationManager;
    private ProtocolManager protocolManager;
    private CrashUtils crashUtils;
    private MaterialName materialName;
    private PaymentProcessor payment;
    private CrashPayment paymentPlugin;
    private CommandManager commandManager;
    private MigrationManager migrationManager;
    private BukkitAudiences adventure;
    private UpdateManager updateManager;

    @Override
    public void onLoad() {
        plugin = this;

        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.paymentPlugin = (CrashPayment) Bukkit.getPluginManager().getPlugin("CrashPayment");

        if (paymentPlugin == null){
            disablePlugin("[Payment] CrashPayment plugin not found, disabling plugin, download and install it here, https://www.spigotmc.org/resources/crashpayment.94069/");
        }

        this.crashUtils = new CrashUtils(this);
    }

    @Override
    public void onEnable() {
        this.pluginSupport = new PluginSupportManager(this); // Enable plugin support

        Bukkit.getPluginManager().registerEvents(pluginSupport, this);

        taskChainFactory = BukkitTaskChainFactory.create(this);
        this.adventure = BukkitAudiences.create(this);

        loadConfigs();

        wrapper = new CompatabilityManager(protocolManager).getWrapper(); // Find and fetch version wrapper

        getLogger().info("Loading language file");
        LocalizationLoader.initialize(); // Init and reload localization
        getLogger().info("Finished loading language file");

        crashUtils.setupMenuSubSystem();
        crashUtils.setupTextureCache();

        payment = paymentPlugin.setupPaymentProvider(this, GlobalConfig.paymentProvider).getProcessor();

        this.visualizationManager = new VisualizationManager(this);
        this.manager = new ClaimDataManager(this);
        this.materialName = new MaterialName();

        this.dataLoaded = true;

        new PermissionHelper(manager);

        this.migrationManager = new MigrationManager(this);

        Bukkit.getPluginManager().registerEvents(new WorldListener(manager, visualizationManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(manager, visualizationManager), this);

        if (PaperLib.isPaper()){
            getLogger().info("Using extra protections provided by the paper api");
            Bukkit.getPluginManager().registerEvents(new PaperListener(manager, visualizationManager), this);
        } else {
            getLogger().info("Looks like your not running paper, some protections will be disabled");
            PaperLib.suggestPaper(this);
        }

        commandManager = new CommandManager(this);

        if (GlobalConfig.useStatistics){
            getLogger().info("Enabling Statistics");
            Metrics metrics = new Metrics(this, 12015);
            metrics.addCustomChart(new SimplePie("used_language", () -> GlobalConfig.locale));
        }

        if (GlobalConfig.checkUpdates){
            updateManager = new UpdateManager(this);
        }

        LocalizationLoader.register(); // Register PlaceHolders

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
        commandManager.getCommandManager().unregisterCommands();
        for (Player player : Bukkit.getOnlinePlayers()){
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof GUI){
                player.closeInventory();
            }
        }

        //Null all references just to be sure, manager will still hold them but this stops this class from being referenced for anything
        dataLoaded = false;
        plugin = null;
        api = null;
        manager = null;
        visualizationManager = null;
        protocolManager = null;
        crashUtils = null;
        materialName = null;
        payment = null;
        paymentPlugin = null;
        commandManager = null;
        migrationManager = null;
        adventure = null;
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

    public CompatabilityWrapper getWrapper() {
        return wrapper;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public PluginSupport getPluginSupport(){
        return pluginSupport.getSupportDistributor();
    }

    public UpdateManager getUpdateManager() {
        return updateManager;
    }
}
