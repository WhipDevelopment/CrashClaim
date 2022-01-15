package net.crashcraft.crashclaim.migration;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.migration.adapters.GriefPreventionAdaptor;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
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

    public CompletableFuture<String> migrate(MigrationAdapter adapter){
        String requirementCheck = adapter.checkRequirements(this);
        if (requirementCheck != null){
            return CompletableFuture.completedFuture("Requirement Check Failed: " + requirementCheck);
        }

        plugin.getLogger().info("Starting data migration with [" + adapter.getIdentifier() + "]");
        CompletableFuture<String> migrateFuture = new CompletableFuture<>();
        CompletableFuture<String> completableFuture = adapter.migrate(this);
        completableFuture.thenAccept((error) -> {
            if (error != null){
                plugin.getLogger().severe("Data migration failed with error: " + error);

                migrateFuture.complete(error);
            }
            plugin.getLogger().info("Data migration completed successfully");

            plugin.getLogger().info("Force data save starting...");
            manager.forceSaveClaims().thenAccept((a) -> {
                plugin.getLogger().info("Force data save finished.");
                migrateFuture.complete(null);
            });
        });

        return migrateFuture;
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
