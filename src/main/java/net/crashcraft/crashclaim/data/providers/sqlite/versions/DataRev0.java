package net.crashcraft.crashclaim.data.providers.sqlite.versions;

import co.aikar.idb.DB;
import net.crashcraft.crashclaim.data.providers.sqlite.DataVersion;

import java.sql.SQLException;

public class DataRev0 implements DataVersion {
    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void executeUpgrade(int fromRevision) throws SQLException {
        DB.executeUpdate("CREATE TABLE \"claim_data\" (\n" +
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
        DB.executeUpdate("CREATE TABLE \"claims\" (\n" +
                "\t\"id\"\tINTEGER UNIQUE,\n" +
                "\t\"data\"\tINTEGER NOT NULL,\n" +
                "\t\"players_id\"\tINTEGER NOT NULL,\n" +
                "\tPRIMARY KEY(\"id\"),\n" +
                "\tFOREIGN KEY(\"data\") REFERENCES \"claim_data\"(\"id\") ON DELETE CASCADE,\n" +
                "\tFOREIGN KEY(\"players_id\") REFERENCES \"players\"(\"id\") ON DELETE CASCADE\n" +
                ")");
        DB.executeUpdate("CREATE TABLE \"claimworlds\" (\n" +
                "\t\"id\"\tINTEGER,\n" +
                "\t\"uuid\"\tTEXT NOT NULL UNIQUE,\n" +
                "\t\"name\"\tTEXT NOT NULL,\n" +
                "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" +
                ")");
        DB.executeUpdate("CREATE TABLE \"permission_containers\" (\n" +
                "\t\"data_id\"\tINTEGER NOT NULL,\n" +
                "\t\"player_id\"\tINTEGER NOT NULL,\n" +
                "\t\"container\"\tINTEGER NOT NULL,\n" +
                "\t\"value\"\tINTEGER NOT NULL,\n" +
                "\tPRIMARY KEY(\"data_id\",\"player_id\",\"container\"),\n" +
                "\tFOREIGN KEY(\"player_id\") REFERENCES \"players\"(\"id\") ON DELETE CASCADE,\n" +
                "\tFOREIGN KEY(\"container\") REFERENCES \"permissioncontainers\"(\"id\") ON DELETE CASCADE,\n" +
                "\tFOREIGN KEY(\"data_id\") REFERENCES \"claim_data\"(\"id\") ON DELETE CASCADE\n" +
                ")");
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
                "\t\"pistons\"\tINTEGER,\n" +
                "\t\"fluids\"\tINTEGER,\n" +
                "\t\"modifyPermissions\"\tINTEGER,\n" +
                "\t\"modifyClaim\"\tINTEGER,\n" +
                "\tFOREIGN KEY(\"data_id\") REFERENCES \"claim_data\"(\"id\") ON DELETE CASCADE,\n" +
                "\tFOREIGN KEY(\"players_id\") REFERENCES \"players\"(\"id\") ON DELETE CASCADE,\n" +
                "\tPRIMARY KEY(\"data_id\",\"players_id\")\n" +
                ")");
        DB.executeUpdate("CREATE TABLE \"permissioncontainers\" (\n" +
                "\t\"id\"\tINTEGER,\n" +
                "\t\"identifier\"\tTEXT NOT NULL UNIQUE,\n" +
                "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" +
                ")");
        DB.executeUpdate("CREATE TABLE \"players\" (\n" +
                "\t\"id\"\tINTEGER,\n" +
                "\t\"uuid\"\tTEXT NOT NULL,\n" +
                "\t\"username\"\tTEXT,\n" +
                "\tUNIQUE(\"id\",\"uuid\"),\n" +
                "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" +
                ")");
        DB.executeUpdate("CREATE TABLE \"properties\" (\n" +
                "\t\"key\"\tTEXT UNIQUE NOT NULL,\n" +
                "\t\"value\"\tTEXT NOT NULL,\n" +
                "\tUNIQUE(\"value\",\"key\")\n" +
                ")");
        DB.executeUpdate("CREATE TABLE \"subclaims\" (\n" +
                "\t\"id\"\tINTEGER,\n" +
                "\t\"data\"\tINTEGER NOT NULL,\n" +
                "\t\"claim_id\"\tINTEGER NOT NULL,\n" +
                "\tPRIMARY KEY(\"id\"),\n" +
                "\tFOREIGN KEY(\"data\") REFERENCES \"claim_data\"(\"id\") ON DELETE CASCADE,\n" +
                "\tFOREIGN KEY(\"claim_id\") REFERENCES \"claims\"(\"id\") ON DELETE CASCADE\n" +
                ")");
    }
}
