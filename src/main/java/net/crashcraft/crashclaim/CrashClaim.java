package net.crashcraft.crashclaim;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import dev.whip.crashutils.CrashUtils;
import io.papermc.lib.PaperLib;
import net.crashcraft.crashpayment.CrashPayment;
import net.crashcraft.crashpayment.payment.PaymentProcessor;
import net.crashcraft.crashclaim.api.CrashClaimAPI;
import net.crashcraft.crashclaim.commands.*;
import net.crashcraft.crashclaim.commands.modes.ModeCommand;
import net.crashcraft.crashclaim.config.ConfigManager;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.data.MaterialName;
import net.crashcraft.crashclaim.listeners.PaperListener;
import net.crashcraft.crashclaim.listeners.PlayerListener;
import net.crashcraft.crashclaim.listeners.WorldListener;
import net.crashcraft.crashclaim.menus.helpers.StaticItemLookup;
import net.crashcraft.crashclaim.permissions.BypassManager;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CrashClaim extends JavaPlugin {
    private static CrashClaim plugin;

    private boolean dataLoaded = false;

    private CrashClaimAPI api;

    private ClaimDataManager manager;
    private VisualizationManager visualizationManager;
    private ProtocolManager protocolManager;
    private CrashUtils crashUtils;
    private MaterialName materialName;
    private PaymentProcessor payment;
    private CrashPayment paymentPlugin;

    @Override
    public void onLoad() {
        plugin = this;
        protocolManager = ProtocolLibrary.getProtocolManager();

        paymentPlugin = (CrashPayment) Bukkit.getPluginManager().getPlugin("CrashPayment");
        if (paymentPlugin == null){
            disablePlugin("Payment plugin not found, disabling plugin");
        }

        this.crashUtils = new CrashUtils(this);

        this.api = new CrashClaimAPI();
    }

    @Override
    public void onEnable() {
        taskChainFactory = BukkitTaskChainFactory.create(this);

        if (getDataFolder().mkdirs()){
            getLogger().info("Created plugin directory");
        }

        new ConfigManager(this);

        crashUtils.setupMenuSubSystem();
        crashUtils.setupTextureCache();

        if (paymentPlugin != null) {
            payment = paymentPlugin.setupPaymentProvider(this).getProcessor();
        }

        visualizationManager = new VisualizationManager(this, protocolManager);
        manager = new ClaimDataManager(this);

        dataLoaded = true;

        materialName = new MaterialName();

        BypassManager bypassManager = new BypassManager();
        new PermissionHelper(manager, bypassManager);

        CommandManager commandManager = new CommandManager(this);

        commandManager.registerCommand(new ShowClaimsCommand(visualizationManager, manager));
        commandManager.registerCommand(new HideClaimsCommand(visualizationManager));
        commandManager.registerCommand(new ModeCommand(this, protocolManager));
        commandManager.registerCommand(new MenuCommand(manager, visualizationManager));
        commandManager.registerCommand(new BypassCommand(bypassManager));
        commandManager.registerCommand(new ClaimInfoCommand(manager));
        commandManager.registerCommand(new EjectCommand(manager));

        Bukkit.getPluginManager().registerEvents(manager, this);
        Bukkit.getPluginManager().registerEvents(bypassManager, this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(manager, visualizationManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(manager, visualizationManager), this);


        if (PaperLib.isPaper()){
            getLogger().info("Using extra protections provided by the paper api");
            Bukkit.getPluginManager().registerEvents(new PaperListener(manager, visualizationManager), this);
        } else {
            getLogger().info("Looks like your not running paper, some protections will be disabled.");
            PaperLib.suggestPaper(this);
        }

        new StaticItemLookup();
    }

    @Override
    public void onDisable() {
        if (dataLoaded) {
            manager.saveClaimsSync();
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
}
