package net.crashcraft.whipclaim.data.providers.sqlite;

import co.aikar.idb.*;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.PermissionGroup;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.PermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.data.providers.DataProvider;
import net.crashcraft.whipclaim.menus.helpers.StaticItemLookup;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.WorldInitEvent;
import sun.security.x509.DNSName;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class SQLiteDataProvider implements DataProvider {
    private Logger logger;

    private DatabaseManager databaseManager;

    private HashMap<Material, Integer> containerIDMap;

    @Override
    public void init(WhipClaim plugin, ClaimDataManager manager) {
        this.logger = plugin.getLogger();
        this.containerIDMap = new HashMap<>();

        plugin.getDataFolder().mkdirs();

        //Setup DB for future use
        DatabaseOptions options = DatabaseOptions.builder().sqlite(new File(plugin.getDataFolder(), "data.sqlite").getPath()).build();
        Database db = PooledDatabaseOptions.builder().options(options).createHikariDatabase();
        DB.setGlobalDatabase(db);

        new DatabaseManager(plugin); // Initialize and get data schema up to date
        try {
            DB.executeUpdate("REPLACE INTO players(id, uuid) VALUES (?, ?)", -1, "00000000-0000-0000-0000-000000000000"); // Add fake user to use as global user for unique checks

            for (World world : Bukkit.getWorlds()){
                addWorld(world);
            }

            for (Material material : manager.getPermissionSetup().getTrackedContainers()){
                DB.executeUpdateAsync("INSERT OR IGNORE INTO permissioncontainers(identifier) VALUES (?)", material.name());
            }

            for (DbRow row : DB.getResults("SELECT id, identifier FROM permissioncontainers")){
                Material material = Material.getMaterial(row.getString("identifier"));
                containerIDMap.put(material, row.getInt("id"));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldAdd(WorldInitEvent e){
        addWorld(e.getWorld());
    }

    private void addWorld(World world){
        DB.executeUpdateAsync("REPLACE INTO claimworlds(uuid, name) VALUES (?, ?)", world.getUID().toString(), world.getName());
    }

    @Override
    public boolean preInitialSave(Claim claim) {
        return true; // no need for any initial setup
    }

    @Override
    public void saveClaim(Claim claim) {
        try {
            addPlayer(claim.getOwner()); //Make sure owner is in db

            //Claim Data
            addClaimData(
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
            DB.executeUpdate("REPLACE INTO claims(id, data, players_id) VALUES (?, " +
                            "(SELECT id FROM claim_data WHERE minX = ? AND minZ = ? AND maxX = ? AND maxZ = ? AND world = ?)," +
                            "(SELECT id FROM players WHERE uuid = ?))",
                    claim.getId(),
                    claim.getMinX(), claim.getMinZ(), claim.getMaxX(), claim.getMaxZ(), claim.getWorld().toString(),
                    claim.getOwner()
            );

            //Claim permissions
            int claimData_id = DB.getFirstColumn("SELECT data FROM claims WHERE claims.id = ?", claim.getId());
            savePermissions(claimData_id, claim.getPerms());

            //Sub Claim
            for (SubClaim subClaim : claim.getSubClaims()) {
                addClaimData(
                        subClaim.getMinX(),
                        subClaim.getMinZ(),
                        subClaim.getMaxX(),
                        subClaim.getMaxZ(),
                        subClaim.getWorld(),
                        subClaim.getName(),
                        subClaim.getEntryMessage(),
                        subClaim.getExitMessage()
                );

                DB.executeUpdate("REPLACE INTO subclaims(id, data, claim_id) VALUES (?, " +
                                "(SELECT id FROM claim_data WHERE minX = ? AND minZ = ? AND maxX = ? AND maxZ = ? AND world = ?)," +
                                "?)",
                        subClaim.getId(),
                        subClaim.getMinX(), subClaim.getMinZ(), subClaim.getMaxX(), subClaim.getMaxZ(), subClaim.getWorld().toString(),
                        subClaim.getParent().getId()
                );

                int subClaimData_id = DB.getFirstColumn("SELECT data FROM subclaims WHERE subclaims.id = ?", subClaim.getId());
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

        DB.executeUpdate("REPLACE INTO permission_set(data_id, players_id, build, interactions, entities, explosions, teleportation, viewSubClaims, pistons, fluids) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                data_id,
                -1,
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

            DB.executeUpdate("REPLACE INTO permission_set(data_id, players_id, build, interactions, entities, teleportation, viewSubClaims, modifyPermissions, modifyClaim) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    data_id,
                    player_id,
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
        int permission_id = DB.getFirstColumn("SELECT id FROM permission_set WHERE data_id = ? AND players_id = ?",  data_id, player_id);

        DB.executeUpdate("DELETE FROM permission_containers WHERE permission_id = ?", permission_id); // Remove old containers as there is no constraint

        for (Map.Entry<Material, Integer> entry : containers.entrySet()){
            int material_id = containerIDMap.get(entry.getKey());

            DB.executeInsert("REPLACE INTO permission_containers(permission_id, container, value) VALUES (?, ?, ?)",
                    permission_id, material_id, entry.getValue());
        }
    }

    private void addPlayer(UUID uuid) throws SQLException{
        DB.executeInsert("INSERT OR IGNORE INTO players(uuid) VALUES (?)",
                uuid.toString()
        );
    }

    private void addClaimData(int minX, int minZ, int maxX, int maxZ, UUID world, String name, String entryMessage, String exitMessage) throws SQLException{
        DB.executeUpdate("REPLACE INTO claim_data(minX, minZ, maxX, maxZ, world, name, entryMessage, exitMessage) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                minX, minZ, maxX, maxZ, world.toString(), name, entryMessage, exitMessage
        );
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
