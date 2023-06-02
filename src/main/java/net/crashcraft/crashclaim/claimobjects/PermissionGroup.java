package net.crashcraft.crashclaim.claimobjects;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.crashclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.UUID;

public abstract class PermissionGroup {
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

    public GlobalPermissionSet getGlobalPermissionSet() {
        return globalPermissionSet;
    }

    public PlayerPermissionSet getPlayerPermissionSet(UUID id) {
        return playerPermissions.computeIfAbsent(id, key -> createPlayerPermissionSet());
    }

    //Used for fixing owner permissions only
    public void setPlayerPermissionSet(UUID uuid, PlayerPermissionSet permissionSet, boolean save) {
        playerPermissions.put(uuid, permissionSet);
        if (save) {
            owner.setToSave(true);
            CrashClaim.getPlugin().getLogger().info("setToSave " + owner.getId() + " (setPlayerPermisssion set)");
        }
    }

    public HashMap<UUID, PlayerPermissionSet> getPlayerPermissions(){
        return playerPermissions;
    }

    public void setOwner(BaseClaim owner, boolean save){
        this.owner = owner;
        if (save) owner.setToSave(true);
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

    //JSON needs this

    public void setGlobalPermissionSet(GlobalPermissionSet globalPermissionSet) {
        this.globalPermissionSet = globalPermissionSet;
    }

    public void setPlayerPermissions(HashMap<UUID, PlayerPermissionSet> playerPermissions) {
        this.playerPermissions = playerPermissions;
    }
}
