package net.crashcraft.whipclaim.claimobjects;

import java.io.Serializable;
import java.util.HashMap;

public class PermissionGroup implements Serializable {
    /**
     * Base Claim will have all perms
     * Sub Claim will have all perms except for admin as that gets inherited
     */

    private PermissionSet globalPermissionSet;
    private HashMap<Integer, PermissionSet> playerPermissions;

    public PermissionGroup(){

    }

    public PermissionGroup(PermissionSet globalPermissionSet, HashMap<Integer, PermissionSet> playerPermissions) {
        this.globalPermissionSet = globalPermissionSet == null ?
                new PermissionSet(PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,
                        PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,
                        PermState.DISABLE, PermState.DISABLE, new HashMap<>()) : globalPermissionSet;
        this.playerPermissions = playerPermissions == null ? new HashMap<>() : playerPermissions ;
    }

    public PermissionSet getPermissionSet() {
        return globalPermissionSet;
    }

    public PermissionSet getPlayerPermissionSet(int id) {
        return playerPermissions.get(id);
    }

    public void setPermissionSet(PermissionSet permissionSet) {
        this.globalPermissionSet = permissionSet;
    }

    public void setPlayerPermissions(HashMap<Integer, PermissionSet> playerPermissions) {
        this.playerPermissions = playerPermissions;
    }
}
