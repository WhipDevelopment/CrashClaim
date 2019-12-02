package net.crashcraft.whipclaim.claimobjects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class PermissionGroup implements Serializable {
    private static final long serialVersionUID = 30L;

    /**
     * Base Claim will have all perms
     * Sub Claim will have all perms except for admin as that gets inherited
     */

    private PermissionSet globalPermissionSet;
    private HashMap<UUID, PermissionSet> playerPermissions;

    private Claim parent;

    public PermissionGroup(){

    }

    public PermissionGroup(Claim parent, PermissionSet globalPermissionSet, HashMap<UUID, PermissionSet> playerPermissions) {
        this.globalPermissionSet = globalPermissionSet == null ?
                new PermissionSet(PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,
                        PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,
                        PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, new HashMap<>()) : globalPermissionSet;
        this.playerPermissions = playerPermissions == null ? new HashMap<>() : playerPermissions ;
    }

    public PermissionSet getPermissionSet() {
        return globalPermissionSet;
    }

    public PermissionSet getPlayerPermissionSet(UUID id) {
        return playerPermissions.get(id);
    }

    public void setPlayerPermissionSet(UUID uuid, PermissionSet permissionSet) {
        playerPermissions.put(uuid, permissionSet);
        parent.setToSave(true);
    }

    public HashMap<UUID, PermissionSet> getPlayerPermissions(){
        return playerPermissions;
    }

    public void setParrent(Claim claim){
        this.parent = claim;
    }

    //TODO add set permisison here using perms router
}
