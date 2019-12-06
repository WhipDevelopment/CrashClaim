package net.crashcraft.whipclaim.claimobjects;

import net.crashcraft.whipclaim.data.MathUtils;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import net.crashcraft.whipclaim.permissions.PermissionRouter;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Claim extends BaseClaim implements Serializable {
    private static final long serialVersionUID = 20L;

    private transient boolean toSave;

    private ArrayList<SubClaim> subClaims;
    private UUID owner;

    public Claim(){

    }

    public Claim(int id, int upperCornerX, int upperCornerZ, int lowerCornerX, int lowerCornerZ, UUID world, PermissionGroup perms, UUID owner) {
        super(id, upperCornerX, upperCornerZ, lowerCornerX, lowerCornerZ, world, perms);
        this.toSave = false;
        this.subClaims = new ArrayList<>();
        this.owner = owner;
    }

    public int getActivePermission(UUID uuid, Location location, PermissionRoute route){   //should only be executed with a location inside the claim
        if (uuid.equals(owner))
            return PermState.ENABLED;

        if (location != null && subClaims.size() > 0){
            for (SubClaim subClaim : subClaims){
                if (MathUtils.checkPointCollide(getUpperCornerX(), getUpperCornerZ(), getLowerCornerX(),
                        getLowerCornerZ(), location.getBlockX(), location.getBlockZ())){

                    return PermissionRouter.getLayeredPermission(this, subClaim, uuid, route);
                }
            }
            return route.getPerm(getPerms().getPermissionSet());
        } else {
            return PermissionRouter.getLayeredPermission(this, null, uuid, route);
        }
    }

    public int getActivePermission(UUID uuid, Location location, PermissionRoute route, Material material){   //should only be executed with a location inside the claim
        if (uuid.equals(owner))
            return PermState.ENABLED;

        if (location != null && subClaims.size() > 0){
            for (SubClaim subClaim : subClaims){
                if (MathUtils.checkPointCollide(getUpperCornerX(), getUpperCornerZ(), getLowerCornerX(),
                        getLowerCornerZ(), location.getBlockX(), location.getBlockZ())){

                    return PermissionRouter.getLayeredContainer(this, subClaim, uuid, material);
                }
            }
            return route.getPerm(getPerms().getPermissionSet());
        } else {
            return PermissionRouter.getLayeredContainer(this, null, uuid, material);
        }
    }

    @Override
    public synchronized boolean isToSave() {
        return toSave;
    }

    @Override
    public synchronized void setToSave(boolean toSave) {
        this.toSave = toSave;
    }

    public void addSubClaim(SubClaim subClaim){
        subClaims.add(subClaim);
    }

    public void removeSubClaim(int id){
        for (SubClaim claim : subClaims){
            if (claim.getId() == id){
                subClaims.remove(claim);
                return;
            }
        }
    }

    public ArrayList<SubClaim> getSubClaims(){
        return subClaims;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}
