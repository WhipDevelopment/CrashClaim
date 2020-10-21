package net.crashcraft.whipclaim.data.providers.sqlite;

import co.aikar.idb.*;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.PermState;
import net.crashcraft.whipclaim.claimobjects.PermissionGroup;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.data.providers.DataProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.WorldInitEvent;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

public class SQLiteDataProvider implements DataProvider {
    private HashMap<Material, Integer> containerIDMap;

    @Override
    public void init(WhipClaim plugin, ClaimDataManager manager) {
        this.containerIDMap = new HashMap<>();

        plugin.getDataFolder().mkdirs();

        //Setup DB for future use
        DatabaseOptions options = DatabaseOptions.builder().sqlite(new File(plugin.getDataFolder(), "data.sqlite").getPath()).build();
        Database db = PooledDatabaseOptions.builder().options(options).createHikariDatabase();
        DB.setGlobalDatabase(db);

        new DatabaseManager(plugin); // Initialize and get data schema up to date
        try {
            Integer playerID = DB.getFirstColumn("SELECT id FROM players WHERE uuid = ?", "00000000-0000-0000-0000-000000000000");
            if (playerID == null) {
                DB.executeUpdate("INSERT OR IGNORE INTO players(id, uuid) VALUES (?, ?);", -1, "00000000-0000-0000-0000-000000000000"); // Add fake user to use as global user for unique checks
            }

            for (World world : Bukkit.getWorlds()){
                addWorld(world);
            }

            for (Material material : manager.getPermissionSetup().getTrackedContainers()){
                Integer id = DB.getFirstColumn("SELECT id FROM permissioncontainers WHERE identifier = ?", material.name());
                if (id == null) {
                    DB.executeUpdate("INSERT OR IGNORE INTO permissioncontainers(identifier) VALUES (?)", material.name());
                }
            }

            for (DbRow row : DB.getResults("SELECT id, identifier FROM permissioncontainers")){
                Material material = Material.getMaterial(row.getString("identifier"));
                containerIDMap.put(material, row.getInt("id"));
            }

            //Setup ID Counter
            Integer maxClaimID = DB.getFirstColumn("SELECT max(id) FROM claims");
            Integer maxSubClaimID = DB.getFirstColumn("SELECT max(id) FROM subclaims");
            int idCount = maxClaimID != null ? maxClaimID : 0;
            if (maxSubClaimID != null && maxSubClaimID > idCount){
                idCount = maxSubClaimID;
            }
            idCount++;
            manager.setIdCounter(idCount);

            //Compute Owned Claims Maps for fast lookups
            computeOwnedClaimData(
                    DB.getResults("SELECT id, data FROM claims"),
                    manager.getAllOwnedClaims()
            );

            computeOwnedClaimData(
                    DB.getResults("SELECT id, data FROM subclaims"),
                    manager.getAllOwnedSubClaims()
            );

            //Load chunks into compute map
            for (DbRow row : DB.getResults("SELECT minX, minZ, maxX, maxZ, (SELECT uuid FROM claimworlds WHERE id = claim_data.world), (SELECT id FROM claims WHERE data = claim_data.id) FROM claim_data")){
                manager.loadChunksForUnLoadedClaim(
                        row.getInt("id"),
                        row.getInt("minX"),
                        row.getInt("minZ"),
                        row.getInt("maxX"),
                        row.getInt("maxZ"),
                        UUID.fromString(row.get("uuid"))
                );
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void computeOwnedClaimData(List<DbRow> data, HashMap<UUID, Set<Integer>> map) throws SQLException{
        for (DbRow row : data){
            int claim_id = row.getInt("id");
            int data_id = row.getInt("data");

            List<String> uuids = DB.getFirstColumnResults("SELECT uuid FROM players WHERE id = " +
                            "(SELECT players_id FROM permission_set WHERE data_id = ? AND (modifyClaim = ? OR modifyPermissions = ?))",
                    data_id,
                    PermState.ENABLED,
                    PermState.ENABLED
            );

            for (String uuidString : uuids){
                UUID uuid = UUID.fromString(uuidString);
                map.computeIfAbsent(uuid, u -> new HashSet<>());
                Set<Integer> ids = map.get(uuid);
                ids.add(claim_id);
            }
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldAdd(WorldInitEvent e){
        try {
            addWorld(e.getWorld());
        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    private void addWorld(World world) throws SQLException{
        DbRow row = DB.getFirstRow("SELECT name FROM claimworlds WHERE uuid = ?", world.getUID().toString());
        if (row == null || !row.getString("name").equals(world.getName())){
            DB.executeUpdateAsync("INSERT INTO claimworlds(uuid, name) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET name = ?;",
                    world.getUID().toString(),
                    world.getName(), world.getName());
        }
    }

    @Override
    public boolean preInitialSave(Claim claim) {
        return true; // no need for any initial setup
    }

    @Override
    public void saveClaim(Claim claim) {
        try {
            addPlayer(claim.getOwner()); //Make sure owner is in db

            Integer claimData_id = DB.getFirstColumn("SELECT data FROM claims WHERE id = ?", claim.getId());

            if (claimData_id == null){
                claimData_id = -1;
            }

            //Claim Data
            addClaimData(
                    claimData_id,
                    claim.getMinX(),
                    claim.getMinZ(),
                    claim.getMaxX(),
                    claim.getMaxZ(),
                    claim.getWorld(),
                    claim.getName(),
                    claim.getEntryMessage(),
                    claim.getExitMessage()
            );

            //Claim
            DB.executeUpdate("INSERT OR IGNORE INTO claims(id, data, players_id) VALUES (?, " +
                            "(SELECT id FROM claim_data WHERE minX = ? AND minZ = ? AND maxX = ? AND maxZ = ? AND world = ?)," +
                            "(SELECT id FROM players WHERE uuid = ?))",
                    claim.getId(),
                    claim.getMinX(), claim.getMinZ(), claim.getMaxX(), claim.getMaxZ(), claim.getWorld().toString(),
                    claim.getOwner()
            );

            //Claim permissions
            if (claimData_id == -1) {
                claimData_id = DB.getFirstColumn("SELECT data FROM claims WHERE claims.id = ?", claim.getId());
            }
            savePermissions(claimData_id, claim.getPerms());

            //Sub Claim
            for (SubClaim subClaim : claim.getSubClaims()) {
                Integer subClaimData_id = DB.getFirstColumn("SELECT data FROM subclaims WHERE subclaims.id = ?", subClaim.getId());

                if (subClaimData_id == null){
                    subClaimData_id = -1;
                }

                addClaimData(
                        subClaimData_id,
                        subClaim.getMinX(),
                        subClaim.getMinZ(),
                        subClaim.getMaxX(),
                        subClaim.getMaxZ(),
                        subClaim.getWorld(),
                        subClaim.getName(),
                        subClaim.getEntryMessage(),
                        subClaim.getExitMessage()
                );

                DB.executeUpdate("INSERT OR IGNORE INTO subclaims(id, data, claim_id) VALUES (?, " +
                                "(SELECT id FROM claim_data WHERE minX = ? AND minZ = ? AND maxX = ? AND maxZ = ? AND world = ?)," +
                                "?)",
                        subClaim.getId(),
                        subClaim.getMinX(), subClaim.getMinZ(), subClaim.getMaxX(), subClaim.getMaxZ(), subClaim.getWorld().toString(),
                        subClaim.getParent().getId()
                );

                if (subClaimData_id == -1) {
                    subClaimData_id = DB.getFirstColumn("SELECT data FROM subclaims WHERE subclaims.id = ?", subClaim.getId());
                }
                savePermissions(subClaimData_id, subClaim.getPerms());
            }

            //Fetch and delete outdated subClaims
            List<Integer> list = DB.getFirstColumnResults("SELECT id FROM subclaims WHERE claim_id = ?", claim.getId());
            for (SubClaim subClaim : claim.getSubClaims()){
                list.remove(Integer.valueOf(subClaim.getId()));
            }

            //Remove deleted subClaim data from database
            for (Integer removableSubClaim : list){
                DB.executeUpdate("DELETE FROM claim_data WHERE id = (SELECT data FROM subclaims WHERE subclaims.id = ?)", removableSubClaim);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void savePermissions(int data_id, PermissionGroup group) throws SQLException{
        GlobalPermissionSet global = group.getGlobalPermissionSet();

        DB.executeUpdate("INSERT INTO permission_set(data_id, players_id, build, interactions, entities, explosions, teleportation, viewSubClaims, pistons, fluids) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (data_id, players_id) DO UPDATE SET " +
                        "build = ?, interactions = ?, entities = ?, explosions = ?, teleportation = ?, viewSubClaims = ?, pistons = ?, fluids = ?",
                data_id,
                -1,
                global.getBuild(),
                global.getInteractions(),
                global.getEntities(),
                global.getExplosions(),
                global.getTeleportation(),
                global.getViewSubClaims(),
                global.getPistons(),
                global.getFluids(),
                //For replacing
                global.getBuild(),
                global.getInteractions(),
                global.getEntities(),
                global.getExplosions(),
                global.getTeleportation(),
                global.getViewSubClaims(),
                global.getPistons(),
                global.getFluids()
        );

        addContainers(data_id, -1, global.getContainers());

        for (Map.Entry<UUID, PlayerPermissionSet> entry : group.getPlayerPermissions().entrySet()){
            UUID uuid = entry.getKey();
            PlayerPermissionSet perms = entry.getValue();

            addPlayer(uuid); // Make sure player is in db

            int player_id = DB.getFirstColumn("SELECT id FROM players WHERE uuid = ?", uuid.toString());

            DB.executeUpdate("INSERT INTO permission_set(data_id, players_id, build, interactions, entities, teleportation, viewSubClaims, modifyPermissions, modifyClaim) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (data_id, players_id) DO UPDATE SET " +
                            "build = ?, interactions = ?, entities = ?, teleportation = ?, viewSubClaims = ?, modifyPermissions = ?, modifyClaim = ?",
                    data_id,
                    player_id,
                    perms.getBuild(),
                    perms.getInteractions(),
                    perms.getEntities(),
                    perms.getTeleportation(),
                    perms.getViewSubClaims(),
                    perms.getModifyPermissions(),
                    perms.getModifyClaim(),
                    //for replacing
                    perms.getBuild(),
                    perms.getInteractions(),
                    perms.getEntities(),
                    perms.getTeleportation(),
                    perms.getViewSubClaims(),
                    perms.getModifyPermissions(),
                    perms.getModifyClaim()
            );

            addContainers(data_id, player_id, perms.getContainers());
        }
    }

    private void addContainers(int data_id, int player_id, HashMap<Material, Integer> containers) throws SQLException{
        for (Map.Entry<Material, Integer> entry : containerIDMap.entrySet()){
            Material material = entry.getKey();
            int container_id = entry.getValue();
            Integer data = containers.get(material);
            if (data == null){
                DB.executeUpdate("DELETE FROM permission_containers WHERE data_id = ? AND player_id = ? AND container = ?",
                        data_id,
                        player_id,
                        container_id); // Remove old containers as there is no constraint
            } else {
                DB.executeInsert("INSERT INTO permission_containers(data_id, player_id, container, value) VALUES (?, ?, ?, ?)" +
                        "ON CONFLICT(data_id, player_id, container) DO UPDATE SET value = ?",
                        data_id,
                        player_id,
                        container_id,
                        data,
                        data);
            }
        }
    }

    private void addPlayer(UUID uuid) throws SQLException{
        DB.executeInsert("INSERT OR IGNORE INTO players(uuid) VALUES (?)",
                uuid.toString()
        );
    }

    private void addClaimData(int data_id, int minX, int minZ, int maxX, int maxZ, UUID world, String name, String entryMessage, String exitMessage) throws SQLException{
        if (data_id == -1) {
            DB.executeUpdate("INSERT INTO claim_data(minX, minZ, maxX, maxZ, world, name, entryMessage, exitMessage) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    minX, minZ, maxX, maxZ, world.toString(), name, entryMessage, exitMessage
            );
        } else {
            DB.executeUpdate("INSERT INTO claim_data(id, minX, minZ, maxX, maxZ, world, name, entryMessage, exitMessage) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                            "ON CONFLICT(id) DO UPDATE SET minX = ?, minZ = ?, maxX = ?, maxZ = ?, world = ?, name = ?, entryMessage = ?, exitMessage = ?",
                    data_id, minX, minZ, maxX, maxZ, world.toString(), name, entryMessage, exitMessage,
                    minX, minZ, maxX, maxZ, world.toString(), name, entryMessage, exitMessage
            );
        }
    }

    @Override
    public void removeClaim(Claim claim) {
        DB.executeUpdateAsync("DELETE FROM claims WHERE id = ?", claim.getId());
    }

    @Override
    public Claim loadClaim(Integer id) {
        return null;
    }
}
