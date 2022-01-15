package net.crashcraft.crashclaim.data.providers.sqlite.versions;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import net.crashcraft.crashclaim.data.providers.sqlite.DataType;
import net.crashcraft.crashclaim.data.providers.sqlite.DataVersion;

import java.sql.SQLException;
import java.util.List;

public class DataRev4 implements DataVersion {
    @Override
    public int getVersion() {
        return 4;
    }

    @Override
    public void executeUpgrade(int fromRevision) throws SQLException {
        DB.executeUpdate("PRAGMA foreign_keys = OFF"); // Turn foreign keys off

        DB.executeUpdate("CREATE TABLE \"claim_data_backup\" (\n" +
                "\t\"id\"\tINTEGER,\n" +
                "\t\"minX\"\tINTEGER NOT NULL,\n" +
                "\t\"minZ\"\tINTEGER NOT NULL,\n" +
                "\t\"maxX\"\tINTEGER NOT NULL,\n" +
                "\t\"maxZ\"\tINTEGER NOT NULL,\n" +
                "\t\"world\"\tINTEGER NOT NULL,\n" +
                "\t\"name\"\tTEXT,\n" +
                "\t\"entryMessage\"\tTEXT,\n" +
                "\t\"exitMessage\"\tTEXT,\n" +
                "\tUNIQUE(\"minX\",\"minZ\",\"maxX\",\"maxZ\",\"world\"),\n" +
                "\tPRIMARY KEY(\"id\" AUTOINCREMENT),\n" +
                "\tFOREIGN KEY(\"world\") REFERENCES \"claimworlds\"(\"id\") ON DELETE CASCADE\n" +
                ")");

        DB.executeInsert("INSERT INTO claim_data_backup SELECT * FROM claim_data");

        DB.executeUpdate("DROP TABLE claim_data");

        DB.executeUpdate("CREATE TABLE \"claim_data\" (\n" +
                "\t\"id\"\tINTEGER,\n" +
                "\t\"type\"\tINTEGER NOT NULL,\n" +
                "\t\"minX\"\tINTEGER NOT NULL,\n" +
                "\t\"minZ\"\tINTEGER NOT NULL,\n" +
                "\t\"maxX\"\tINTEGER NOT NULL,\n" +
                "\t\"maxZ\"\tINTEGER NOT NULL,\n" +
                "\t\"world\"\tINTEGER NOT NULL,\n" +
                "\t\"name\"\tTEXT,\n" +
                "\t\"entryMessage\"\tTEXT,\n" +
                "\t\"exitMessage\"\tTEXT,\n" +
                "\tFOREIGN KEY(\"world\") REFERENCES \"claimworlds\"(\"id\") ON DELETE CASCADE,\n" +
                "\tPRIMARY KEY(\"id\" AUTOINCREMENT),\n" +
                "\tUNIQUE(\"minX\",\"minZ\",\"maxX\",\"maxZ\",\"world\",\"type\")\n" +
                ")");

        for (DbRow row : DB.getResults("SELECT id, `data` FROM claims")){
            if (row == null){
                continue;
            }

            Integer id = row.getInt("id");
            Integer data = row.getInt("data");

            if (id == null || data == null){
                continue;
            }

            DB.executeInsert("INSERT INTO claim_data(id, minX, minZ, maxX, maxZ, world, name, entryMessage, exitMessage, `type`) SELECT *, ? FROM claim_data_backup WHERE id = ?",
                    DataType.CLAIM.getType(), data);

            List<Integer> subClaims = DB.getFirstColumnResults("SELECT `data` FROM subclaims WHERE claim_id = ?", id);
            for (Integer subData : subClaims){
                if (subData == null){
                    continue;
                }

                DB.executeInsert("INSERT INTO claim_data(id, minX, minZ, maxX, maxZ, world, name, entryMessage, exitMessage, `type`) SELECT *, ? FROM claim_data_backup WHERE id = ?",
                        DataType.SUB_CLAIM.getType(), subData);
            }
        }

        DB.executeUpdate("PRAGMA foreign_keys = ON");  // Undo
    }
}
