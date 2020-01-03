package net.crashcraft.whipclaim.permissions;

import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

public class PermissionHelper {
    private static PermissionHelper helper;

    private ClaimDataManager manager;

    public PermissionHelper(ClaimDataManager manager){
        helper = this;

        this.manager = manager;
    }

    public boolean hasPermission(UUID player, Location location, Material material){
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim == null){
            return true;
        } else {
            return claim.hasPermission(player, location, material);
        }
    }

    public boolean hasPermission(UUID player, Location location, PermissionRoute route){
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim == null){
            return true;
        } else {
            return claim.hasPermission(player, location, route);
        }
    }

    public boolean hasPermission(Location location, Material material){
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim == null){
            return true;
        } else {
            return claim.hasPermission(location, material);
        }
    }

    public boolean hasPermission(Location location, PermissionRoute route) {
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim == null) {
            return true;
        } else {
            return claim.hasPermission(location, route);
        }
    }

    public static PermissionHelper getPermissionHelper(){
        return helper;
    }
}
