package net.crashcraft.crashclaim.claimobjects.permission;

import org.bukkit.Material;

import java.util.HashMap;

public class PlayerPermissionSet extends PermissionSet implements Cloneable {
    private int modifyPermissions;
    private int modifyClaim;

    public PlayerPermissionSet() {

    }

    public PlayerPermissionSet(int build, int interactions, int entities, int explosions, int teleportation, int viewSubClaims, HashMap<Material, Integer> containers, int defaultContainerValue, int modifyPermissions, int modifyClaim, int dropPickupItems) {
        super(build, interactions, entities, teleportation, viewSubClaims, containers, defaultContainerValue, dropPickupItems);
        this.modifyPermissions = modifyPermissions;
        this.modifyClaim = modifyClaim;
    }

    public int getModifyPermissions() {
        return modifyPermissions;
    }

    public void setModifyPermissions(int modifyPermissions) {
        this.modifyPermissions = modifyPermissions;
    }

    public int getModifyClaim() {
        return modifyClaim;
    }

    public void setModifyClaim(int modifyClaim) {
        this.modifyClaim = modifyClaim;
    }

    public PlayerPermissionSet clone() {
        return (PlayerPermissionSet) super.clone();
    }
}
