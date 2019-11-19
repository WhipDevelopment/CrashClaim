package net.crashcraft.whipclaim.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.claimobjects.PermissionGroup;
import net.crashcraft.whipclaim.claimobjects.PermissionSet;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.cache2k.Cache2kBuilder;
import org.cache2k.IntCache;
import org.cache2k.integration.CacheLoader;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import static net.crashcraft.whipclaim.data.StaticClaimLogic.getChunkHash;

public class ClaimDataManager implements Listener {
    private static FSTConfiguration serializeConf = FSTConfiguration.createDefaultConfiguration();

    private final WhipClaim plugin;
    private final Path dataPath;
    private final Logger logger;

    private HashMap<UUID, ArrayList<Integer>> playerData;

    private IntCache<Claim> claimLookup; // claim id - claim  - First to get called on loads
    private HashMap<UUID, Long2ObjectOpenHashMap<ArrayList<Integer>>> chunkLookup; // Pre load with data from mem

    private int idCounter;

    public ClaimDataManager(WhipClaim plugin){
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        serializeConf.registerClass(Claim.class, BaseClaim.class, PermissionGroup.class, PermissionSet.class);
        dataPath = Paths.get(plugin.getDataFolder().getAbsolutePath(), "ClaimData");

        chunkLookup = new HashMap<>();   // create faster map
        playerData = new HashMap<>();

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

                        logger.info(chunkLookup.toString());
                    } catch (NumberFormatException e){
                        logger.warning("Claim file[" + file.getName() + "] had an invalid filename, continuing however that claim will not be loaded.");
                    } catch (FileNotFoundException ex){
                        logger.warning("Claim was not found at file listed by directory");
                    } catch (IOException ex2) {
                        logger.warning("Claim failed to load into memory, skipping. file[ " + file.getName() + " ]");
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
                            return readClaim(stream);
                        }
                        return null;
                    })
                    .buildForIntKey();
        } catch (Exception e){
            e.printStackTrace();
            logger.info("Cache initialized with cache2k");
        }
    }

    public int requestUniqueID(){
        return idCounter+=1;
    }

    public ClaimResponse createClaim(Location upperCorner, Location lowerCorner){
        if (upperCorner == null || lowerCorner == null || upperCorner.getWorld() == null){
            return new ClaimResponse(false, "Claim locations were null.");
        }

        /*
        int x1 = upperCorner.getBlockX();
        int x2 = lowerCorner.getBlockX();
        int y1 = upperCorner.getBlockZ();
        int y2 = lowerCorner.getBlockZ();

                (x1 > x2) ? x2 : x1,
                (y1 > y2) ? y2 : y1,
                (x1 > x2) ? x1 : x2,
                (y1 > y2) ? y1 : y2,
         */

        Claim claim = new Claim(requestUniqueID(),
                upperCorner.getBlockX(),
                upperCorner.getBlockZ(),
                lowerCorner.getBlockX(),
                lowerCorner.getBlockZ(),
                upperCorner.getWorld().getUID(),
                new PermissionGroup(null, null));

        return addClaim(claim) ? new ClaimResponse(true, claim) : new ClaimResponse(false, "Error adding claim to memory and filesystem");
    }

    private boolean addClaim(Claim claim){ //Should not be called from anywhere else
        File file = new File(Paths.get(dataPath.toString(), Integer.toString(claim.getId())).toUri());

        if (file.exists()){
            logger.warning("Claim file already exists for id: " + claim.getId() + ", aborting");
            return false;
        }

        claimLookup.put(claim.getId(), claim);
        loadChunksForClaim(claim);

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
            ArrayList<Integer> chunkMap = chunks.get(entry.getKey());
            chunkMap.removeAll(entry.getValue());   //Remove all of the existing claim chunk entries
        }
    }

    private void loadChunksForClaim(Claim claim){
        chunkLookup.get(claim.getWorld()).putAll(getChunksForClaim(claim));
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

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Claim claim : claims){
                if (claim.isToSave()) {
                    saveClaim(claim);
                }
            }
        });
    }

    @EventHandler
    void onChunkLoad(ChunkLoadEvent e){
        Chunk chunk = e.getChunk();
        long seed = getChunkHash(chunk.getX(), chunk.getZ());
        preLoadChunk(e.getWorld().getUID(), seed);

        logger.info("Chunk loaded and Claims loaded for seed: " + seed);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    void onWorldLoad(WorldLoadEvent e){  // make sur eon new world loads there is a cache entry
        chunkLookup.putIfAbsent(e.getWorld().getUID(), new Long2ObjectOpenHashMap<>());
    }

    @EventHandler
    void onSave(WorldSaveEvent e){
        saveClaims();
    }

    public ConcurrentMap<Integer, Claim> temporaryTestGetClaimMap(){
        return claimLookup.asMap();
    }
}
