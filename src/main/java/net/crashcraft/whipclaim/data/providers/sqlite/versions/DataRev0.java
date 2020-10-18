package net.crashcraft.whipclaim.data.providers.sqlite.versions;

import co.aikar.idb.DB;
import net.crashcraft.whipclaim.data.providers.sqlite.DataVersion;

import java.sql.SQLException;

public class DataRev0 implements DataVersion {
    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void executeUpgrade(int fromRevision) throws SQLException {
        DB.executeUpdate(
                "CREATE TABLE IF NOT EXISTS properties (" +
                        "key TEXT NOT NULL," +
                        "value TEXT NOT NULL," +
                        "UNIQUE(value,key)" +
                        ")"
        );
        DB.executeUpdate(
                "CREATE TABLE IF NOT EXISTS players (" +
                        "id INTEGER," +
                        "uuid INTEGER NOT NULL," +
                        "username TEXT," +
                        "PRIMARY KEY(id AUTOINCREMENT)" +
                        ")"
        );
        DB.executeUpdate(
                "CREATE TABLE IF NOT EXISTS claimworld (" +
                        "id INTEGER," +
                        "world INTEGER NOT NULL UNIQUE," +
                        "PRIMARY KEY(id AUTOINCREMENT)" +
                        ")"
        );
        DB.executeUpdate(
                "CREATE TABLE IF NOT EXISTS claim_data (" +
                        "id INTEGER," +
                        "minX INTEGER NOT NULL," +
                        "minZ INTEGER NOT NULL," +
                        "maxX INTEGER NOT NULL," +
                        "maxZ INTEGER NOT NULL," +
                        "world INTEGER NOT NULL," +
                        "name TEXT," +
                        "entryMessage TEXT," +
                        "exitMessage TEXT," +
                        "FOREIGN KEY(world) REFERENCES claimworld(id) ON DELETE CASCADE," +
                        "PRIMARY KEY(id AUTOINCREMENT)" +
                        ")"
        );
        DB.executeUpdate(
                "CREATE TABLE IF NOT EXISTS permissioncontainers (" +
                        "id INTEGER," +
                        "identifier TEXT NOT NULL UNIQUE," +
                        "PRIMARY KEY(id AUTOINCREMENT)" +
                        ")"
        );
        DB.executeUpdate(
                "CREATE TABLE IF NOT EXISTS permission_set (" +
                        "claimdata_id INTEGER NOT NULL," +
                        "players_id INTEGER," +
                        "containers_id INTEGER NOT NULL," +
                        "build INTEGER NOT NULL," +
                        "interactions INTEGER NOT NULL," +
                        "entities INTEGER NOT NULL," +
                        "explosions INTEGER NOT NULL," +
                        "teleportation INTEGER NOT NULL," +
                        "viewSubClaims INTEGER NOT NULL," +
                        "pistons INTEGER," +
                        "fluids INTEGER," +
                        "modifyPermissions INTEGER," +
                        "modifyClaim INTEGER," +
                        "FOREIGN KEY(containers_id) REFERENCES permissioncontainers(id) ON DELETE CASCADE," +
                        "FOREIGN KEY(claimdata_id) REFERENCES claim_data(id) ON DELETE CASCADE," +
                        "FOREIGN KEY(players_id) REFERENCES players(id) ON DELETE CASCADE" +
                        ")"
        );
        DB.executeUpdate(
                "CREATE TABLE IF NOT EXISTS claims (" +
                        "id INTEGER NOT NULL," +
                        "data INTEGER NOT NULL," +
                        "players_id INTEGER NOT NULL," +
                        "FOREIGN KEY(data) REFERENCES claim_data(id) ON DELETE CASCADE," +
                        "FOREIGN KEY(players_id) REFERENCES players(id) ON DELETE CASCADE," +
                        "PRIMARY KEY(id)" +
                        ")"
        );
        DB.executeUpdate(
                "CREATE TABLE IF NOT EXISTS subclaims (" +
                        "id INTEGER," +
                        "data INTEGER NOT NULL," +
                        "claim_id INTEGER NOT NULL," +
                        "PRIMARY KEY(id AUTOINCREMENT)" +
                        ")"
        );

    }
}
