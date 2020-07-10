package net.crashcraft.whipclaim.visualize.api.claim;

import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.whipclaim.config.GlobalConfig;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import net.crashcraft.whipclaim.permissions.PermissionRouter;
import net.crashcraft.whipclaim.visualize.api.*;
import net.crashcraft.whipclaim.visualize.api.visuals.BaseGlowVisual;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class GlowClaimVisual extends BaseGlowVisual {
    public GlowClaimVisual(VisualColor color, VisualGroup parent, Player player, int y,  BaseClaim claim) {
        super(VisualType.CLAIM, color, parent, player, y, claim);
    }

    @Override
    public void remove() {
        removeAll();
    }

    @Override
    public void spawn() {
        World world = getPlayer().getWorld();

        int NWCordX = getClaim().getMinX();
        int NWCordZ = getClaim().getMinZ();
        int SECordX = getClaim().getMaxX();
        int SECordZ = getClaim().getMaxZ();

        spawnEntity(NWCordX, NWCordZ, calcY(NWCordX, NWCordZ, world));
        spawnEntity(NWCordX, SECordZ, calcY(NWCordX, SECordZ, world));

        spawnEntity(SECordX, SECordZ, calcY(SECordX, SECordZ, world));
        spawnEntity(SECordX, NWCordZ, calcY(SECordX, NWCordZ, world));

        for (Integer integer : VisualUtils.getLine(SECordX - NWCordX)){
            spawnEntity(NWCordX + integer, NWCordZ, calcY(NWCordX + integer, NWCordZ, world));
            spawnEntity(NWCordX + integer, SECordZ, calcY(NWCordX + integer, SECordZ, world));
        }

        for (Integer integer : VisualUtils.getLine(SECordZ - NWCordZ)){
            spawnEntity(NWCordX, NWCordZ + integer, calcY(NWCordX, NWCordZ + integer, world));
            spawnEntity(SECordX, NWCordZ + integer, calcY(SECordX, NWCordZ + integer, world));
        }

        colorEntities(getParent().getPlayer(), getColor(), getEntityUUIDs());
    }

    private int calcY(int x, int z, World world){
        if (GlobalConfig.visual_use_highest_block){
            return world.getHighestBlockYAt(x, z);
        }
        return getY();
    }

    @Override
    public VisualColor getColor() {
        if (getDefaultColor() == null){
            PermissionGroup group;
            PlayerPermissionSet set;
            boolean isEditing;

            if (getClaim() instanceof SubClaim){
                SubClaim subClaim = (SubClaim) getClaim();
                group = subClaim.getParent().getPerms();
                set = group.getPlayerPermissionSet(getParent().getPlayer().getUniqueId());
                isEditing = subClaim.isEditing();
            } else {
                group = getClaim().getPerms();
                set = group.getPlayerPermissionSet(getParent().getPlayer().getUniqueId());
                isEditing = getClaim().isEditing();
            }

            if (set == null) {
                return VisualColor.RED;
            } else if (PermissionRouter.getLayeredPermission(group.getGlobalPermissionSet(), set, PermissionRoute.MODIFY_CLAIM) == PermState.ENABLED){
                if (isEditing) {
                    return VisualColor.YELLOW;
                } else {
                    return VisualColor.GREEN;
                }
            } else {
                return  VisualColor.GOLD;
            }
        }
        return getDefaultColor();
    }
}
