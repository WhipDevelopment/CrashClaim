package net.crashcraft.whipclaim.claimobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Claim extends BaseClaim implements Serializable {
    private transient boolean toSave;

    private ArrayList<SubClaim> subClaims;

    public Claim(int id, int upperCornerX, int upperCornerZ, int lowerCornerX, int lowerCornerZ, UUID world, PermissionGroup perms) {
        super(id, upperCornerX, upperCornerZ, lowerCornerX, lowerCornerZ, world, perms);
        this.toSave = false;
    }

    public synchronized boolean isToSave() {
        return toSave;
    }

    public synchronized void setToSave(boolean toSave) {
        this.toSave = toSave;
    }

    public void addSubClaim(SubClaim subClaim){

    }

    public void removeSubClaim(int id){

    }

    public void getLayered
}
