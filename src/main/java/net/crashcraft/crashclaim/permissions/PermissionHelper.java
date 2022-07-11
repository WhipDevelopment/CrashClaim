package net.crashcraft.crashclaim.permissions;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.PermState;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class PermissionHelper {
    private static PermissionHelper helper;

    private final ClaimDataManager manager;
    private final BypassManager bypassManager;

    public PermissionHelper(ClaimDataManager manager){
        helper = this;

        this.manager = manager;
        this.bypassManager = new BypassManager();

        Bukkit.getPluginManager().registerEvents(bypassManager, CrashClaim.getPlugin());
    }

    public Boolean hasPermission(BaseClaim claim, UUID player, PermissionRoute route){
        if (bypassManager.isBypass(player)){
            return true;
        }

        // Override owner to grant permissions
        if (claim instanceof SubClaim subClaim && subClaim.getParent().getOwner().equals(player)){
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

        // Override owner to grant permissions
        if (claim instanceof SubClaim subClaim && subClaim.getParent().getOwner().equals(player)){
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
