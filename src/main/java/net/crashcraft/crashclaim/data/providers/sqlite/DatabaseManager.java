package net.crashcraft.crashclaim.data.providers.sqlite;

import co.aikar.idb.DB;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.data.providers.sqlite.versions.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class DatabaseManager {
    private final CrashClaim plugin;
    private final Logger logger;
    private final ArrayList<DataVersion> dataVersions;

    private int currentRevision;

    public DatabaseManager(CrashClaim plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dataVersions = new ArrayList<>();
        this.currentRevision = -1; // represents no database configured yet

        setInitialParams();
        setupDataVersions();
        validateDataVersions();

        try {
            String value = DB.getFirstColumn("SELECT value FROM properties WHERE `key` = ?", "dataVersion");
            currentRevision = Integer.parseInt(value);
        } catch (SQLException e){ // Table does not exist so we assume database is fresh
            logger.info("No data version found, creating database schema");
        }

        updateDatabase();
    }

    private void setInitialParams(){
        try {
            DB.executeUpdate("PRAGMA foreign_keys=ON");
        } catch (SQLException e){
            plugin.disablePlugin("There was an error enabling SQlite foreign keys support, it is unsafe to continue.");
        }
    }

    private void updateDatabase(){
        int latestVersion = (dataVersions.get(dataVersions.size() - 1).getVersion());
        if (currentRevision >= latestVersion){
            if (currentRevision > latestVersion){
                logger.severe("Database is at a higher version than the plugin, tread with caution");
            }
            return;
        }

        logger.info("Database is converting: [" + currentRevision + " -> " + latestVersion + "]");

        for (int x = currentRevision == -1 ? 0 : currentRevision + 1; x < dataVersions.size(); x++){
            DataVersion version = dataVersions.get(x);
            logger.info("Converting [" + currentRevision + " -> " + version.getVersion() + "]");
            try {
                version.executeUpgrade(currentRevision);
                currentRevision = version.getVersion();
            } catch (SQLException e){
                e.printStackTrace();
                plugin.disablePlugin("Failed conversion, disabling plugin, manual data fixing may be needed");
            }

            try {
                DB.executeInsert("REPLACE INTO properties(key, value) VALUES (?, ?)", "dataVersion", currentRevision);
            } catch (SQLException e){
                logger.severe("Failed to write property to database");
                e.printStackTrace();
            }
        }
    }

    private void setupDataVersions(){
        registerDataVersion(new DataRev0());
        registerDataVersion(new DataRev1());
        registerDataVersion(new DataRev2());
        registerDataVersion(new DataRev3());
        registerDataVersion(new DataRev4());
        registerDataVersion(new DataRev5());
    }

    private void validateDataVersions(){
        ArrayList<Integer> failedDataVersions = new ArrayList<>();
        for (int x = 0; x < dataVersions.size(); x++){
            DataVersion version = dataVersions.get(x);

            if (version == null){
                failedDataVersions.add(x);
            }
        }

        if (failedDataVersions.size() > 0){
            StringBuilder builder = new StringBuilder("Data versions failed to initialize. [");
            for (int version : failedDataVersions){
                builder.append(version).append(", ");
            }
            builder.append("]");
            plugin.disablePlugin(builder.toString());
        }
    }

    private void registerDataVersion(DataVersion version){
        dataVersions.add(version.getVersion(), version);
    }
}
