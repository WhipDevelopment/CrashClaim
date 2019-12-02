package net.crashcraft.whipclaim;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.crashcraft.whipclaim.commands.*;
import net.crashcraft.whipclaim.commands.modes.ModeCommand;
import net.crashcraft.whipclaim.commands.modes.SubClaimCommand;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.visualize.VisualizationManager;
import org.bukkit.Bukkit;

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
        visualizationManager = new VisualizationManager(this, protocolManager);
        manager = new ClaimDataManager(this);

        CommandManager commandManager = new CommandManager(this);

        ModeCommand modeCommand = new ModeCommand(this, protocolManager);

        ShowClaimsCommand showClaimsCommand = new ShowClaimsCommand(visualizationManager, manager);
        TestCommand testCommand = new TestCommand(manager);
        HideClaimsCommand hideClaimsCommand = new HideClaimsCommand(visualizationManager);
        SubClaimCommand subClaimCommand = new SubClaimCommand(manager, visualizationManager);

        commandManager.registerCommand(showClaimsCommand);
        commandManager.registerCommand(testCommand);
        commandManager.registerCommand(hideClaimsCommand);
        commandManager.registerCommand(modeCommand);

        Bukkit.getPluginManager().registerEvents(manager, this);
        Bukkit.getPluginManager().registerEvents(subClaimCommand, this);
    }

    @Override
    public void onDisable() {
        manager.saveClaimsSync();
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
