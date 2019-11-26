package net.crashcraft.whipclaim.visualize;

import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;

import java.util.ArrayList;

public class ClaimVisual extends Visual{
    private BaseClaim claim;
    private int y;

    public ClaimVisual(BaseClaim claim, int y) {
        super(VisualType.CLAIM);
        this.claim = claim;
        this.y = y;
    }

    @Override
    public void spawn() {
        int NWCordX = claim.getUpperCornerX();
        int NWCordZ = claim.getUpperCornerZ();
        int SECordX = claim.getLowerCornerX();
        int SECordZ = claim.getLowerCornerZ();

        spawnEntity(NWCordX, NWCordZ, y);
        spawnEntity(NWCordX, SECordZ, y);

        spawnEntity(SECordX, SECordZ, y);
        spawnEntity(SECordX, NWCordZ, y);

        for (Integer integer : getLine(SECordX - NWCordX)){
            spawnEntity(NWCordX + integer, NWCordZ, y);
            spawnEntity(NWCordX + integer, SECordZ, y);
        }

        for (Integer integer : getLine(SECordZ - NWCordZ)){
            spawnEntity(NWCordX, NWCordZ + integer, y);
            spawnEntity(SECordX, NWCordZ + integer, y);
        }
    }

    @Override
    public void color(TeamColor color) {
        getParent().getManager().colorEntities(getParent().getPlayer(), color, getEntityUUIDs());
    }

/*
    public TeamColor getColor() {
        ClaimPermsObject permsObject = claim.getRAWClaimPermsForPlayer(UserCache.getUser(getOwner()).getUserID());

        if (permsObject == null)
            return TeamColor.RED;

        if (permsObject.isCoOwner()){
            if (claim.isResizing())
                return TeamColor.YELLOW;

            return TeamColor.GREEN;
        } else {
            return TeamColor.GOLD;
        }
    }

    public TeamColor getColor() {
        ClaimPermsObject permsObject = claim.getRAWClaimPermsForPlayer(UserCache.getUser(getOwner()).getUserID());

        if (permsObject == null)
            return TeamColor.RED;

        if (permsObject.isCoOwner()){
            if (claim.isResizing())
                return TeamColor.YELLOW;

            return TeamColor.GREEN;
        } else {
            return TeamColor.GOLD;
        }
    }

 */

    public BaseClaim getClaim() {
        return claim;
    }

    public int getY() {
        return y;
    }

    private static ArrayList<Integer> getLine(int Length){
        ArrayList<Integer> LineDots = new ArrayList<Integer>(){};
        LineDots.add(1);
        LineDots.add(Length-1);
        int Parts = (int) Math.ceil((float) Length/18);
        int PartLength = (int) Math.ceil((float) Length/Parts);
        for (int i=PartLength; i<Length ; i+=PartLength){
            LineDots.add(i-2);
            LineDots.add(i-1);
            LineDots.add(i);
            if(((Length % 2 == 0) && (Parts % 2 == 0)) || ((Length % 2 != 0) && (Parts % 2 != 0))){
                LineDots.add(i+1);
            }
        }
        return LineDots;
    }
}
