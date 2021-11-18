package net.crashcraft.crashclaim.data.providers.sqlite.versions;

import co.aikar.idb.DB;
import net.crashcraft.crashclaim.data.providers.sqlite.DataVersion;

import java.sql.SQLException;

public class DataRev3 implements DataVersion {
    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public void executeUpgrade(int fromRevision) throws SQLException {
        DB.executeUpdate("PRAGMA foreign_keys = OFF"); // Turn foreign keys off

        DB.executeUpdate("CREATE TABLE \"permission_set_backup\" (\n" +
                "\t\"data_id\"\tINTEGER NOT NULL,\n" +
                "\t\"players_id\"\tINTEGER NOT NULL,\n" +
                "\t\"build\"\tINTEGER NOT NULL,\n" +
                "\t\"interactions\"\tINTEGER NOT NULL,\n" +
                "\t\"entities\"\tINTEGER NOT NULL,\n" +
                "\t\"teleportation\"\tINTEGER NOT NULL,\n" +
                "\t\"viewSubClaims\"\tINTEGER NOT NULL,\n" +
                "\t\"defaultContainer\"\tINTEGER NOT NULL,\n" +
                "\t\"explosions\"\tINTEGER,\n" +
                "\t\"pistons\"\tINTEGER,\n" +
                "\t\"fluids\"\tINTEGER,\n" +
                "\t\"modifyPermissions\"\tINTEGER,\n" +
                "\t\"modifyClaim\"\tINTEGER,\n" +
                "\tFOREIGN KEY(\"data_id\") REFERENCES \"claim_data\"(\"id\") ON DELETE CASCADE,\n" +
                "\tFOREIGN KEY(\"players_id\") REFERENCES \"players\"(\"id\") ON DELETE CASCADE,\n" +
                "\tPRIMARY KEY(\"data_id\",\"players_id\")\n" +
                ")");

        DB.executeInsert("INSERT INTO permission_set_backup SELECT * FROM permission_set");

        DB.executeUpdate("DROP TABLE permission_set");

        DB.executeUpdate("CREATE TABLE \"permission_set\" (\n" +
                "\t\"data_id\"\tINTEGER NOT NULL,\n" +
                "\t\"players_id\"\tINTEGER NOT NULL,\n" +
                "\t\"build\"\tINTEGER NOT NULL,\n" +
                "\t\"interactions\"\tINTEGER NOT NULL,\n" +
                "\t\"entities\"\tINTEGER NOT NULL,\n" +
                "\t\"teleportation\"\tINTEGER NOT NULL,\n" +
                "\t\"viewSubClaims\"\tINTEGER NOT NULL,\n" +
                "\t\"defaultContainer\"\tINTEGER NOT NULL,\n" +
                "\t\"explosions\"\tINTEGER,\n" +
                "\t\"entityGrief\"\tINTEGER,\n" +
                "\t\"pistons\"\tINTEGER,\n" +
                "\t\"fluids\"\tINTEGER,\n" +
                "\t\"modifyPermissions\"\tINTEGER,\n" +
                "\t\"modifyClaim\"\tINTEGER,\n" +
                "\tFOREIGN KEY(\"data_id\") REFERENCES \"claim_data\"(\"id\") ON DELETE CASCADE,\n" +
                "\tFOREIGN KEY(\"players_id\") REFERENCES \"players\"(\"id\") ON DELETE CASCADE,\n" +
                "\tPRIMARY KEY(\"data_id\",\"players_id\")\n" +
                ")");

        DB.executeInsert("INSERT INTO permission_set(data_id, players_id, build, interactions, entities, teleportation, viewSubClaims, defaultContainer, " +
                "explosions, pistons, fluids, modifyPermissions, modifyClaim) SELECT * FROM permission_set_backup;");

        DB.executeInsert("INSERT INTO permission_set(entityGrief) VALUES (0) WHERE players_id = -1");

        DB.executeUpdate("PRAGMA foreign_keys = ON");  // Undo
    }
}
