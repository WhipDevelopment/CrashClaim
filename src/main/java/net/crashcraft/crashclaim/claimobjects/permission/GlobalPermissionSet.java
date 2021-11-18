package net.crashcraft.crashclaim.claimobjects.permission;

import org.bukkit.Material;

import java.util.HashMap;

public class GlobalPermissionSet extends PermissionSet {
    private int pistons;
    private int fluids;
    private int explosions;
    private int entityGrief;

    public GlobalPermissionSet() {

    }

    public GlobalPermissionSet(int build, int interactions, int entities, int explosions, int entityGrief, int teleportation, int viewSubClaims, HashMap<Material, Integer> containers, int defaultContainerValue, int pistons, int fluids) {
        super(build, interactions, entities, teleportation, viewSubClaims, containers, defaultContainerValue);
        this.entityGrief = entityGrief;
        this.pistons = pistons;
        this.explosions = explosions;
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

    public int getExplosions() {
        return explosions;
    }

    public void setExplosions(int explosions) {
        this.explosions = explosions;
    }

    public int getEntityGrief() {
        return entityGrief;
    }

    public void setEntityGrief(int entityGrief) {
        this.entityGrief = entityGrief;
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
    public void setTeleportation(int teleportation) {
        super.setTeleportation(teleportation);
    }

    @Override
    public void setViewSubClaims(int viewSubClaims) {
        super.setViewSubClaims(viewSubClaims);
    }
}
