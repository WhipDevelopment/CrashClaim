package net.crashcraft.whipclaim.claimobjects;

import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import org.bukkit.Material;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public abstract class PermissionGroup implements Serializable {
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
                createGlobalPermissionSet() : globalPermissionSet;
        this.playerPermissions = playerPermissions == null ? new HashMap<>() : playerPermissions;
    }

    public abstract PlayerPermissionSet createPlayerPermissionSet();

    public abstract GlobalPermissionSet createGlobalPermissionSet();

    public abstract int checkGlobalValue(int value, PermissionRoute route);

    public abstract int checkPlayerValue(int value, PermissionRoute route);

    public GlobalPermissionSet getPermissionSet() {
        return globalPermissionSet;
    }

    public PlayerPermissionSet getPlayerPermissionSet(UUID id) {
        if (playerPermissions.containsKey(id)) {
            return playerPermissions.get(id);
        } else {

            PlayerPermissionSet set = createPlayerPermissionSet();

            playerPermissions.put(id, set);

            return set;
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
        route.setPerm(globalPermissionSet, checkGlobalValue(value, route));
        owner.setToSave(true);
    }

    public void setPlayerPermission(UUID uuid, PermissionRoute route, int value){
        route.setPerm(getPlayerPermissionSet(uuid), checkPlayerValue(value, route));
        owner.setToSave(true);
        route.postSetPayload(this, route.getPerm(getPlayerPermissionSet(uuid)), uuid);
    }

    public void setContainerPermission(int value, Material material){
        PermissionRoute.CONTAINERS.setPerm(globalPermissionSet, checkGlobalValue(value, PermissionRoute.CONTAINERS), material);
        owner.setToSave(true);
    }

    public void setContainerPlayerPermission(UUID uuid, int value, Material material) {
        PermissionRoute.CONTAINERS.setPerm(getPlayerPermissionSet(uuid), checkPlayerValue(value, PermissionRoute.CONTAINERS), material);
        owner.setToSave(true);
    }
}
