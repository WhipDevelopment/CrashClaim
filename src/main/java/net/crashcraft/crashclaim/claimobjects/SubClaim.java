package net.crashcraft.crashclaim.claimobjects;

import java.util.UUID;

public class SubClaim extends BaseClaim {
    private final Claim parent;

    public SubClaim(Claim parent, int id, int upperCornerX, int upperCornerY, int lowerCornerX, int lowerCornerY, UUID world, PermissionGroup perms) {
        super(id, upperCornerX, upperCornerY, lowerCornerX, lowerCornerY, world, perms);
        this.parent = parent;
    }

    @Override
    public void setToSave(boolean toSave) {
        if (parent == null){
            // Needed for json setting this should never happen after load
            return;
        }
        parent.setToSave(true);
    }

    @Override
    public boolean isToSave() {
        return parent.isToSave();
    }

    @Override
    public UUID getWorld(){
        return parent.getWorld();
    }

    public Claim getParent() {
        return parent;
    }
}
