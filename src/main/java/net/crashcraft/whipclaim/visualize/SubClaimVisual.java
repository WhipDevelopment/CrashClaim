package net.crashcraft.whipclaim.visualize;

import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import net.crashcraft.whipclaim.permissions.PermissionRouter;

public class SubClaimVisual extends ClaimVisual{
    public SubClaimVisual(BaseClaim claim, int y) {
        super(claim, y);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public void color(TeamColor color) {
        SubClaim subClaim = (SubClaim) getClaim();
        BaseClaim parentClaim = subClaim.getParent();

        if (color == null){
            PermissionGroup group = parentClaim.getPerms();
            PlayerPermissionSet set = group.getPlayerPermissionSet(getParent().getPlayer().getUniqueId());

            if (set == null) {
                color = TeamColor.RED;
            } else if (PermissionRouter.getLayeredPermission(group.getPermissionSet(), set, PermissionRoute.MODIFY_CLAIM) == PermState.ENABLED){
                if (subClaim.isEditing()) {
                    color = TeamColor.YELLOW;
                } else {
                    color =  TeamColor.GREEN;
                }
            } else {
                color =  TeamColor.GOLD;
            }
        }

        getParent().getManager().colorEntities(getParent().getPlayer(), color, getEntityUUIDs());
    }
}
