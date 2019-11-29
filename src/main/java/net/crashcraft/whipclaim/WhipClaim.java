package net.crashcraft.whipclaim;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.crashcraft.whipclaim.commands.ClaimModeCommand;
import net.crashcraft.whipclaim.commands.CommandManager;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.listeners.ProtocalListener;
import net.crashcraft.whipclaim.permissions.PermissionSetup;
import net.crashcraft.whipclaim.visualize.VisualizationManager;
import org.bukkit.Bukkit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class WhipClaim extends JavaPlugin {
    private static WhipClaim plugin;

    private ClaimDataManager manager;
    private VisualizationManager visualizationManager;
    private ProtocolManager protocolManager;

    @Override
    public void onLoad() {
        plugin = this;
        protocolManager = ProtocolLibrary.getProtocolManager();

        if (getDataFolder().mkdirs()){
            getLogger().info("Created plugin directory");
        }

        saveDefaultConfig();
        saveResource("localization.yml", false);
        saveResource("lookup.yml", false);
    }

    @Override
    public void onEnable() {
        PermissionSetup permissionSetup = new PermissionSetup(this);

        visualizationManager = new VisualizationManager(this, protocolManager);
        manager = new ClaimDataManager(this);

        CommandManager commandManager = new CommandManager(this);
        ClaimModeCommand claimModeCommand = new ClaimModeCommand(this);
        commandManager.registerCommand(claimModeCommand);
        Bukkit.getPluginManager().registerEvents(claimModeCommand, this);

        new ProtocalListener(protocolManager, this, claimModeCommand);
    }

    @Override
    public void onDisable() {

    }

    public static WhipClaim getPlugin() {
        return plugin;
    }

    public ClaimDataManager getDataManager() {
        return manager;
    }

    public VisualizationManager getVisualizationManager() {
        return visualizationManager;
    }
}
