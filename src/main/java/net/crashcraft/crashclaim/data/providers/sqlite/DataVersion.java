package net.crashcraft.crashclaim.data.providers.sqlite;

import java.sql.SQLException;

public interface DataVersion {
    /**
     * @return the revision id of this database set. Needs to be incremented from the previous data revision
     */
    int getVersion();

    /**
     * Executes the upgrade queries on the global DB
     * @throws SQLException When data version has failed
     */
    void executeUpgrade(int fromRevision) throws SQLException;
}
