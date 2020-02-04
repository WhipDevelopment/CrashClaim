package net.crashcraft.whipclaim.data;

import dev.whip.crashutils.Payment.TransactionRecipe;
import dev.whip.crashutils.Payment.TransactionResponse;
import dev.whip.crashutils.Payment.TransactionType;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.crashcraft.menu.defaultmenus.ConfirmationMenu;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.PermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.child.SubPermissionGroup;
import net.crashcraft.whipclaim.claimobjects.permission.parent.ParentPermissionGroup;
import net.crashcraft.whipclaim.config.ValueConfig;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import net.crashcraft.whipclaim.permissions.PermissionRouter;
import net.crashcraft.whipclaim.permissions.PermissionSetup;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.cache2k.Cache2kBuilder;
import org.cache2k.IntCache;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import static net.crashcraft.whipclaim.data.StaticClaimLogic.getChunkHash;
import static net.crashcraft.whipclaim.data.StaticClaimLogic.getChunkHashFromLocation;

public class ClaimDataManager implements Listener {
    private static FSTConfiguration serializeConf = FSTConfiguration.createDefaultConfiguration();

    private final WhipClaim plugin;
    private final Path dataPath;
    private final Logger logger;

    private PermissionSetup permissionSetup;

    private HashMap<UUID, Set<Integer>> ownedClaims;   // ids of claims t  hat the user has permission to modify - used for menu lookups
    private HashMap<UUID, Set<Integer>> ownedSubClaims;

    private HashMap<Integer, Integer> subClaimLookupParent;

    private IntCache<Claim> claimLookup; // claim id - claim  - First to get called on loads
    private HashMap<UUID, Long2ObjectOpenHashMap<ArrayList<Integer>>> chunkLookup; // Pre load with data from mem

    private int idCounter;

    private boolean isSaving;
    private boolean reSave;

    public ClaimDataManager(WhipClaim plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.isSaving = false;
        this.subClaimLookupParent = new HashMap<>();

        permissionSetup = new PermissionSetup(plugin);

        serializeConf.registerClass(Claim.class, BaseClaim.class, PermissionGroup.class, PermissionSet.class, PlayerPermissionSet.class, GlobalPermissionSet.class);
        dataPath = Paths.get(plugin.getDataFolder().getAbsolutePath(), "ClaimData");

        chunkLookup = new HashMap<>();
        ownedClaims = new HashMap<>();
        ownedSubClaims = new HashMap<>();

        for (World world : Bukkit.getWorlds()){
            chunkLookup.put(world.getUID(), new Long2ObjectOpenHashMap<>());
            logger.info("Loaded " + world.getName() + " into chunk map");
        }

        File dataFolder = dataPath.toFile();
        idCounter = 0;

        if (!dataFolder.exists()){
            if (dataFolder.mkdirs())
                    logger.info("Created data directory.");
        } else {
            logger.info("Starting claim and chunk bulk data load.");

            File[] files = dataFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    try {
                        int temp = Integer.valueOf(file.getName());
                        if (temp > idCounter) {
                            idCounter = temp;
                        }

                        logger.info("Loading claim filename: " + file.getName());

                        Claim claim = readClaim(new FileInputStream(file));

                        logger.info("Loaded claim id: " + claim.getId());

                        loadChunksForClaim(claim);

                        logger.info("Loaded chunks for claim id: " + claim.getId());

                        PermissionGroup permissionGroup = claim.getPerms();
                        ArrayList<SubClaim> subClaims = claim.getSubClaims();

                        if (subClaims != null) {
                            for (SubClaim subClaim : claim.getSubClaims()) {
                                subClaimLookupParent.put(subClaim.getId(), claim.getId());
                            }
                        }

                        for (Map.Entry<UUID, PlayerPermissionSet> entry : permissionGroup.getPlayerPermissions().entrySet()) {
                            UUID uuid = entry.getKey();

                            if (PermissionRouter.getLayeredPermission(claim,
                                    null, uuid, PermissionRoute.MODIFY_CLAIM) == PermState.ENABLED ||
                                    PermissionRouter.getLayeredPermission(claim,
                                            null, uuid, PermissionRoute.MODIFY_PERMISSIONS) == PermState.ENABLED) {
                                ownedClaims.computeIfAbsent(uuid, u -> new HashSet<>());
                                Set<Integer> ids = ownedClaims.get(uuid);
                                ids.add(claim.getId());
                                continue;
                            }

                            if (subClaims != null){
                                for (SubClaim subClaim : subClaims){
                                    PermissionGroup subPerms = subClaim.getPerms();
                                    if (PermissionRouter.getLayeredPermission(subPerms.getPermissionSet(),
                                            subPerms.getPlayerPermissionSet(uuid), PermissionRoute.MODIFY_CLAIM) == PermState.ENABLED ||
                                            PermissionRouter.getLayeredPermission(subPerms.getPermissionSet(),
                                                    subPerms.getPlayerPermissionSet(uuid), PermissionRoute.MODIFY_PERMISSIONS) == PermState.ENABLED) {
                                        ownedSubClaims.computeIfAbsent(uuid, u -> new HashSet<>());
                                        Set<Integer> ids = ownedSubClaims.get(uuid);
                                        ids.add(subClaim.getId());
                                    }
                                }
                            }
                        }

                        logger.info("Loaded claims into admin and owner map for claim id: " + claim.getId());

                        //ValueConfig check to make sure no dinky plugins loaded worlds
                        if (!ValueConfig.MENU_VISUAL_CLAIM_ITEMS.containsKey(claim.getWorld())){
                            ValueConfig.MENU_VISUAL_CLAIM_ITEMS.put(claim.getWorld(), Material.OAK_FENCE);
                        }
                    } catch (NumberFormatException e){
                        logger.warning("Claim file[" + file.getName() + "] had an invalid filename, continuing however that claim will not be loaded.");
                    } catch (FileNotFoundException ex){
                        logger.warning("Claim was not found at file listed by directory");
                    } catch (IOException ex2) {
                        logger.warning("Claim failed to load into memory, skipping. file[ " + file.getName() + " ]");
                        System.out.println(ex2.getMessage());
                        ex2.printStackTrace();
                    } catch (ClassNotFoundException ex3) {
                        logger.warning("Claim class was not found this is fatal and" +
                                " the server will not be able to load claims, corrupted jar file?\n " +
                                "file[ " + file.getName() + " ]\n" +
                                "Stopping server to prevent  issues");

                        Bukkit.shutdown();
                    }
                }
            }
        }

        try {
            claimLookup = new Cache2kBuilder<Integer, Claim>() {}
                    .name("chunkToClaimCache")
                    .storeByReference(true)
                    .loaderThreadCount(3)
                    .disableStatistics(true)
                    .loader((id) -> {
                        File file = new File(Paths.get(dataPath.toString(), id.toString()).toUri());
                        if (file.exists()){
                            FileInputStream stream = new FileInputStream(file);
                            Claim claim = readClaim(stream);

                            fixupOwnerPerms(claim);

                            return claim;
                        }
                        return null;
                    })
                    .buildForIntKey();
        } catch (Exception e){
            e.printStackTrace();
            logger.info("Cache initialized with cache2k");
        }

        logger.info("Starting claim saving routine");
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::saveClaims, 0, 1200);
    }

    public int requestUniqueID(){
        return idCounter+=1;
    }

    public ClaimResponse createClaim(Location maxCorner, Location minCorner, UUID owner){
        if (maxCorner == null || minCorner == null || maxCorner.getWorld() == null){
            return new ClaimResponse(false, ErrorType.CLAIM_LOCATIONS_WERE_NULL);
        }

        if (isTooSmall(maxCorner.getBlockX(), maxCorner.getBlockZ(), minCorner.getBlockX(), minCorner.getBlockZ())){
            return new ClaimResponse(false, ErrorType.TOO_SMALL);
        }

        Claim claim = new Claim(requestUniqueID(),
                maxCorner.getBlockX(),
                maxCorner.getBlockZ(),
                minCorner.getBlockX(),
                minCorner.getBlockZ(),
                maxCorner.getWorld().getUID(),
                new ParentPermissionGroup(null, null, null),
                owner);

        claim.getPerms().setOwner(claim);

        claim.addContribution(owner, ContributionManager.getArea(claim.getMinX(), claim.getMinZ(), claim.getMaxX(), claim.getMaxZ())); //Contribution tracking initial put

        return addClaim(claim) ? new ClaimResponse(true, claim) : new ClaimResponse(false, ErrorType.FILESYSTEM_OR_MEMORY_ERROR);
    }

    public ErrorType resizeClaim(Claim claim, int start_x, int start_z, int end_x, int end_z, Player resizer, Consumer<Boolean> consumer){
        int[] arr = calculateResize(claim.getMinX(), claim.getMaxX(),
                claim.getMinZ(), claim.getMaxZ(), start_x, start_z, end_x, end_z);

        int newMinX = arr[0];
        int newMinZ = arr[2];
        int newMaxX = arr[1];
        int newMaxZ = arr[3];

        for (SubClaim subClaim : claim.getSubClaims()){
            if (!MathUtils.containedInside(newMinX, newMinZ, newMaxX, newMaxZ,
                    subClaim.getMinX(), subClaim.getMinZ(), subClaim.getMaxX(), subClaim.getMaxZ())){
                return ErrorType.OVERLAP_EXISITNG;
            }
        }

        if (isTooSmall(newMaxX, newMaxZ, newMinX, newMinZ)){
            return ErrorType.TOO_SMALL;
        }

        if (arr[4] == 1) {
            int area = ContributionManager.getArea(newMinX, newMinZ, newMaxX, newMaxZ);
            int originalArea = ContributionManager.getArea(claim.getMinX(), claim.getMinZ(), claim.getMaxX(), claim.getMaxZ());

            int difference = area - originalArea;

            if (difference > 0) {
                int price = (int) Math.ceil(difference * ValueConfig.MONEY_PER_BLOCK);
                //Check price with player
                new ConfirmationMenu(resizer,
                        "Confirm Claim Resize",
                        ChatColor.GREEN + "The claim resize will cost: " + ChatColor.YELLOW + price,
                        new ArrayList<>(Arrays.asList("Confirm or deny the resize.")),
                        Material.EMERALD,
                        (player, aBoolean) -> {
                            if (aBoolean) {
                                TransactionRecipe response = WhipClaim.getPlugin().getPayment().makeTransaction(resizer.getUniqueId(), TransactionType.WITHDRAW, "Claim Resize Up", price);
                                if (response.getTransactionStatus() == TransactionResponse.SUCCESS) {
                                    ContributionManager.addContribution(claim, newMinX, newMinZ, newMaxX, newMaxZ, resizer.getUniqueId());  // Contribution tracking
                                    resizeClaimCall(claim, newMinX, newMinZ, newMaxX, newMaxZ);

                                    consumer.accept(true);
                                } else {
                                    //Didnt have enough money or something
                                    player.sendMessage(ChatColor.RED + response.getTransactionError());
                                    consumer.accept(false);
                                }
                            }
                            return "";
                        },
                        player -> {
                            consumer.accept(false);
                            return "";
                        }).open();
            }

            return ErrorType.NONE;
        } else {
            return ErrorType.CANNOT_FLIP_ON_RESIZE;
        }
    }

    private void resizeClaimCall(Claim claim, int newMinX, int newMinZ, int newMaxX, int newMaxZ){
            removeChunksForClaim(claim);

            claim.setMinCornerX(newMinX);
            claim.setMinCornerZ(newMinZ);
            claim.setMaxCornerX(newMaxX);
            claim.setMaxCornerZ(newMaxZ);

            loadChunksForClaim(claim);
    }

    public ErrorType resizeSubClaim(SubClaim subClaim, int start_x, int start_z, int end_x, int end_z){
        int[] arr = calculateResize(subClaim.getMinX(), subClaim.getMaxX(),
                subClaim.getMinZ(), subClaim.getMaxZ(), start_x, start_z, end_x, end_z);

        int newMinX = arr[0];
        int newMinZ = arr[2];
        int newMaxX = arr[1];
        int newMaxZ = arr[3];

        if (isTooSmall(newMaxX, newMaxZ, newMinX, newMinZ)){
            return ErrorType.TOO_SMALL;
        }

        if (arr[4] == 1) {
            subClaim.setMinCornerX(newMinX);
            subClaim.setMinCornerZ(newMinZ);
            subClaim.setMaxCornerX(newMaxX);
            subClaim.setMaxCornerZ(newMaxZ);

            subClaim.setEditing(false);
            return ErrorType.NONE;
        } else {
            subClaim.setEditing(false);
            return ErrorType.CANNOT_FLIP_ON_RESIZE;
        }
    }

    private static int[] calculateResize(int min_x, int max_x, int min_z, int max_z, int Start_x, int Start_z, int End_x, int End_z) {
        /*
            Works for any size changes: corners or sides.
            Note:
            min_x defines the West Line
            max_x defines the East Line

            min_z defines the North Line
            max_z defines the South Line

            NW is now min
            SE is now max
         */

        int newMin_x = min_x;
        int newMax_x = max_x;
        int newMin_z = min_z;
        int newMax_z = max_z;

        int change = 0;

        if (Start_x == min_x) {
            /*Start is West*/
            // is now south
            newMin_x = End_x;
        }

        if (Start_x == max_x) {
            /*Start is East*/
            // is now north
            newMax_x = End_x;
        }

        if (Start_z == min_z) {
            /*Start is North*/
            //  is now west
            newMin_z = End_z;
        }

        if (Start_z == max_z) {
            /*Start is South*/
            // is now east
            newMax_z = End_z;
        }

        if (newMax_x > min_x && newMin_x < max_x && newMax_z > min_z && newMin_z < max_z) {
            min_x = newMin_x;
            max_x = newMax_x;
            min_z = newMin_z;
            max_z = newMax_z;
            change = 1;
        }

        int[] arr = new int[5];

        arr[0] = min_x;
        arr[1] = max_x;
        arr[2] = min_z;
        arr[3] = max_z;

        arr[4] = change;

        return arr;
    }

    public boolean isTooSmall(int maxX, int maxZ, int minX, int minZ){
        return ((maxX - minX) < 4 || (maxZ - minZ) < 4);
    }

    public boolean checkOverLapSurroudningClaims(int claimid, int maxX, int maxZ, int minX, int minZ, UUID world){
        long NWChunkX = minX >> 4;
        long NWChunkZ = minZ >> 4;
        long SEChunkX = maxX >> 4;
        long SEChunkZ = maxZ >> 4;

        ArrayList<Claim> claims = new ArrayList<>();

        for (long zs = NWChunkZ; zs <= SEChunkZ; zs++) {
            for (long xs = NWChunkX; xs <= SEChunkX; xs++) {
                ArrayList<Integer> integers = chunkLookup.get(world).get(getChunkHash(xs, zs));

                if (integers == null) {
                    continue;
                }

                for (int id : integers){
                    if (id == claimid)
                        continue;

                    Claim claim = getClaim(id);
                    if (!claims.contains(claim))
                        claims.add(claim);
                }
            }
        }

        for (Claim claim : claims){
            if (MathUtils.doOverlap(minX, minZ, maxX, maxZ,
                    claim.getMinX(), claim.getMinZ(), claim.getMaxX(), claim.getMaxZ())){
                return true;
            }
        }
        return false;
    }

    private boolean addClaim(Claim claim){ //Should not be called from anywhere else
        File file = new File(Paths.get(dataPath.toString(), Integer.toString(claim.getId())).toUri());

        if (file.exists()){
            logger.warning("Claim file already exists for id: " + claim.getId() + ", aborting");
            return false;
        }

        addOwnedClaim(claim.getOwner(), claim);
        claimLookup.put(claim.getId(), claim);
        loadChunksForClaim(claim);

        fixupOwnerPerms(claim);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveClaim(claim));
        return true;
    }

    public void deleteClaim(Claim claim){
        //Claim Data
        File file = new File(Paths.get(dataPath.toString(), Integer.toString(claim.getId())).toUri());
        file.delete();

        for (Set<Integer> set : ownedClaims.values()){
            set.remove(claim.getId()); // remove from everyone
        }

        //Chunks
        Long2ObjectOpenHashMap<ArrayList<Integer>> chunks = chunkLookup.get(claim.getWorld());

        for (Map.Entry<Long, ArrayList<Integer>> entry : getChunksForClaim(claim).entrySet()){
            ArrayList<Integer> chunkMap = chunks.get(entry.getKey().longValue());
            chunkMap.removeAll(entry.getValue());   //Remove all of the existing claim chunk entries
        }

        for (SubClaim subClaim : claim.getSubClaims()){
            deleteSubClaim(subClaim);
        }
    }

    public void deleteSubClaim(SubClaim subClaim){
        subClaimLookupParent.remove(subClaim.getId());
        for (Set<Integer> set : ownedSubClaims.values()){
            set.remove(subClaim.getId()); //remove from everyone
        }

        Claim parent = subClaim.getParent();
        parent.removeSubClaim(subClaim.getId());
    }

    private void loadChunksForClaim(Claim claim){
        Long2ObjectOpenHashMap<ArrayList<Integer>> map = chunkLookup.get(claim.getWorld());
        for (Map.Entry<Long, ArrayList<Integer>> entry : getChunksForClaim(claim).entrySet()){
            map.computeIfAbsent(entry.getKey(), (id) -> new ArrayList<>());
            ArrayList<Integer> integers = map.get(entry.getKey().longValue());
            integers.add(claim.getId());
        }
    }

    private void removeChunksForClaim(Claim claim){
        Long2ObjectOpenHashMap<ArrayList<Integer>> map = chunkLookup.get(claim.getWorld());
        for (Map.Entry<Long, ArrayList<Integer>> entry : getChunksForClaim(claim).entrySet()){
            map.computeIfAbsent(entry.getKey(), (id) -> new ArrayList<>());
            ArrayList<Integer> integers = map.get(entry.getKey().longValue());
            integers.remove(Integer.valueOf(claim.getId()));
        }
    }

    private HashMap<Long, ArrayList<Integer>> getChunksForClaim(Claim claim){
        HashMap<Long, ArrayList<Integer>> chunks = new HashMap<>();

        long NWChunkX = claim.getMinX() >> 4;
        long NWChunkZ = claim.getMinZ() >> 4;
        long SEChunkX = claim.getMaxX() >> 4;
        long SEChunkZ = claim.getMaxZ() >> 4;

        for (long zs = NWChunkZ; zs <= SEChunkZ; zs++) {
            for (long xs = NWChunkX; xs <= SEChunkX; xs++) {
                long identifier = getChunkHash(xs, zs);

                chunks.computeIfAbsent(identifier, c -> chunks.put(identifier, new ArrayList<>()));

                chunks.get(identifier).add(claim.getId());
            }
        }

        return chunks;
    }

    public ClaimResponse createSubClaim(Player player, Claim claim, Location loc1, Location loc2){
        if (!MathUtils.containedInside(claim.getMinX(), claim.getMinZ(), claim.getMaxX(), claim.getMaxZ(),
                loc1.getBlockX(), loc1.getBlockZ(), loc2.getBlockX(), loc2.getBlockZ())){
            return new ClaimResponse(false, ErrorType.OUT_OF_BOUNDS);
        }

        Location max = StaticClaimLogic.calculateMaxCorner(loc1, loc2);
        Location min = StaticClaimLogic.calculateMinCorner(loc1, loc2);

        if (isTooSmall(max.getBlockX(), max.getBlockZ(), min.getBlockX(), min.getBlockZ())){
            return new ClaimResponse(false, ErrorType.TOO_SMALL);
        }

        for (SubClaim subClaim : claim.getSubClaims()){
            if (MathUtils.doOverlap(subClaim.getMinX(), subClaim.getMinZ(), subClaim.getMaxX(), subClaim.getMaxZ(),
                    max.getBlockX(), max.getBlockZ(), min.getBlockX(), min.getBlockZ())){
                return new ClaimResponse(false, ErrorType.OVERLAP_EXISITNG);
            }
        }

        World world = loc1.getWorld();

        if (world == null) {
            return new ClaimResponse(false, ErrorType.GENERIC);
        }

        SubClaim subClaim = new SubClaim(claim,
                requestUniqueID(),
                max.getBlockX(),
                max.getBlockZ(),
                min.getBlockX(),
                min.getBlockZ(),
                loc1.getWorld().getUID(),
                new SubPermissionGroup(null, null, null));

        PermissionGroup permissionGroup = subClaim.getPerms();

        permissionGroup.setOwner(subClaim);

        permissionGroup.setPlayerPermissionSet(player.getUniqueId(), permissionSetup.getOwnerPermissionSet().clone());

        claim.addSubClaim(subClaim);

        subClaimLookupParent.put(subClaim.getId(), claim.getId());

        claim.setToSave(true);

        return new ClaimResponse(true, subClaim);
    }

    private Claim readClaim(InputStream stream) throws IOException, ClassNotFoundException {
        FSTObjectInput in = new FSTObjectInput(stream);
        Claim result = (Claim)in.readObject();
        in.close();
        return result;
    }

    private void writeClaim(OutputStream stream, Claim toWrite ) throws IOException {
        FSTObjectOutput out = new FSTObjectOutput(stream);
        out.writeObject( toWrite );
        out.close();
    }

    public Claim getClaim(int id){
        return claimLookup.get(id);
    }

    public Claim getParentClaim(int subID){
        return claimLookup.get(subClaimLookupParent.get(subID));
    }

    public void preLoadChunk(UUID world, long seed){
        ArrayList<Integer> claims = chunkLookup.get(world).get(seed);
        if (claims != null){
            claimLookup.prefetchAll(claims, null);
        }
    }

    public void saveClaim(Claim claim){
        File file = new File(Paths.get(dataPath.toString(), Integer.toString(claim.getId())).toUri());
        try {
            FileOutputStream stream = new FileOutputStream(file, false);
            writeClaim(stream, claim);

            claim.setToSave(false);
        } catch (FileNotFoundException ex) {
            logger.warning("[WhipClaim] Claim was attempting to save but could not complete action due to file error. File: " + file.toURI());
        } catch (IOException ex1){
            logger.warning("[WhipClaim[ Claim was attempting to save but could not complete action due to an IO error. File: " + file.toURI());
        }
    }

    public void saveClaims(){
        Collection<Claim> claims = claimLookup.asMap().values();

        if (isSaving){
            logger.warning("Tried to save claims while claims were already being saved. If this is on shutdown ignore it.");
            setReSave(true);
            return;
        }

        setSaving(true);

        logger.info("Starting to save claims");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Claim claim : claims){
                if (claim.isToSave()) {
                    saveClaim(claim);
                }
            }

            logger.info("Finished save successfully");

            this.setSaving(false);

            if (reSave){
                logger.info("ReSaving claims to disk.");

                setReSave(false);
                saveClaims();
            }
        });
    }

    public void saveClaimsSync(){   //Force save all data - shutdown
        Collection<Claim> claims = claimLookup.asMap().values();

        Bukkit.getScheduler().cancelTasks(plugin);  //Stop tasks here to prevent ReSaving old data over new data

        for (Claim claim : claims){
            saveClaim(claim);
        }
    }

    @EventHandler
    void onChunkLoad(ChunkLoadEvent e){
        Chunk chunk = e.getChunk();
        long seed = getChunkHash(chunk.getX(), chunk.getZ());
        preLoadChunk(e.getWorld().getUID(), seed);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    void onWorldLoad(WorldLoadEvent e){  // make sur eon new world loads there is a cache entry
        chunkLookup.putIfAbsent(e.getWorld().getUID(), new Long2ObjectOpenHashMap<>());
    }

    @EventHandler
    void onLeave(PlayerQuitEvent e){
        plugin.getVisualizationManager().cleanup(e.getPlayer());
    }

    public Claim getClaim(int x, int z, UUID world){
        ArrayList<Integer> integers = chunkLookup.get(world).get(getChunkHashFromLocation(x, z));

        if (integers == null)
            return null;

        for (Integer id : integers){
            Claim claim = getClaim(id);

            if (x >= claim.getMinX() && x <= claim.getMaxX()
                    && z >= claim.getMinZ() && z <= claim.getMaxZ()){
                return claim;
            }
        }
        return null;
    }

    public Claim getClaim(Location location) {
        if (location.getWorld() == null)
            return null;

        return getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
    }

    public void fixupOwnerPerms(Claim claim){
        PermissionGroup group = claim.getPerms();
        group.setPlayerPermissionSet(claim.getOwner(), permissionSetup.getOwnerPermissionSet().clone());
    }

    public void addOwnedClaim(UUID uuid, Claim claim){
        ownedClaims.computeIfAbsent(uuid, (u) -> new HashSet<>());
        Set<Integer> set = ownedClaims.get(uuid);
        set.add(claim.getId());
    }

    public void removeOwnedClaim(UUID uuid, Claim claim){
        if (ownedClaims.containsKey(uuid)){
            ownedClaims.get(uuid).remove(claim.getId());
        }
    }

    public void addOwnedSubClaim(UUID uuid, SubClaim claim){
        ownedSubClaims.computeIfAbsent(uuid, (u) -> new HashSet<>());
        Set<Integer> set = ownedSubClaims.get(uuid);
        set.add(claim.getId());
    }

    public void removeOwnedCSublaim(UUID uuid, SubClaim claim){
        if (ownedSubClaims.containsKey(uuid)){
            ownedSubClaims.get(uuid).remove(claim.getId());
        }
    }

    public PermissionSetup getPermissionSetup() {
        return permissionSetup;
    }

    public Long2ObjectOpenHashMap<ArrayList<Integer>> getClaimChunkMap(UUID world){
        return chunkLookup.get(world);
    }

    public ConcurrentMap<Integer, Claim> temporaryTestGetClaimMap(){
        return claimLookup.asMap();
    }

    public HashMap<UUID, Long2ObjectOpenHashMap<ArrayList<Integer>>> temporaryTestGetChunkMap(){
        return chunkLookup;
    }

    public Set<Integer> getOwnedClaims(UUID uuid) {
        return ownedClaims.get(uuid);
    }

    public Set<Integer> getOwnedSubClaims(UUID uuid) {
        return ownedSubClaims.get(uuid);
    }

    public synchronized boolean isSaving() {
        return isSaving;
    }

    public synchronized void setSaving(boolean saving) {
        isSaving = saving;
    }

    public synchronized boolean isReSave() {
        return reSave;
    }

    public synchronized void setReSave(boolean reSave) {
        this.reSave = reSave;
    }

    public IntCache<Claim> getClaimCache(){
        return claimLookup;
    }
}
