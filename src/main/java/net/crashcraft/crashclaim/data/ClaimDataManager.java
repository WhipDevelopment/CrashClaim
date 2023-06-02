package net.crashcraft.crashclaim.data;

import dev.whip.crashutils.menusystem.defaultmenus.ConfirmationMenu;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.PermissionGroup;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.claimobjects.permission.child.SubPermissionGroup;
import net.crashcraft.crashclaim.claimobjects.permission.parent.ParentPermissionGroup;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.data.providers.DataProvider;
import net.crashcraft.crashclaim.data.providers.sqlite.SQLiteDataProvider;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.permissions.PermissionSetup;
import net.crashcraft.crashpayment.payment.TransactionResponse;
import net.crashcraft.crashpayment.payment.TransactionType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitTask;
import org.cache2k.Cache2kBuilder;
import org.cache2k.CacheEntry;
import org.cache2k.IntCache;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static net.crashcraft.crashclaim.data.StaticClaimLogic.getChunkHash;
import static net.crashcraft.crashclaim.data.StaticClaimLogic.getChunkHashFromLocation;

public class ClaimDataManager implements Listener {
    private final CrashClaim plugin;
    private final Logger logger;

    private final DataProvider provider;
    private final PermissionSetup permissionSetup;

    private final IntCache<Claim> claimLookup; // claim id - claim  - First to get called on loads
    private final HashMap<UUID, Long2ObjectOpenHashMap<ArrayList<Integer>>> chunkLookup; // Pre load with data from mem

    private final AtomicInteger idCounter;

    private BukkitTask forceSaveTask;
    private int forceSavePosition;

    public ClaimDataManager(CrashClaim plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.idCounter = new AtomicInteger(0);
        this.permissionSetup = new PermissionSetup(plugin);
        this.chunkLookup = new HashMap<>();

        for (World world : Bukkit.getWorlds()){
            chunkLookup.put(world.getUID(), new Long2ObjectOpenHashMap<>());
            logger.info("Loaded " + world.getName() + " into chunk map");
        }

        this.provider = new SQLiteDataProvider();
        provider.init(plugin, this);
        Bukkit.getPluginManager().registerEvents(provider, plugin);

        claimLookup = new Cache2kBuilder<Integer, Claim>() {}
                .name("chunkToClaimCache")
                .storeByReference(true)
                .loaderThreadCount(3)
                .disableStatistics(true)
                .loader((id) -> {
                    Claim claim = provider.loadClaim(id);
                    if (claim != null) {
                        fixupOwnerPerms(claim);
                        return claim;
                    } else {
                        return null;
                    }
                })
                .buildForIntKey();

        logger.info("Starting claim saving routine");
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (CacheEntry<Integer, Claim> entry : claimLookup.entries()){
                Claim claim = entry.getValue();

                if (claim == null || !claim.isToSave() || claim.isDeleted()){
                    continue;
                }

                saveClaim(claim);
            }
        }, 200L, 200L);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public int requestUniqueID(){
        return idCounter.getAndIncrement();
    }

    public ClaimResponse createClaim(Location maxCorner, Location minCorner, UUID owner){
        if (maxCorner == null || minCorner == null || maxCorner.getWorld() == null){
            return new ClaimResponse(false, ErrorType.CLAIM_LOCATIONS_WERE_NULL);
        }

        if (isTooSmall(maxCorner.getBlockX(), maxCorner.getBlockZ(), minCorner.getBlockX(), minCorner.getBlockZ())){
            return new ClaimResponse(false, ErrorType.TOO_SMALL);
        }

        if (checkOverLapSurroudningClaims(-1, maxCorner.getBlockX(), maxCorner.getBlockZ(), minCorner.getBlockX(), minCorner.getBlockZ(), maxCorner.getWorld().getUID())){
            return new ClaimResponse(false, ErrorType.OVERLAP_EXISTING);
        }

        if (!plugin.getPluginSupport().canClaim(minCorner, maxCorner)){
            return new ClaimResponse(false, ErrorType.OVERLAP_EXISTING_OTHER);
        }

        Claim claim = new Claim(requestUniqueID(),
                maxCorner.getBlockX(),
                maxCorner.getBlockZ(),
                minCorner.getBlockX(),
                minCorner.getBlockZ(),
                maxCorner.getWorld().getUID(),
                new ParentPermissionGroup(null, null, null),
                owner);

        claim.getPerms().setOwner(claim, true);

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

        if (checkOverLapSurroudningClaims(claim.getId(), newMaxX, newMaxZ, newMinX, newMinZ, claim.getWorld())){
            return ErrorType.OVERLAP_EXISTING;
        }

        for (SubClaim subClaim : claim.getSubClaims()){
            if (!MathUtils.containedInside(newMinX, newMinZ, newMaxX, newMaxZ,
                    subClaim.getMinX(), subClaim.getMinZ(), subClaim.getMaxX(), subClaim.getMaxZ())){
                return ErrorType.OVERLAP_EXISTING_SUBCLAIM;
            }
        }

        if (isTooSmall(newMaxX, newMaxZ, newMinX, newMinZ)){
            return ErrorType.TOO_SMALL;
        }

        if (arr[4] == 1) {
            if (!CrashClaim.getPlugin().getPluginSupport().canClaim(
                    new Location(Bukkit.getWorld(claim.getWorld()), newMinX, 0, newMinZ),
                    new Location(Bukkit.getWorld(claim.getWorld()), newMaxX, 0, newMaxZ))){ // TODO ugh create corners higher up and pass down, dirty fix for now
                return ErrorType.OVERLAP_EXISTING_OTHER;
            }

            int area = ContributionManager.getArea(newMinX, newMinZ, newMaxX, newMaxZ);
            int originalArea = ContributionManager.getArea(claim.getMinX(), claim.getMinZ(), claim.getMaxX(), claim.getMaxZ());

            int difference = area - originalArea;

            if (difference > 0) {
                int price = (int) Math.ceil(difference * GlobalConfig.money_per_block);
                String priceString = Integer.toString(price);
                //Check price with player
                new ConfirmationMenu(resizer,
                        Localization.RESIZE__MENU__CONFIRMATION__TITLE.getMessage(resizer),
                        Localization.RESIZE__MENU__CONFIRMATION__MESSAGE.getItem(resizer,
                                "price", priceString),
                        Localization.RESIZE__MENU__CONFIRMATION__ACCEPT.getItem(resizer,
                                "price", priceString),
                        Localization.RESIZE__MENU__CONFIRMATION__DENY.getItem(resizer,
                                "price", priceString),
                        (player, aBoolean) -> {
                            if (aBoolean) {
                                if (PermissionHelper.getPermissionHelper().hasPermission(claim, player.getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                                    CrashClaim.getPlugin().getPayment().makeTransaction(resizer.getUniqueId(), TransactionType.WITHDRAW, "Claim Resize Up", price, (response) -> {
                                        if (response.getTransactionStatus() == TransactionResponse.SUCCESS) {
                                            ContributionManager.addContribution(claim, newMinX, newMinZ, newMaxX, newMaxZ, resizer.getUniqueId());  // Contribution tracking
                                            resizeClaimCall(claim, newMinX, newMinZ, newMaxX, newMaxZ);

                                            consumer.accept(true);
                                            return;
                                        } else {
                                            //Didnt have enough money or something
                                            player.spigot().sendMessage(Localization.RESIZE__TRANSACTION_ERROR.getMessage(player,
                                                    "error", response.getTransactionError()));
                                        }
                                        consumer.accept(false);
                                    });
                                } else {
                                    player.spigot().sendMessage(Localization.RESIZE__NO_LONGER_PERMISSION.getMessage(player));
                                }
                            }
                            return "";
                        },
                        player -> ""
                        ).open();
            } else {
                //Need to issue a refund
                ContributionManager.addContribution(claim, newMinX, newMinZ, newMaxX, newMaxZ, resizer.getUniqueId());  // Contribution tracking
                resizeClaimCall(claim, newMinX, newMinZ, newMaxX, newMaxZ);
                consumer.accept(true);
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

            claim.setToSave(true);
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

        for (SubClaim tempClaim : subClaim.getParent().getSubClaims()){
            if (tempClaim.equals(subClaim)){
                continue;
            }

            if (MathUtils.doOverlap(tempClaim.getMinX(), tempClaim.getMinZ(), tempClaim.getMaxX(), tempClaim.getMaxZ(),
                    newMinX, newMinZ, newMaxX, newMaxZ)){
                return ErrorType.OVERLAP_EXISTING_SUBCLAIM;
            }
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
        provider.preInitialSave(claim);

        claimLookup.put(claim.getId(), claim);
        loadChunksForClaim(claim);

        fixupOwnerPerms(claim);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveClaim(claim));
        return true;
    }

    public synchronized void deleteClaim(Claim claim){
        if (claim.isDeleted()){
            return;
        }

        try {
            claim.setDeleted(); // Make sure it doesn't get saved again

            claimLookup.remove(Integer.valueOf(claim.getId()));
            provider.removeClaim(claim);

            //Chunks
            Long2ObjectOpenHashMap<ArrayList<Integer>> chunks = chunkLookup.get(claim.getWorld());

            for (Map.Entry<Long, ArrayList<Integer>> entry : getChunksForClaim(claim).entrySet()) {
                ArrayList<Integer> chunkMap = chunks.get(entry.getKey().longValue());
                chunkMap.removeAll(entry.getValue());   //Remove all of the existing claim chunk entries
            }

            if (claim.getSubClaims() != null) {
                new ArrayList<>(claim.getSubClaims()).iterator().forEachRemaining(this::deleteSubClaimWithoutSave);
            }

            //Refund
            ContributionManager.refundContributors(claim);
            // Continue and restart saving.
        } catch (Exception ex){
            ex.printStackTrace();
            logger.warning("An exception occurred while a claim was being deleted, restarting saving process.");
        }
    }

    public synchronized void deleteSubClaimWithoutSave(SubClaim subClaim){
        Claim parent = subClaim.getParent();
        parent.removeSubClaim(subClaim.getId());
    }

    public synchronized void deleteSubClaim(SubClaim subClaim){
        Claim parent = subClaim.getParent();
        parent.removeSubClaim(subClaim.getId());
        saveClaim(parent);
    }

    public void loadChunksForClaim(Claim claim){
        Long2ObjectOpenHashMap<ArrayList<Integer>> map = chunkLookup.get(claim.getWorld());
        for (Map.Entry<Long, ArrayList<Integer>> entry : getChunksForClaim(claim).entrySet()){
            map.putIfAbsent(entry.getKey().longValue(), new ArrayList<>());
            ArrayList<Integer> integers = map.get(entry.getKey().longValue());
            integers.add(claim.getId());
        }
    }

    public void loadChunksForUnLoadedClaim(int claim_id, int minX, int minZ, int maxX, int maxZ, UUID world){
        Long2ObjectOpenHashMap<ArrayList<Integer>> map = chunkLookup.get(world);

        if (map == null){
            map = new Long2ObjectOpenHashMap<>();
            chunkLookup.put(world, map);
        }

        for (Map.Entry<Long, ArrayList<Integer>> entry : getChunksForUnLoadedClaim(minX, minZ, maxX, maxZ, claim_id).entrySet()){
            map.putIfAbsent(entry.getKey().longValue(), new ArrayList<>());
            ArrayList<Integer> integers = map.get(entry.getKey().longValue());
            integers.add(claim_id);
        }
    }

    private void removeChunksForClaim(Claim claim){
        Long2ObjectOpenHashMap<ArrayList<Integer>> map = chunkLookup.get(claim.getWorld());
        for (Map.Entry<Long, ArrayList<Integer>> entry : getChunksForClaim(claim).entrySet()){
            map.putIfAbsent(entry.getKey().longValue(), new ArrayList<>());
            ArrayList<Integer> integers = map.get(entry.getKey().longValue());
            integers.remove(Integer.valueOf(claim.getId()));
        }
    }

    private HashMap<Long, ArrayList<Integer>> getChunksForClaim(Claim claim){
        return getChunksForUnLoadedClaim(claim.getMinX(), claim.getMinZ(), claim.getMaxX(), claim.getMaxZ(), claim.getId());
    }

    private HashMap<Long, ArrayList<Integer>> getChunksForUnLoadedClaim(int minX, int minZ, int maxX, int maxZ, int claim_id){
        HashMap<Long, ArrayList<Integer>> chunks = new HashMap<>();

        long NWChunkX = minX >> 4;
        long NWChunkZ = minZ >> 4;
        long SEChunkX = maxX >> 4;
        long SEChunkZ = maxZ >> 4;

        for (long zs = NWChunkZ; zs <= SEChunkZ; zs++) {
            for (long xs = NWChunkX; xs <= SEChunkX; xs++) {
                long identifier = getChunkHash(xs, zs);

                chunks.putIfAbsent(identifier, new ArrayList<>());

                chunks.get(identifier).add(claim_id);
            }
        }

        return chunks;
    }

    public ClaimResponse createSubClaim(Claim claim, Location loc1, Location loc2, UUID owner){
        if (claim.isDeleted()){
            return new ClaimResponse(false, ErrorType.GENERIC);
        }

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
                    min.getBlockX(), min.getBlockZ(), max.getBlockX(), max.getBlockZ())){
                return new ClaimResponse(false, ErrorType.OVERLAP_EXISTING_SUBCLAIM);
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

        permissionGroup.setOwner(subClaim, true);

        permissionGroup.setPlayerPermissionSet(owner, permissionSetup.getOwnerPermissionSet().clone(), false);

        claim.addSubClaim(subClaim);
        claim.setToSave(true);
        CrashClaim.getPlugin().getLogger().info("setToSave " + claim.getId() + " (createSubClaim)");

        return new ClaimResponse(true, subClaim);
    }

    public Claim getClaim(Integer id){
        return claimLookup.get(id);
    }

    public void preLoadChunk(UUID world, long seed){
        Long2ObjectOpenHashMap<ArrayList<Integer>> map = chunkLookup.get(world);
        if (map == null){
            Long2ObjectOpenHashMap<ArrayList<Integer>> newMap = new Long2ObjectOpenHashMap<>();
            chunkLookup.put(world, newMap);
            map = newMap;
        }

        ArrayList<Integer> claims = map.get(seed);
        if (claims != null){
            claimLookup.prefetchAll(claims, null);
        }
    }

    public synchronized void saveClaim(Claim claim){
        if (claim.isDeleted()){
            return;
        }

        provider.saveClaim(claim);
        claim.setToSave(false);
        //CrashClaim.getPlugin().getLogger().info("setToSave " + claim.getId() + " (saveClaimSyncronizedBlah)");
    }

    public void saveClaimsSync(){   //Force save all data - shutdown
        Collection<Claim> claims = claimLookup.asMap().values();

        for (Claim claim : claims){
            if (claim.isToSave()) saveClaim(claim);
        }
    }

    public CompletableFuture<Void> forceSaveClaims(){
        forceSavePosition = 0;
        ArrayList<Claim> claims = new ArrayList<>(claimLookup.asMap().values());
        CompletableFuture<Void> finishedFuture = new CompletableFuture<>();

        forceSaveTask = Bukkit.getScheduler().runTaskTimer(CrashClaim.getPlugin(), () -> {
            int nextPos = Math.min(forceSavePosition + 5, claims.size());
            for (int x = forceSavePosition; x < nextPos; x++){
                //logger.info("Saving " + x + " : " + claims.get(x).getId());
                saveClaim(claims.get(x));
            }

            forceSavePosition = nextPos;

            if (nextPos >= claims.size()){
                finishedFuture.complete(null);
                forceSaveTask.cancel();
            }
        }, 20L, 1L);

        return finishedFuture;
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

        if (integers == null) {
            return null;
        }

        for (Integer id : integers){
            Claim claim = getClaim(id);

            if (x >= claim.getMinX() && x <= claim.getMaxX()
                    && z >= claim.getMinZ() && z <= claim.getMaxZ()){
                return claim;
            }
        }
        return null;
    }

    public ArrayList<Claim> getClaims(long chunkX, long chunkZ, UUID world){
        ArrayList<Integer> integers = chunkLookup.get(world).get(getChunkHash(chunkX, chunkZ));

        if (integers == null) {
            return null;
        }

        ArrayList<Claim> claims = new ArrayList<>(integers.size());

        for (Integer id : integers){
            claims.add(getClaim(id));
        }

        return claims;
    }

    public Claim getClaim(Location location) {
        if (location.getWorld() == null)
            return null;

        return getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
    }

    public void fixupOwnerPerms(Claim claim){
        PermissionGroup group = claim.getPerms();
        group.setPlayerPermissionSet(claim.getOwner(), permissionSetup.getOwnerPermissionSet().clone(), false);
    }

    public ArrayList<Claim> getOwnedClaims(UUID uuid) {
        ArrayList<Claim> claims = new ArrayList<>();

        for (Integer id : provider.getPermittedClaims(uuid)){
            Claim claim = getClaim(id);
            if (Bukkit.getWorld(claim.getWorld()) != null){ // Make sure world of claim is loaded before we send it back.
                claims.add(claim);
            }
        }

        return claims;
    }

    public int getNumberOwnedClaims(UUID uuid) {
        return provider.getPermittedClaims(uuid).size();
    }

    public ArrayList<Claim> getOwnedParentClaims(UUID uuid) {
        ArrayList<Claim> claims = new ArrayList<>();

        for (Integer id : provider.getOwnedParentClaims(uuid)){
            Claim claim = getClaim(id);
            if (Bukkit.getWorld(claim.getWorld()) != null){ // Make sure world of claim is loaded before we send it back.
                claims.add(claim);
            }
        }

        return claims;
    }

    public int getNumberOwnedParentClaims(UUID uuid) {
        return provider.getOwnedParentClaims(uuid).size();
    }

    public void setIdCounter(int idCounter) {
        this.idCounter.set(idCounter);
    }

    public PermissionSetup getPermissionSetup() {
        return permissionSetup;
    }

    public Long2ObjectOpenHashMap<ArrayList<Integer>> getClaimChunkMap(UUID world){
        return chunkLookup.get(world);
    }

    public void cleanupAndClose() {
        chunkLookup.clear();
        claimLookup.clearAndClose();
    }

    public IntCache<Claim> getClaimCache() {
        return claimLookup;
    }
}
