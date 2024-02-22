package net.crashcraft.crashclaim.data.providers.sqlite.versions;

import co.aikar.idb.DB;
import net.crashcraft.crashclaim.data.providers.sqlite.DataVersion;

import java.sql.SQLException;

public class DataRev5 implements DataVersion {
    @Override
    public int getVersion() {
        return 5;
    }

    // add dropPickupItems column to permission_set
    @Override
    public void executeUpgrade(int fromRevision) throws SQLException {
        DB.executeUpdate("ALTER TABLE permission_set ADD COLUMN dropPickupItems INTEGER DEFAULT 2");
    }
}
