package net.crashcraft.crashclaim.data.providers.sqlite.versions;

import co.aikar.idb.DB;
import net.crashcraft.crashclaim.data.providers.sqlite.DataVersion;

import java.sql.SQLException;

public class DataRev1 implements DataVersion {
    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void executeUpgrade(int fromRevision) throws SQLException {
        DB.executeUpdate("CREATE TABLE \"contributions\" (\n" +
                "\t\"data_id\"\tINTEGER NOT NULL,\n" +
                "\t\"players_id\"\tINTEGER NOT NULL,\n" +
                "\t\"amount\"\tINTEGER NOT NULL,\n" +
                "\tFOREIGN KEY(\"data_id\") REFERENCES \"claim_data\"(\"id\") ON DELETE CASCADE,\n" +
                "\tFOREIGN KEY(\"players_id\") REFERENCES \"players\"(\"id\") ON DELETE CASCADE,\n" +
                "\tPRIMARY KEY(\"data_id\",\"players_id\")\n" +
                ");");
    }
}
