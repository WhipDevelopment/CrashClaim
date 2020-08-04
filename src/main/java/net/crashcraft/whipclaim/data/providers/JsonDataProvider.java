package net.crashcraft.whipclaim.data.providers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.PermState;
import net.crashcraft.whipclaim.claimobjects.PermissionGroup;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.child.SubPermissionGroup;
import net.crashcraft.whipclaim.claimobjects.permission.parent.ParentPermissionGroup;
import net.crashcraft.whipclaim.config.GlobalConfig;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import net.crashcraft.whipclaim.permissions.PermissionRouter;
import org.bukkit.Material;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class JsonDataProvider implements DataProvider{
    private File dataFolder;
    private Logger logger;
    private ObjectMapper mapper;

    @Override
    public void init(WhipClaim plugin, ClaimDataManager manager) {
        this.logger = plugin.getLogger();
        this.dataFolder = new File(plugin.getDataFolder(), "ClaimData");
        this.mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        mapper.registerSubtypes(Claim.class, SubClaim.class, SubPermissionGroup.class, ParentPermissionGroup.class,
                GlobalPermissionSet.class, PlayerPermissionSet.class);

        int idCounter = manager.getIdCounter();

        if (!dataFolder.exists()){
            if (dataFolder.mkdirs())
                logger.info("Created data directory.");
        } else {
            logger.info("Starting claim and chunk bulk data load.");

            HashMap<UUID, Set<Integer>> ownedClaims = manager.getAllOwnedClaims();
            HashMap<UUID, Set<Integer>> ownedSubClaims = manager.getAllOwnedSubClaims();

            File[] files = dataFolder.listFiles();
            if (files != null) {
                long start = System.currentTimeMillis();
                for (File file : files) {
                    try {
                        int temp = Integer.valueOf(file.getName().substring(0, file.getName().length() - (".json".length())));
                        if (temp > idCounter) {
                            idCounter = temp;
                        }

                        Claim claim = readClaim(new FileInputStream(file));
                        manager.loadChunksForClaim(claim);
                        PermissionGroup permissionGroup = claim.getPerms();
                        ArrayList<SubClaim> subClaims = claim.getSubClaims();

                        if (subClaims != null) {
                            for (SubClaim subClaim : claim.getSubClaims()) {
                                manager.getSubClaimLookupParent().put(subClaim.getId(), claim.getId());
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
                                    if (PermissionRouter.getLayeredPermission(subPerms.getGlobalPermissionSet(),
                                            subPerms.getPlayerPermissionSet(uuid), PermissionRoute.MODIFY_CLAIM) == PermState.ENABLED ||
                                            PermissionRouter.getLayeredPermission(subPerms.getGlobalPermissionSet(),
                                                    subPerms.getPlayerPermissionSet(uuid), PermissionRoute.MODIFY_PERMISSIONS) == PermState.ENABLED) {
                                        ownedSubClaims.computeIfAbsent(uuid, u -> new HashSet<>());
                                        Set<Integer> ids = ownedSubClaims.get(uuid);
                                        ids.add(subClaim.getId());
                                    }
                                }
                            }
                        }

                        //ValueConfig check to make sure no dinky plugins loaded worlds
                        if (!GlobalConfig.visual_menu_items.containsKey(claim.getWorld())){
                            GlobalConfig.visual_menu_items.put(claim.getWorld(), Material.OAK_FENCE);
                        }
                    } catch (NumberFormatException e){
                        logger.warning("Claim file[" + file.getName() + "] had an invalid filename, continuing however that claim will not be loaded.");
                    } catch (FileNotFoundException ex){
                        logger.warning("Claim was not found at file listed by directory");
                    } catch (IOException ex2) {
                        logger.warning("Claim failed to load into memory, skipping. file[ " + file.getName() + " ]");
                        System.out.println(ex2.getMessage());
                        ex2.printStackTrace();
                    }
                }

                logger.info("Finished data load in " + ((System.currentTimeMillis() - start) / 1000));
            }
        }

        manager.setIdCounter(idCounter);
    }

    @Override
    public boolean preInitialSave(Claim claim) {
        File file = new File(dataFolder, claim.getId()  + ".json");

        if (file.exists()){
            logger.warning("Claim file already exists for id: " + claim.getId() + ", aborting");
            return false;
        }

        return true;
    }

    @Override
    public void saveClaim(Claim claim) {
        File file = new File(dataFolder, claim.getId() + ".json");
        try {
            writeClaim(file, claim);

            claim.setToSave(false);
        } catch (IOException ex1){
            logger.warning("[WhipClaim[ Claim was attempting to save but could not complete action due to an IO error. File: " + file.toURI());
        }
    }

    @Override
    public void removeClaim(Claim claim) {
        File file = new File(dataFolder, claim.getId() + ".json");
        file.delete();
    }

    @Override
    public Claim loadClaim(Integer id) {
        try {
            File file = new File(dataFolder, id.toString() + ".json");
            if (file.exists()){
                FileInputStream stream = new FileInputStream(file);

                return readClaim(stream);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private Claim readClaim(InputStream stream) throws IOException {
        return mapper.readValue(stream, Claim.class);
    }

    private void writeClaim(File file, Claim toWrite) throws IOException {
        mapper.writer().writeValue(file, toWrite);
    }
}
