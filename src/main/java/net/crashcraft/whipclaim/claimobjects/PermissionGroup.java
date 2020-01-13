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

    private GlobalPermissionSet globalPermissionSet;
    private HashMap<UUID, PlayerPermissionSet> playerPermissions;

    private BaseClaim owner;

    public PermissionGroup(){

    }

    public PermissionGroup(BaseClaim owner, GlobalPermissionSet globalPermissionSet, HashMap<UUID, PlayerPermissionSet> playerPermissions) {
        this.owner = owner;
        this.globalPermissionSet = globalPermissionSet == null ?
                new GlobalPermissionSet(PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,
                        PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,  new HashMap<>(),
                        PermState.DISABLE, PermState.DISABLE) : globalPermissionSet;
        this.playerPermissions = playerPermissions == null ? new HashMap<>() : playerPermissions;
    }

    public PermissionGroup(BaseClaim owner, GlobalPermissionSet globalPermissionSet, HashMap<UUID, PlayerPermissionSet> playerPermissions, boolean skipGlobalchecks) {
        this.owner = owner;
        this.globalPermissionSet = globalPermissionSet == null ?
                new GlobalPermissionSet(PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,
                        PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,  new HashMap<>(),
                        PermState.DISABLE, PermState.DISABLE, skipGlobalchecks) : globalPermissionSet;
        this.playerPermissions = playerPermissions == null ? new HashMap<>() : playerPermissions;
    }

    public GlobalPermissionSet getPermissionSet() {
        return globalPermissionSet;
    }

    public PlayerPermissionSet getPlayerPermissionSet(UUID id) {
        if (playerPermissions.containsKey(id)) {
            return playerPermissions.get(id);
        } else {
            PlayerPermissionSet perms;
            if (owner instanceof SubClaim){
                perms = new PlayerPermissionSet(PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL,
                        PermState.NEUTRAL, new HashMap<>(), PermState.NEUTRAL, PermState.NEUTRAL);
            } else {
                perms = new PlayerPermissionSet(PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL,
                        PermState.DISABLE, new HashMap<>(), PermState.DISABLE, PermState.DISABLE);
            }
            playerPermissions.put(id, perms);
            return perms;
        }
    }

    //Used for fixing owner permissions only
    public void setPlayerPermissionSet(UUID uuid, PlayerPermissionSet permissionSet) {
        playerPermissions.put(uuid, permissionSet);
        owner.setToSave(true);
    }

    public HashMap<UUID, PlayerPermissionSet> getPlayerPermissions(){
        return playerPermissions;
    }

    public void setOwner(BaseClaim owner){
        this.owner = owner;
    }

    public BaseClaim getOwner() {
        return owner;
    }

    public void setPermission(PermissionRoute route, int value){
        route.setPerm(globalPermissionSet, value);
        owner.setToSave(true);
    }

    public void setPlayerPermission(UUID uuid, PermissionRoute route, int value){
        route.setPerm(getPlayerPermissionSet(uuid), value);
        owner.setToSave(true);
        route.postSetPayload(this, route.getPerm(getPlayerPermissionSet(uuid)), uuid);
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
