package net.crashcraft.whipclaim.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.*;
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
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.cache2k.Cache;
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

    private HashMap<UUID, ArrayList<Integer>> ownedClaims;   // ids of claims t  hat the user has permission to modify - used for menu lookups
    private HashMap<UUID, ArrayList<Integer>> ownedSubClaims;

    private HashMap<UUID, Material> materialLookup;

    private IntCache<Claim> claimLookup; // claim id - claim  - First to get called on loads
    private HashMap<UUID, Long2ObjectOpenHashMap<ArrayList<Integer>>> chunkLookup; // Pre load with data from mem

    private int idCounter;

    private boolean isSaving;
    private boolean reSave;

    public ClaimDataManager(WhipClaim plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.isSaving = false;

        permissionSetup = new PermissionSetup(plugin);

        serializeConf.registerClass(Claim.class, BaseClaim.class, PermissionGroup.class, PermissionSet.class, PlayerPermissionSet.class, GlobalPermissionSet.class);
        dataPath = Paths.get(plugin.getDataFolder().getAbsolutePath(), "ClaimData");

        chunkLookup = new HashMap<>();
        ownedClaims = new HashMap<>();
        ownedSubClaims = new HashMap<>();
        materialLookup = new HashMap<>();

        for (World world : Bukkit.getWorlds()){
            chunkLookup.put(world.getUID(), new Long2ObjectOpenHashMap<>());
            logger.info("Loaded " + world.getName() + " into chunk map");
        }

        /*
        menu:
  visualize:
    visualize-claim-items:
         */

        FileConfiguration config = plugin.getConfig();

        ConfigurationSection section = config.getConfigurationSection("menu.visualize.visualize-claim-items");

        if (section == null){
            section = config.createSection("menu.visualize.visualize-claim-items");

            for (World world : Bukkit.getWorlds()){
                section.set(world.getName(), Material.GRASS.name());
                logger.info("World name not found, adding with GRASS");
                materialLookup.put(world.getUID(), Material.GRASS);
            }

            config.set("menu.visualize.visualize-claim-items", section);
        } else {
            for (String key : section.getKeys(true)) {
                String name = config.getString("menu.visualize.visualize-claim-items." + key);

                if (name == null) {
                    continue;
                }

                World world = Bukkit.getWorld(name);

                if (world == null) {
                    logger.warning("World name for menu.visualize.visualize-claim-items." + key + " is not valid.");
                    continue;
                }

                Material material = Material.getMaterial(name);

                if (material == null) {
                    logger.warning("Material for menu.visualize.visualize-claim-items." + key + " is not a valid material. Defaulting to OAK_FENCE");
                    continue;
                }

                materialLookup.put(world.getUID(), material);
            }

            for (World world : Bukkit.getWorlds()){
                if (!materialLookup.containsKey(world.getUID())){
                    config.set(world.getName(), Material.GRASS.name());
                    materialLookup.put(world.getUID(), Material.GRASS);
                }
            }
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

                        for (Map.Entry<UUID, PlayerPermissionSet> entry : permissionGroup.getPlayerPermissions().entrySet()) {
                            UUID uuid = entry.getKey();

                            if (PermissionRouter.getLayeredPermission(claim,
                                    null, uuid, PermissionRoute.MODIFY_CLAIM) == PermState.ENABLED ||
                                    PermissionRouter.getLayeredPermission(claim,
                                            null, uuid, PermissionRoute.MODIFY_PERMISSIONS) == PermState.ENABLED) {
                                ownedClaims.computeIfAbsent(uuid, u -> new ArrayList<>());
                                ArrayList<Integer> ids = ownedClaims.get(uuid);
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
                                        ownedSubClaims.computeIfAbsent(uuid, u -> new ArrayList<>());
                                        ArrayList<Integer> ids = ownedSubClaims.get(uuid);
                                        ids.add(subClaim.getId());
                                    }
                                }
                            }
                        }

                        logger.info("Loaded claims into admin and owner map for claim id: " + claim.getId());

                        if (!materialLookup.containsKey(claim.getWorld())){
                            materialLookup.put(claim.getWorld(), Material.OAK_FENCE);
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

    public ClaimResponse createClaim(Location upperCorner, Location lowerCorner, UUID owner){
        if (upperCorner == null || lowerCorner == null || upperCorner.getWorld() == null){
            return new ClaimResponse(false, ErrorType.CLAIM_LOCATIONS_WERE_NULL);
        }

        if (isTooSmall(upperCorner.getBlockX(), upperCorner.getBlockZ(), lowerCorner.getBlockX(), lowerCorner.getBlockZ())){
            return new ClaimResponse(false, ErrorType.TOO_SMALL);
        }

        Claim claim = new Claim(requestUniqueID(),
                upperCorner.getBlockX(),
                upperCorner.getBlockZ(),
                lowerCorner.getBlockX(),
                lowerCorner.getBlockZ(),
                upperCorner.getWorld().getUID(),
                new PermissionGroup(null, null, null),
                owner);

        claim.getPerms().setOwner(claim);

        return addClaim(claim) ? new ClaimResponse(true, claim) : new ClaimResponse(false, ErrorType.FILESYSTEM_OR_MEMORY_ERROR);
    }

    public ErrorType resizeClaim(Claim claim, int start_x, int start_z, int end_x, int end_z, Function<int[], ErrorType> verify){
        int[] arr = calculateResize(claim.getUpperCornerX(), claim.getLowerCornerX(),
                claim.getUpperCornerZ(), claim.getLowerCornerZ(), start_x, start_z, end_x, end_z);

        ErrorType val = verify.apply(arr);

        if (val != ErrorType.NONE)
            return val;

        int newUpperX = arr[0];
        int newUpperZ = arr[2];
        int newLowerX = arr[1];
        int newLowerZ = arr[3];

        for (SubClaim subClaim : claim.getSubClaims()){
            if (!MathUtils.containedInside(newUpperX, newUpperZ, newLowerX, newLowerZ,
                    subClaim.getUpperCornerX(), subClaim.getUpperCornerZ(), subClaim.getLowerCornerX(), subClaim.getLowerCornerZ())){
                return ErrorType.OVERLAP_EXISITNG;
            }
        }

        if (isTooSmall(newUpperX, newUpperZ, newLowerX, newLowerZ)){
            return ErrorType.TOO_SMALL;
        }

        if (arr[4] == 1) {
            removeChunksForClaim(claim);

            claim.setUpperCornerX(newUpperX);
            claim.setUpperCornerZ(newUpperZ);
            claim.setLowerCornerX(newLowerX);
            claim.setLowerCornerZ(newLowerZ);

            loadChunksForClaim(claim);

            return ErrorType.NONE;
        } else {
            return ErrorType.CANNOT_FLIP_ON_RESIZE;
        }
    }

    public ErrorType resizeSubClaim(SubClaim subClaim, int start_x, int start_z, int end_x, int end_z, Function<int[], ErrorType> verify){
        int[] arr = calculateResize(subClaim.getUpperCornerX(), subClaim.getLowerCornerX(),
                subClaim.getUpperCornerZ(), subClaim.getLowerCornerZ(), start_x, start_z, end_x, end_z);

        ErrorType val = verify.apply(arr);

        if (val != ErrorType.NONE)
            return val;

        int newUpperX = arr[0];
        int newUpperZ = arr[2];
        int newLowerX = arr[1];
        int newLowerZ = arr[3];

        if (isTooSmall(newUpperX, newUpperZ, newLowerX, newLowerZ)){
            return ErrorType.TOO_SMALL;
        }

        if (arr[4] == 1) {
            subClaim.setUpperCornerX(newUpperX);
            subClaim.setUpperCornerZ(newUpperZ);
            subClaim.setLowerCornerX(newLowerX);
            subClaim.setLowerCornerZ(newLowerZ);

            subClaim.setEditing(false);
            return ErrorType.NONE;
        } else {
            subClaim.setEditing(false);
            return ErrorType.CANNOT_FLIP_ON_RESIZE;
        }
    }

    private static int[] calculateResize(int NWCorner_x, int SECorner_x, int NWCorner_z, int SECorner_z, int Start_x, int Start_z, int End_x, int End_z) {
        /*
            Works for any size changes: corners or sides.
            Note:
            NWCorner_x defines the West Line
            SECorner_x defines the East Line

            NWCorner_z defines the North Line
            SECorner_z defines the South Line
         */

        int newNWCorner_x = NWCorner_x;
        int newSECorner_x = SECorner_x;
        int newNWCorner_z = NWCorner_z;
        int newSECorner_z = SECorner_z;

        int change = 0;

        if (Start_x == NWCorner_x) {
            /*Start is West*/
            newNWCorner_x = End_x;
        }

        if (Start_x == SECorner_x) {
            /*Start is East*/
            newSECorner_x = End_x;
        }

        if (Start_z == NWCorner_z) {
            /*Start is North*/
            newNWCorner_z = End_z;
        }

        if (Start_z == SECorner_z) {
            /*Start is South*/
            newSECorner_z = End_z;
        }

        if (newSECorner_x > NWCorner_x && newNWCorner_x < SECorner_x && newSECorner_z > NWCorner_z && newNWCorner_z < SECorner_z) {
            NWCorner_x = newNWCorner_x;
            SECorner_x = newSECorner_x;
            NWCorner_z = newNWCorner_z;
            SECorner_z = newSECorner_z;
            change = 1;
        }

        int[] arr = new int[5];

        arr[0] = NWCorner_x;
        arr[1] = SECorner_x;
        arr[2] = NWCorner_z;
        arr[3] = SECorner_z;
        arr[4] = change;

        return arr;
    }

    public boolean isTooSmall(int upperX, int upperZ, int lowerX, int lowerZ){
        return ((lowerX - upperX) < 4 || (lowerZ - upperZ) < 4);
    }

    public boolean checkOverLapSurroudningClaims(int claimid, int upperX, int upperZ, int lowerX, int lowerZ, UUID world){
        long NWChunkX = upperX >> 4;
        long NWChunkZ = upperZ >> 4;
        long SEChunkX = lowerX >> 4;
        long SEChunkZ = lowerZ >> 4;

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
            if (MathUtils.doOverlap(upperX, upperZ, lowerX, lowerZ,
                    claim.getUpperCornerX(), claim.getUpperCornerZ(), claim.getLowerCornerX(), claim.getLowerCornerZ())){
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
        //TODO Player Claim Data, is this needed?

        //Chunks
        Long2ObjectOpenHashMap<ArrayList<Integer>> chunks = chunkLookup.get(claim.getWorld());

        for (Map.Entry<Long, ArrayList<Integer>> entry : getChunksForClaim(claim).entrySet()){
            ArrayList<Integer> chunkMap = chunks.get(entry.getKey().longValue());
            chunkMap.removeAll(entry.getValue());   //Remove all of the existing claim chunk entries
        }
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

        long NWChunkX = claim.getUpperCornerX() >> 4;
        long NWChunkZ = claim.getUpperCornerZ() >> 4;
        long SEChunkX = claim.getLowerCornerX() >> 4;
        long SEChunkZ = claim.getLowerCornerZ() >> 4;

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
        if (!MathUtils.containedInside(claim.getUpperCornerX(), claim.getUpperCornerZ(), claim.getLowerCornerX(), claim.getLowerCornerZ(),
                loc1.getBlockX(), loc1.getBlockZ(), loc2.getBlockX(), loc2.getBlockZ())){
            return new ClaimResponse(false, ErrorType.OUT_OF_BOUNDS);
        }

        Location upper = StaticClaimLogic.calculateUpperCorner(loc1, loc2);
        Location lower = StaticClaimLogic.calculateLowerCorner(loc1, loc2);

        for (SubClaim subClaim : claim.getSubClaims()){
            if (MathUtils.doOverlap(subClaim.getUpperCornerX(), subClaim.getUpperCornerZ(), subClaim.getLowerCornerX(), subClaim.getLowerCornerZ(),
                    upper.getBlockX(), upper.getBlockZ(), lower.getBlockX(), lower.getBlockZ())){
                return new ClaimResponse(false, ErrorType.OVERLAP_EXISITNG);
            }
        }

        World world = loc1.getWorld();

        if (world == null) {
            return new ClaimResponse(false, ErrorType.GENERIC);
        }

        SubClaim subClaim = new SubClaim(claim,
                requestUniqueID(),
                upper.getBlockX(),
                upper.getBlockZ(),
                lower.getBlockX(),
                lower.getBlockZ(),
                loc1.getWorld().getUID(),
                new PermissionGroup(claim, null, null));

        PermissionGroup permissionGroup = subClaim.getPerms();

        permissionGroup.setPlayerPermissionSet(player.getUniqueId(), permissionSetup.getOwnerPermissionSet().clone());

        claim.addSubClaim(subClaim);

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

    public Claim getClaim(int x, int z, UUID world){
        ArrayList<Integer> integers = chunkLookup.get(world).get(getChunkHashFromLocation(x, z));

        if (integers == null)
            return null;

        for (Integer id : integers){
            Claim claim = getClaim(id);

            if (x >= claim.getUpperCornerX() && x <= claim.getLowerCornerX()
                    && z >= claim.getUpperCornerZ() && z <= claim.getLowerCornerZ()){
                return claim;
            }
        }
        return null;
    }

    public void fixupOwnerPerms(Claim claim){
        PermissionGroup group = claim.getPerms();
        group.setPlayerPermissionSet(claim.getOwner(), permissionSetup.getOwnerPermissionSet().clone());
    }

    public void addOwnedClaim(UUID uuid, Claim claim){
        ownedClaims.computeIfAbsent(uuid, (u) -> new ArrayList<>());
        ArrayList<Integer> arrayList = ownedClaims.get(uuid);
        arrayList.add(claim.getId());
    }

    public void removeOwnedClaim(UUID uuid, Claim claim){
        if (ownedClaims.containsKey(uuid)){
            ownedClaims.get(uuid).remove(claim.getId());
        }
    }

    public void addOwnedSubClaim(UUID uuid, Claim claim){
        ownedSubClaims.computeIfAbsent(uuid, (u) -> new ArrayList<>());
        ArrayList<Integer> arrayList = ownedSubClaims.get(uuid);
        arrayList.add(claim.getId());
    }

    public void removeOwnedCSublaim(UUID uuid, Claim claim){
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

    public ArrayList<Integer> getOwnedClaims(UUID uuid) {
        return ownedClaims.get(uuid);
    }

    public ArrayList<Integer> getOwnedSubClaims(UUID uuid) {
        return ownedSubClaims.get(uuid);
    }

    public HashMap<UUID, ArrayList<Integer>> getOwnedClaims() {
        return ownedClaims;
    }

    public HashMap<UUID, ArrayList<Integer>> getOwnedSubClaims() {
        return ownedSubClaims;
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

    public HashMap<UUID, Material> getMaterialLookup() {
        return materialLookup;
    }

    public IntCache<Claim> getClaimCache(){
        return claimLookup;
    }
}
