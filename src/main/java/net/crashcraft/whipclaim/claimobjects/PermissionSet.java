package net.crashcraft.whipclaim.claimobjects;

import org.bukkit.Material;

import java.io.Serializable;
import java.util.HashMap;

public class PermissionSet implements Serializable, Cloneable{
    private static final long serialVersionUID = 40L;

    private int build;
    private int interactions;
    private int entities;
    private int explosions;
    private int teleportation;

    private int pistons;
    private int fluids;

    private int modifyPermissions;
    private int viewSubClaims;
    private int modifyClaim;

    private HashMap<Material, Integer> containers;

    public PermissionSet(){

    }

    public PermissionSet(int build, int interactions, int entities, int explosions, int teleportation, int pistons, int fluids, int viewSubClaims, int modifyPermissions, int modifyClaim, HashMap<Material, Integer> containers) {
        this.build = build;
        this.interactions = interactions;
        this.entities = entities;
        this.explosions = explosions;
        this.teleportation = teleportation;
        this.pistons = pistons;
        this.fluids = fluids;
        this.viewSubClaims = viewSubClaims;
        this.modifyPermissions = modifyPermissions;
        this.modifyClaim = modifyClaim;
        this.containers = containers;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getBuild() {
        return build;
    }

    public void setBuild(int build) {
        this.build = build;
    }

    public int getInteractions() {
        return interactions;
    }

    public void setInteractions(int interactions) {
        this.interactions = interactions;
    }

    public int getEntities() {
        return entities;
    }

    public void setEntities(int entities) {
        this.entities = entities;
    }

    public int getExplosions() {
        return explosions;
    }

    public void setExplosions(int explosions) {
        this.explosions = explosions;
    }

    public int getTeleportation() {
        return teleportation;
    }

    public void setTeleportation(int teleportation) {
        this.teleportation = teleportation;
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

    public int getViewSubClaims() {
        return viewSubClaims;
    }

    public void setViewSubClaims(int viewSubClaims) {
        this.viewSubClaims = viewSubClaims;
    }

    public int getModifyPermissions() {
        return modifyPermissions;
    }

    public void setModifyPermissions(int modifyPermissions) {
        this.modifyPermissions = modifyPermissions;
    }

    public int getModifyClaim() {
        return modifyClaim;
    }

    public void setModifyClaim(int modifyClaim) {
        this.modifyClaim = modifyClaim;
    }

    public HashMap<Material, Integer> getContainers() {
        return containers;
    }

    public void setContainers(HashMap<Material, Integer> containers) {
        this.containers = containers;
    }

    public PermissionSet clone() {
        try {
            return (PermissionSet) super.clone();
        } catch (CloneNotSupportedException e){
            e.printStackTrace();
        }
        return null;
    }
}
