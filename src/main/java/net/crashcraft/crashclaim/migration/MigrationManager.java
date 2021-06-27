package net.crashcraft.crashclaim.migration;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.migration.adapters.GriefPreventionAdaptor;

import java.util.ArrayList;
import java.util.logging.Logger;

public class MigrationManager {
    private final CrashClaim plugin;
    private final ClaimDataManager manager;
    private final Logger logger;
    private final ArrayList<MigrationAdapter> adapters;

    public MigrationManager(CrashClaim plugin){
        this.plugin = plugin;
        this.manager = plugin.getDataManager();
        this.logger = plugin.getLogger();
        this.adapters = new ArrayList<>();

        adapters.add(new GriefPreventionAdaptor());
    }

    public String migrate(MigrationAdapter adapter){
        String requirementCheck = adapter.checkRequirements(this);
        if (requirementCheck != null){
            return "Requirement Check Failed: " + requirementCheck;
        }

        manager.setFreezeSaving(true);

        plugin.getLogger().info("Starting data migration with [" + adapter.getIdentifier() + "]");
        String error = adapter.migrate(this);
        if (error != null){
            plugin.getLogger().severe("Data migration failed with error: " + error);

            cleanup();
            return error;
        }
        plugin.getLogger().info("Data migration completed successfully");

        manager.setFreezeSaving(false);
        manager.saveClaimsSync();

        cleanup();
        return null;
    }

    private void cleanup(){
        manager.setFreezeSaving(false);
    }

    public MigrationAdapter getMigrationAdaptor(String name){
        String realName = name.toLowerCase();

        for (MigrationAdapter adapter : adapters){
            if (adapter.getIdentifier().toLowerCase().equals(realName)){
                return adapter;
            }
        }

        return null;
    }

    public ArrayList<MigrationAdapter> getAdapters() {
        return adapters;
    }

    public ClaimDataManager getManager() {
        return manager;
    }

    public Logger getLogger() {
        return logger;
    }
}
