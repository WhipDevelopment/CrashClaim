package net.crashcraft.whipclaim.claimobjects;

import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import net.crashcraft.whipclaim.permissions.PermissionSetup;
import org.bukkit.Material;

import java.io.Serializable;
import java.security.Permission;
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

    private BaseClaim owner;

    public PermissionGroup(){

    }

    public PermissionGroup(BaseClaim owner, PermissionSet globalPermissionSet, HashMap<UUID, PermissionSet> playerPermissions) {
        this.owner = owner;
        this.globalPermissionSet = globalPermissionSet == null ?
                new PermissionSet(PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,
                        PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,
                        PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,  new HashMap<>()) : globalPermissionSet;
        this.playerPermissions = playerPermissions == null ? new HashMap<>() : playerPermissions ;
    }

    public PermissionSet getPermissionSet() {
        return globalPermissionSet;
    }

    public PermissionSet getPlayerPermissionSet(UUID id) {
        if (playerPermissions.containsKey(id)) {
            return playerPermissions.get(id);
        } else {
            PermissionSet perms = new PermissionSet(PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL,
                    PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL,  new HashMap<>());
            playerPermissions.put(id, perms);
            return perms;
        }
    }

    //Used for fixing owner permissions only
    public void setPlayerPermissionSet(UUID uuid, PermissionSet permissionSet) {
        playerPermissions.put(uuid, permissionSet);
        owner.setToSave(true);
    }

    public HashMap<UUID, PermissionSet> getPlayerPermissions(){
        return playerPermissions;
    }

    public void setOwner(BaseClaim owner){
        this.owner = owner;
    }

    public void setPermission(PermissionRoute route, int value){
        route.setPerm(globalPermissionSet, value);
        owner.setToSave(true);
    }

    public void setPlayerPermission(UUID uuid, PermissionRoute route, int value){
        route.setPerm(getPlayerPermissionSet(uuid), value);
        owner.setToSave(true);
    }

    public void setContainerPermission(int value, Material material){
        PermissionRoute.CONTAINERS.setPerm(globalPermissionSet, value, material);
        owner.setToSave(true);
    }

    public void setContainerPlayerPermission(UUID uuid, int value, Material material) {
        PermissionRoute.CONTAINERS.setPerm(getPlayerPermissionSet(uuid), value, material);
        owner.setToSave(true);
    }
}
