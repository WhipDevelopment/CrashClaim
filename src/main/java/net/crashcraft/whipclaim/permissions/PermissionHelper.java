package net.crashcraft.whipclaim.permissions;

import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.PermState;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class PermissionHelper {
    private static PermissionHelper helper;

    private ClaimDataManager manager;
    private BypassManager bypassManager;

    public PermissionHelper(ClaimDataManager manager, BypassManager bypassManager){
        helper = this;

        this.manager = manager;
        this.bypassManager = bypassManager;
    }

    public Boolean hasPermission(BaseClaim claim, UUID player, PermissionRoute route){
        if (bypassManager.isBypass(player)){
            return true;
        }

        PlayerPermissionSet set = claim.getPerms().getPlayerPermissionSet(player);
        if (set == null){
            return false;
        } else {
            int value = route.getPerm(set);
            if (value == 4){
                return null;
            }
            return value == PermState.ENABLED;
        }
    }

    public Boolean hasPermission(BaseClaim claim, UUID player, Material material){
        if (bypassManager.isBypass(player)){
            return true;
        }

        PlayerPermissionSet set = claim.getPerms().getPlayerPermissionSet(player);
        return set != null && PermissionRoute.CONTAINERS.getPerm(set, material) == PermState.ENABLED;
    }

    public Boolean hasPermission(UUID player, Location location, Material material){
        if (bypassManager.isBypass(player)){
            return true;
        }

        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim == null){
            return true;
        } else {
            return claim.hasPermission(player, location, material);
        }
    }

    public Boolean hasPermission(UUID player, Location location, PermissionRoute route){
        if (bypassManager.isBypass(player)){
            return true;
        }

        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim == null){
            return true;
        } else {
            return claim.hasPermission(player, location, route);
        }
    }

    public Boolean hasPermission(Location location, Material material){
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim == null){
            return true;
        } else {
            return claim.hasPermission(location, material);
        }
    }

    public Boolean hasPermission(Location location, PermissionRoute route) {
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim == null) {
            return true;
        } else {
            return claim.hasPermission(location, route);
        }
    }

    public BypassManager getBypassManager() {
        return bypassManager;
    }

    public static PermissionHelper getPermissionHelper(){
        return helper;
    }
}
