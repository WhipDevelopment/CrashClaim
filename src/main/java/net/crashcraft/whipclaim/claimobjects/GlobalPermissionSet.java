package net.crashcraft.whipclaim.claimobjects;

import org.bukkit.Material;

import java.io.Serializable;
import java.util.HashMap;

public class GlobalPermissionSet extends PermissionSet implements Serializable {
    private int pistons;
    private int fluids;

    public GlobalPermissionSet() {

    }

    public GlobalPermissionSet(int build, int interactions, int entities, int explosions, int teleportation, int viewSubClaims, HashMap<Material, Integer> containers, int pistons, int fluids) {
        super(build, interactions, entities, explosions, teleportation, viewSubClaims, containers);
        this.pistons = pistons;
        this.fluids = fluids;
    }

    private int checkValue(int value){
        return value == PermState.NEUTRAL ? PermState.DISABLE : value;
    }

    public int getPistons() {
        return pistons;
    }

    public void setPistons(int pistons) {
        this.pistons = checkValue(pistons);
    }

    public int getFluids() {
        return fluids;
    }

    public void setFluids(int fluids) {
        this.fluids = checkValue(fluids);
    }

    @Override
    public void setBuild(int build) {
        super.setBuild(checkValue(build));
    }

    @Override
    public void setInteractions(int interactions) {
        super.setInteractions(checkValue(interactions));
    }

    @Override
    public void setEntities(int entities) {
        super.setEntities(checkValue(entities));
    }

    @Override
    public void setExplosions(int explosions) {
        super.setExplosions(checkValue(explosions));
    }

    @Override
    public void setTeleportation(int teleportation) {
        super.setTeleportation(checkValue(teleportation));
    }

    @Override
    public void setViewSubClaims(int viewSubClaims) {
        super.setViewSubClaims(checkValue(viewSubClaims));
    }
}
