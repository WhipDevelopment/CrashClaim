package net.crashcraft.crashclaim.data.providers.sqlite;

import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.DbRow;
import co.aikar.idb.PooledDatabaseOptions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.PermState;
import net.crashcraft.crashclaim.claimobjects.PermissionGroup;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.crashclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.crashclaim.claimobjects.permission.child.SubPermissionGroup;
import net.crashcraft.crashclaim.claimobjects.permission.parent.ParentPermissionGroup;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.data.providers.DataProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.WorldInitEvent;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SQLiteDataProvider implements DataProvider {
    private BiMap<Material, Integer> containerIDMap;

    @Override
    public void init(CrashClaim plugin, ClaimDataManager manager) {
        this.containerIDMap = HashBiMap.create();

        plugin.getDataFolder().mkdirs();

        //Setup DB for future use
        DatabaseOptions options = DatabaseOptions.builder().sqlite(new File(plugin.getDataFolder(), "data.sqlite").getPath()).build();
        Database db = PooledDatabaseOptions.builder().options(options)
                .maxConnections(1)
                .createHikariDatabase();
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

            //Load chunks into compute map

            for (DbRow row : DB.getResults(
                    "Select" +
                            "    claim_data.minX," +
                            "    claim_data.minZ," +
                            "    claim_data.maxX," +
                            "    claim_data.maxZ," +
                            "    claimworlds.uuid," +
                            "    claims.id " +
                            "From" +
                            "    claim_data Inner Join" +
                            "    claimworlds On claimworlds.id = claim_data.world Inner Join" +
                            "    claims On claim_data.id = claims.data")){
                UUID world = UUID.fromString(row.get("uuid"));

                manager.loadChunksForUnLoadedClaim(
                        row.getInt("id"),
                        row.getInt("minX"),
                        row.getInt("minZ"),
                        row.getInt("maxX"),
                        row.getInt("maxZ"),
                        world
                );

                //ValueConfig check to make sure no dinky plugins loaded worlds
                if (!GlobalConfig.visual_menu_items.containsKey(world)){
                    GlobalConfig.visual_menu_items.put(world, Material.OAK_FENCE);
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public Set<Integer> getPermittedClaims(UUID uuid) {
        try {
            Set<Integer> claims = new HashSet<>(DB.getFirstColumnResults("Select" +
                            "    subclaims.claim_id " +
                            "From" +
                            "    claim_data Inner Join" +
                            "    permission_set On claim_data.id = permission_set.data_id Inner Join" +
                            "    players On players.id = permission_set.players_id Inner Join" +
                            "    subclaims On claim_data.id = subclaims.data" +
                            "    inner join claims on claims.id = subclaims.claim_id " +
                            "Where" +
                            "    players.uuid = ? And" +
                            "    permission_set.modifyPermissions = 1 And" +
                            "    permission_set.modifyClaim = 1",
                    uuid.toString()));

            claims.addAll(DB.getFirstColumnResults("Select\n" +
                            "    claims.id\n" +
                            "From\n" +
                            "    claim_data Inner Join\n" +
                            "    permission_set On claim_data.id = permission_set.data_id Inner Join\n" +
                            "    players On players.id = permission_set.players_id Inner Join\n" +
                            "    claims On claim_data.id = claims.data\n" +
                            "Where\n" +
                            "    players.uuid = ? And\n" +
                            "    permission_set.modifyPermissions = 1 And\n" +
                            "    permission_set.modifyClaim = 1",
                    uuid.toString()));

            return claims;
        } catch (SQLException e){
            e.printStackTrace();
        }

        return new HashSet<>();
    }

    @Override
    public Set<Integer> getOwnedParentClaims(UUID uuid) {
        try {
            return new HashSet<>(DB.getFirstColumnResults("Select\n" +
                            "    claims.id\n" +
                            "From\n" +
                            "    claim_data Inner Join\n" +
                            "    permission_set On claim_data.id = permission_set.data_id Inner Join\n" +
                            "    players On players.id = permission_set.players_id Inner Join\n" +
                            "    claims On claim_data.id = claims.data\n" +
                            "Where\n" +
                            "    players.uuid = ?",
                    uuid.toString()));
        } catch (SQLException e){
            e.printStackTrace();
        }

        return new HashSet<>();
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
        CrashClaim.getPlugin().getLogger().info("Saving " + claim.getId());
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
                    claim.getExitMessage(),
                    DataType.CLAIM
            );

            //Claim
            DB.executeUpdate("INSERT OR IGNORE INTO claims(id, data, players_id) VALUES (?, " +
                            "(SELECT id FROM claim_data WHERE minX = ? AND minZ = ? AND maxX = ? AND maxZ = ? AND world = (SELECT id FROM claimworlds WHERE uuid = ?))," +
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
                        subClaim.getExitMessage(),
                        DataType.SUB_CLAIM
                );

                DB.executeUpdate("INSERT OR IGNORE INTO subclaims(id, data, claim_id) VALUES (?, " +
                                "(SELECT id FROM claim_data WHERE minX = ? AND minZ = ? AND maxX = ? AND maxZ = ? AND world = (SELECT id FROM claimworlds WHERE uuid = ?))," +
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

            //Contributions
            for (Map.Entry<UUID, Integer> entry : claim.getContribution().entrySet()) {
                DB.executeUpdate("INSERT INTO contributions(data_id, players_id, amount) VALUES (?, (SELECT id FROM players WHERE uuid = ?), ?) ON CONFLICT (data_id, players_id) DO UPDATE SET amount = ?",
                        claimData_id, entry.getKey().toString(), entry.getValue(), entry.getValue());
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void savePermissions(int data_id, PermissionGroup group) throws SQLException{
        GlobalPermissionSet global = group.getGlobalPermissionSet();

        DB.executeUpdate("INSERT INTO permission_set(data_id, players_id, build, interactions, entities, explosions, entityGrief, teleportation, defaultContainer, viewSubClaims, pistons, fluids) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (data_id, players_id) DO UPDATE SET " +
                        "build = ?, interactions = ?, entities = ?, explosions = ?, entityGrief = ?, teleportation = ?, defaultContainer = ?, viewSubClaims = ?, pistons = ?, fluids = ?",
                data_id,
                -1,
                global.getBuild(),
                global.getInteractions(),
                global.getEntities(),
                global.getExplosions(),
                global.getEntityGrief(),
                global.getTeleportation(),
                global.getDefaultConatinerValue(),
                global.getViewSubClaims(),
                global.getPistons(),
                global.getFluids(),
                //For replacing
                global.getBuild(),
                global.getInteractions(),
                global.getEntities(),
                global.getExplosions(),
                global.getEntityGrief(),
                global.getTeleportation(),
                global.getDefaultConatinerValue(),
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

            DB.executeUpdate("INSERT INTO permission_set(data_id, players_id, build, interactions, entities, teleportation, defaultContainer, viewSubClaims, modifyPermissions, modifyClaim) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (data_id, players_id) DO UPDATE SET " +
                            "build = ?, interactions = ?, entities = ?, teleportation = ?, defaultContainer = ?, viewSubClaims = ?, modifyPermissions = ?, modifyClaim = ?",
                    data_id,
                    player_id,
                    perms.getBuild(),
                    perms.getInteractions(),
                    perms.getEntities(),
                    perms.getTeleportation(),
                    perms.getDefaultConatinerValue(),
                    perms.getViewSubClaims(),
                    perms.getModifyPermissions(),
                    perms.getModifyClaim(),
                    //for replacing
                    perms.getBuild(),
                    perms.getInteractions(),
                    perms.getEntities(),
                    perms.getTeleportation(),
                    perms.getDefaultConatinerValue(),
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

    private void addClaimData(int data_id, int minX, int minZ, int maxX, int maxZ, UUID world, String name, String entryMessage, String exitMessage, DataType type) throws SQLException{
        if (data_id == -1) {
            DB.executeUpdate("INSERT INTO claim_data(minX, minZ, maxX, maxZ, world, name, entryMessage, exitMessage, `type`) VALUES (?, ?, ?, ?, (SELECT id FROM claimworlds WHERE uuid = ?), ?, ?, ?, ?)",
                    minX, minZ, maxX, maxZ, world.toString(), name, entryMessage, exitMessage, type.getType()
            );
        } else {
            DB.executeUpdate("INSERT INTO claim_data(id, minX, minZ, maxX, maxZ, world, name, entryMessage, exitMessage, `type`) VALUES (?, ?, ?, ?, ?, (SELECT id FROM claimworlds WHERE uuid = ?), ?, ?, ?, ?)" +
                            "ON CONFLICT(id) DO UPDATE SET minX = ?, minZ = ?, maxX = ?, maxZ = ?, world = (SELECT id FROM claimworlds WHERE uuid = ?), name = ?, entryMessage = ?, exitMessage = ?",
                    data_id, minX, minZ, maxX, maxZ, world.toString(), name, entryMessage, exitMessage, type.getType(),
                    minX, minZ, maxX, maxZ, world.toString(), name, entryMessage, exitMessage
            );
        }
    }

    @Override
    public void removeClaim(Claim claim) {
        try {
            DB.executeUpdate("DELETE FROM claim_data WHERE id IN (SELECT data FROM subclaims WHERE subclaims.claim_id = ?)\n", claim.getId());
            DB.executeUpdate("DELETE FROM claim_data WHERE id = (SELECT `data` FROM claims WHERE id = ?)", claim.getId());
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public Claim loadClaim(Integer id) {
        try {
            DbRow claimDataRow = DB.getFirstRow(
                    "Select" +
                            "    claim_data.minX," +
                            "    claim_data.minZ," +
                            "    claim_data.maxX," +
                            "    claim_data.id," +
                            "    claim_data.maxZ," +
                            "    claim_data.name," +
                            "    claim_data.entryMessage," +
                            "    claim_data.exitMessage," +
                            "    players.uuid," +
                            "    claimworlds.uuid As world_uuid " +
                            "From " +
                            "    claim_data Inner Join" +
                            "    claims On claim_data.id = claims.data Inner Join" +
                            "    players On players.id = claims.players_id Inner Join" +
                            "    claimworlds On claimworlds.id = claim_data.world " +
                            "Where" +
                            "    claims.id = ?", id);

            Integer data_id = claimDataRow.getInt("id");
            UUID owner = UUID.fromString(claimDataRow.getString("uuid"));

            if (data_id == null){
                return null;
            }

            GlobalPermissionSet globalPermissionSet = getGlobalPermissionSet(data_id);

            PermissionGroup group = new ParentPermissionGroup(null,
                    globalPermissionSet,
                    getPlayerPermissions(data_id)
            );

            UUID world = UUID.fromString(claimDataRow.getString("world_uuid"));

            Claim claim = new Claim(id,
                    claimDataRow.getInt("maxX"),
                    claimDataRow.getInt("maxZ"),
                    claimDataRow.getInt("minX"),
                    claimDataRow.getInt("minZ"),
                    world,
                    group,
                    owner
            );

            claim.setName(claimDataRow.getString("name"), false);
            claim.setEntryMessage(claimDataRow.getString("entryMessage"), false);
            claim.setExitMessage(claimDataRow.getString("exitMessage"), false);

            group.setOwner(claim, false);

            //Contributions
            for (DbRow row : DB.getResults("Select" +
                    "    contributions.amount," +
                    "    players.uuid " +
                    "From" +
                    "    contributions Inner Join" +
                    "    players On players.id = contributions.players_id " +
                    "Where" +
                    "    contributions.data_id = ?", data_id)){
                claim.addContribution(UUID.fromString(row.getString("uuid")),
                        row.getInt("amount"));
            }

            for (DbRow row : DB.getResults("Select" +
                    "    claim_data.minX," +
                    "    claim_data.id," +
                    "    claim_data.minZ," +
                    "    claim_data.maxX," +
                    "    claim_data.maxZ," +
                    "    claim_data.name," +
                    "    claim_data.entryMessage," +
                    "    claim_data.exitMessage," +
                    "    subclaims.id As subClaim_id " +
                    "From" +
                    "    claim_data Inner Join" +
                    "    subclaims On claim_data.id = subclaims.data " +
                    "Where" +
                    "    subclaims.claim_id = ?", id)){

                int subClaimData_id = row.getInt("id");
                GlobalPermissionSet subClaim_globalPermissionSet = getGlobalPermissionSet(subClaimData_id);

                PermissionGroup subClaim_group = new SubPermissionGroup(null,
                        subClaim_globalPermissionSet,
                        getPlayerPermissions(subClaimData_id)
                );

                SubClaim subClaim = new SubClaim(claim,
                        row.getInt("subClaim_id"),
                        row.getInt("maxX"),
                        row.getInt("maxZ"),
                        row.getInt("minX"),
                        row.getInt("minZ"),
                        world,
                        subClaim_group
                );

                subClaim.setName(row.getString("name"), false);
                subClaim.setEntryMessage(row.getString("entryMessage"), false);
                subClaim.setExitMessage(row.getString("exitMessage"), false);

                subClaim_group.setOwner(subClaim, false);

                claim.addSubClaim(subClaim);
            }

            claim.setEditing(false);

            return claim;
        } catch (Exception e){
            CrashClaim.getPlugin().getLogger().severe("There was a fatal error while loading a claim with id: " + id);
            e.printStackTrace();
        }

        return null;
    }

    private GlobalPermissionSet getGlobalPermissionSet(int data_id) throws SQLException{
        DbRow globalPermissionRow = DB.getFirstRow("SELECT build, interactions, entities, explosions, entityGrief, teleportation, viewSubClaims, pistons, fluids, defaultContainer FROM permission_set " +
                "WHERE data_id = ? AND players_id = -1", data_id);

        HashMap<Material, Integer> globalContainers = new HashMap<>();
        for (DbRow globalContainerRow : DB.getResults("SELECT container, value FROM permission_containers WHERE player_id = -1 AND data_id = ?", data_id)){
            globalContainers.put(containerIDMap.inverse().get(globalContainerRow.getInt("container")), globalContainerRow.getInt("value"));
        }

        return new GlobalPermissionSet(
                globalPermissionRow.getInt("build"),
                globalPermissionRow.getInt("interactions"),
                globalPermissionRow.getInt("entities"),
                globalPermissionRow.getInt("explosions"),
                globalPermissionRow.getInt("entityGrief"),
                globalPermissionRow.getInt("teleportation"),
                globalPermissionRow.getInt("viewSubClaims"),
                globalContainers,
                globalPermissionRow.getInt("defaultContainer"),
                globalPermissionRow.getInt("pistons"),
                globalPermissionRow.getInt("fluids")
        );
    }

    private HashMap<UUID, PlayerPermissionSet> getPlayerPermissions(int data_id) throws SQLException{
        HashMap<UUID, PlayerPermissionSet> perms = new HashMap<>();

        for (DbRow permissionRow : DB.getResults("SELECT players_id, (SELECT uuid FROM players WHERE players.id = permission_set.players_id) AS uuid, build, interactions, entities, teleportation, viewSubClaims, defaultContainer, modifyPermissions, modifyClaim FROM permission_set " +
                "WHERE data_id = ? AND players_id IS NOT -1", data_id)){

            HashMap<Material, Integer> containers = new HashMap<>();
            for (DbRow globalContainerRow : DB.getResults("SELECT container, value FROM permission_containers WHERE player_id = ? AND data_id = ?",
                    permissionRow.getInt("players_id"), data_id)){
                containers.put(containerIDMap.inverse().get(globalContainerRow.getInt("container")), globalContainerRow.getInt("value"));
            }

            PlayerPermissionSet set = new PlayerPermissionSet(
                    permissionRow.getInt("build"),
                    permissionRow.getInt("interactions"),
                    permissionRow.getInt("entities"),
                    PermState.ENABLED, // TODO not used
                    permissionRow.getInt("teleportation"),
                    permissionRow.getInt("viewSubClaims"),
                    containers,
                    permissionRow.getInt("defaultContainer"),
                    permissionRow.getInt("modifyPermissions"),
                    permissionRow.getInt("modifyClaim")
            );

            perms.put(UUID.fromString(permissionRow.getString("uuid")), set);
        }

        return perms;
    }
}
