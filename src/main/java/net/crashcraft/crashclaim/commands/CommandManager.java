package net.crashcraft.crashclaim.commands;

import co.aikar.commands.PaperCommandManager;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.commands.claiming.ClaimCommand;
import net.crashcraft.crashclaim.commands.claiming.UnClaimCommand;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.migration.MigrationAdapter;
import net.crashcraft.crashclaim.permissions.BypassManager;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.visualize.VisualizationManager;

import java.util.ArrayList;

public class CommandManager {
    private final PaperCommandManager commandManager;
    private final CrashClaim plugin;

    public CommandManager(CrashClaim plugin) {
        this.plugin = plugin;

        this.commandManager = new PaperCommandManager(plugin);

        loadCommandCompletions();
        loadCommands();
    }

    private void loadCommands(){
        ClaimDataManager manager = plugin.getDataManager();
        BypassManager bypassManager = PermissionHelper.getPermissionHelper().getBypassManager();
        VisualizationManager visualizationManager = plugin.getVisualizationManager();

        commandManager.registerCommand(new ShowClaimsCommand(visualizationManager, manager));
        commandManager.registerCommand(new HideClaimsCommand(visualizationManager));

        commandManager.registerCommand(new ClaimCommand(manager, visualizationManager));
        commandManager.registerCommand(new UnClaimCommand(manager, visualizationManager));


        commandManager.registerCommand(new MenuCommand(manager, visualizationManager));
        commandManager.registerCommand(new BypassCommand(bypassManager));
        commandManager.registerCommand(new ClaimInfoCommand(manager));
        commandManager.registerCommand(new EjectCommand(manager));
        commandManager.registerCommand(new AdminCommand(plugin, plugin.getMigrationManager()));
    }

    private void loadCommandCompletions(){
        ArrayList<MigrationAdapter> adapters = plugin.getMigrationManager().getAdapters();
        String[] completions = new String[adapters.size()];

        for (int x = 0; x < completions.length; x++){
            completions[x] = adapters.get(x).getIdentifier();
        }

        commandManager.getCommandCompletions().registerStaticCompletion("migrators", completions);
    }

    public PaperCommandManager getCommandManager() {
        return commandManager;
    }
}
