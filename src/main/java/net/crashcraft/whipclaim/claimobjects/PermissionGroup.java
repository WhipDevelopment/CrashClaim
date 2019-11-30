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

    public PermissionGroup(){

    }

    public PermissionGroup(PermissionSet globalPermissionSet, HashMap<UUID, PermissionSet> playerPermissions) {
        this.globalPermissionSet = globalPermissionSet == null ?
                new PermissionSet(PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,
                        PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,
                        PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, new HashMap<>()) : globalPermissionSet;
        this.playerPermissions = playerPermissions == null ? new HashMap<>() : playerPermissions ;
    }

/*
    public int getActivePermission(UUID uuid, PermissionRoute route){
        return PermissionRouter.getLayeredPermission(globalPermissionSet, playerPermissions.get(uuid), route);
    }

 */

    public PermissionSet getPermissionSet() {
        return globalPermissionSet;
    }

    public PermissionSet getPlayerPermissionSet(UUID id) {
        return playerPermissions.get(id);
    }

    public void setPermissionSet(PermissionSet permissionSet) {
        this.globalPermissionSet = permissionSet;
    }

    public void setPlayerPermissionSet(UUID uuid, PermissionSet permissionSet) {
        playerPermissions.put(uuid, permissionSet);
    }

    public void setPlayerPermissions(HashMap<UUID, PermissionSet> playerPermissions) {
        this.playerPermissions = playerPermissions;
    }

    public HashMap<UUID, PermissionSet> getPlayerPermissions(){
        return playerPermissions;
    }
}
