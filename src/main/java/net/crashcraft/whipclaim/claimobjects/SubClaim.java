package net.crashcraft.whipclaim.claimobjects;

import java.io.Serializable;
import java.util.UUID;

public class SubClaim extends BaseClaim implements Serializable {
    private static final long serialVersionUID = 50L;

    private Claim parent;

    public SubClaim() {

    }

    public SubClaim(Claim parent, int id, int upperCornerX, int upperCornerY, int lowerCornerX, int lowerCornerY, UUID world, PermissionGroup perms) {
        super(id, upperCornerX, upperCornerY, lowerCornerX, lowerCornerY, world, perms);
        this.parent = parent;
    }

    @Override
    void setToSave(boolean toSave) {
        parent.setToSave(true);
    }

    @Override
    boolean isToSave() {
        return parent.isToSave();
    }

    public Claim getParent() {
        return parent;
    }
}
