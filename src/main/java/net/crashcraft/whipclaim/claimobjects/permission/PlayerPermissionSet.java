package net.crashcraft.whipclaim.claimobjects.permission;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.bukkit.Material;

import java.util.HashMap;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class,
        property = "@object_id")
@JsonTypeName("PlayerPermissionSet")
public class PlayerPermissionSet extends PermissionSet implements Cloneable {
    private int modifyPermissions;
    private int modifyClaim;

    public PlayerPermissionSet() {

    }

    public PlayerPermissionSet(int build, int interactions, int entities, int explosions, int teleportation, int viewSubClaims, HashMap<Material, Integer> containers, int defaultContainerValue, int modifyPermissions, int modifyClaim) {
        super(build, interactions, entities, teleportation, viewSubClaims, containers, defaultContainerValue);
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

    //JSON needs this


}
