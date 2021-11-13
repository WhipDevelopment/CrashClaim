package net.crashcraft.crashclaim.data.providers.sqlite.versions;

import co.aikar.idb.DB;
import net.crashcraft.crashclaim.data.providers.sqlite.DataVersion;

import java.sql.SQLException;
import java.util.List;

public class DataRev2 implements DataVersion {
    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public void executeUpgrade(int fromRevision) throws SQLException {
        /*
        This code is to fix duplicate entries in the player id table, because of a misused constraint multiple uuid entries were created but only the first is used
        so we need to remove all of the entries after the first and then fix the cosntraint.
         */

        List<String> uuids = DB.getFirstColumnResults("SELECT DISTINCT uuid FROM players");

        for (String uuid : uuids){
            List<Integer> toRemoveList = DB.getFirstColumnResults(
                    "SELECT id FROM players \n" +
                            "\tWHERE uuid = ? \n" +
                            "\tORDER BY id LIMIT (SELECT count(id) FROM players) OFFSET 1", uuid);
            for (int idToRemove : toRemoveList){
                DB.executeUpdate("DELETE FROM players WHERE id = ?", idToRemove);
            }
        }

        DB.executeUpdate("PRAGMA foreign_keys = OFF"); // Turn foreign keys off

        DB.executeUpdate("CREATE TABLE \"players_backup\" (\n" +
                "\t\"id\"\tINTEGER,\n" +
                "\t\"uuid\"\tTEXT NOT NULL,\n" +
                "\t\"username\"\tTEXT,\n" +
                "\tUNIQUE(\"id\",\"uuid\"),\n" +
                "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" +
                ")");

        DB.executeInsert("INSERT INTO players_backup SELECT * FROM players");

        DB.executeUpdate("DROP TABLE players");

        DB.executeUpdate("CREATE TABLE \"players\" (\n" +
                    "\t\"id\"\tINTEGER,\n" +
                    "\t\"uuid\"\tTEXT NOT NULL UNIQUE,\n" +
                    "\t\"username\"\tTEXT,\n" +
                    "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" +
                    ");");

        DB.executeInsert("INSERT INTO players SELECT * FROM players_backup");

        DB.executeUpdate("PRAGMA foreign_keys = ON");  // Undo
    }
}
