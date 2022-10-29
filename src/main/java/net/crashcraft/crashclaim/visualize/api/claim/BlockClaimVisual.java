package net.crashcraft.crashclaim.visualize.api.claim;

import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import net.crashcraft.crashclaim.claimobjects.PermState;
import net.crashcraft.crashclaim.claimobjects.PermissionGroup;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.permissions.PermissionRouter;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.VisualType;
import net.crashcraft.crashclaim.visualize.api.VisualUtils;
import net.crashcraft.crashclaim.visualize.api.visuals.BaseBlockVisual;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class BlockClaimVisual extends BaseBlockVisual {
    private final ArrayList<Location> changedBlocks;

    public BlockClaimVisual(VisualColor color, VisualGroup parent, Player player, int y, BaseClaim claim) {
        super(VisualType.CLAIM, color, parent, player, y, claim);
        this.changedBlocks = new ArrayList<>();
    }

    @Override
    public void remove(){
        for (Location location : changedBlocks){
            revertBlock(getPlayer(), location);
        }
    }

    private void sendBlock(int x, int z, int y, World world){
        Location location = new Location(world, x, y, z);
        changedBlocks.add(location);
        setBlock(getPlayer(), location, getColor().getMaterial());
    }

    @Override
    public void spawn() {
        World world = getPlayer().getWorld();

        int NWCordX = getClaim().getMinX();
        int NWCordZ = getClaim().getMinZ();
        int SECordX = getClaim().getMaxX();
        int SECordZ = getClaim().getMaxZ();

        sendBlock(NWCordX, NWCordZ, calcY(NWCordX, NWCordZ, world), world);
        sendBlock(NWCordX, SECordZ, calcY(NWCordX, SECordZ, world), world);

        sendBlock(SECordX, SECordZ, calcY(SECordX, SECordZ, world), world);
        sendBlock(SECordX, NWCordZ, calcY(SECordX, NWCordZ, world), world);

        for (Integer integer : VisualUtils.getLine(SECordX - NWCordX)){
            sendBlock(NWCordX + integer, NWCordZ, calcY(NWCordX + integer, NWCordZ, world), world);
            sendBlock(NWCordX + integer, SECordZ, calcY(NWCordX + integer, SECordZ, world), world);
        }

        for (Integer integer : VisualUtils.getLine(SECordZ - NWCordZ)){
            sendBlock(NWCordX, NWCordZ + integer, calcY(NWCordX, NWCordZ + integer, world), world);
            sendBlock(SECordX, NWCordZ + integer, calcY(SECordX, NWCordZ + integer, world), world);
        }
    }

    private int calcY(int x, int z, World world){
        if (GlobalConfig.visual_use_highest_block){
            return world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
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
