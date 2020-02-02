package net.crashcraft.whipclaim.claimobjects.permission;

import net.crashcraft.whipclaim.claimobjects.PermState;
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

    public int getPistons() {
        return pistons;
    }

    public void setPistons(int pistons) {
        this.pistons = pistons;
    }

    public int getFluids() {
        return fluids;
    }

    public void setFluids(int fluids) {
        this.fluids = fluids;
    }

    @Override
    public void setContainer(Material material, int value) {
        super.setContainer(material, value);
    }

    @Override
    public void setBuild(int build) {
        super.setBuild(build);
    }

    @Override
    public void setInteractions(int interactions) {
        super.setInteractions(interactions);
    }

    @Override
    public void setEntities(int entities) {
        super.setEntities(entities);
    }

    @Override
    public void setExplosions(int explosions) {
        super.setExplosions(explosions);
    }

    @Override
    public void setTeleportation(int teleportation) {
        super.setTeleportation(teleportation);
    }

    @Override
    public void setViewSubClaims(int viewSubClaims) {
        super.setViewSubClaims(viewSubClaims);
    }
}
