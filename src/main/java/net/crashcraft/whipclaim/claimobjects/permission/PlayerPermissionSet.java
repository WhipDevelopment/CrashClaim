package net.crashcraft.whipclaim.claimobjects.permission;

import org.bukkit.Material;

import java.io.Serializable;
import java.util.HashMap;

public class PlayerPermissionSet extends PermissionSet implements Cloneable, Serializable {
    private int modifyPermissions;
    private int modifyClaim;

    public PlayerPermissionSet() {

    }

    public PlayerPermissionSet(int build, int interactions, int entities, int explosions, int teleportation, int viewSubClaims, HashMap<Material, Integer> containers, int modifyPermissions, int modifyClaim) {
        super(build, interactions, entities, explosions, teleportation, viewSubClaims, containers);
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
