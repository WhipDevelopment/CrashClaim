package net.crashcraft.whipclaim.claimobjects;

import javafx.scene.paint.Material;

import java.io.Serializable;
import java.util.HashMap;

public class PermissionSet implements Serializable {
    private int build;
    private int interactions;
    private int entities;
    private int explosions;
    private int teleportation;
    private int pistons;
    private int fluids;
    private int allowSlimes;
    private int admin;

    private HashMap<Material, Integer> containers;

    public PermissionSet(){

    }

    public PermissionSet(int build, int interactions, int entities, int explosions, int teleportation, int pistons, int fluids, int allowSlimes, int admin, HashMap<Material, Integer> containers) {
        this.build = build;
        this.interactions = interactions;
        this.entities = entities;
        this.explosions = explosions;
        this.teleportation = teleportation;
        this.pistons = pistons;
        this.fluids = fluids;
        this.allowSlimes = allowSlimes;
        this.admin = admin;
        this.containers = containers;
    }

    public int getBuild() {
        return build;
    }

    public int getInteractions() {
        return interactions;
    }

    public int getEntities() {
        return entities;
    }

    public int getExplosions() {
        return explosions;
    }

    public int getTeleportation() {
        return teleportation;
    }

    public int getPistons() {
        return pistons;
    }

    public int getFluids() {
        return fluids;
    }

    public int getAllowSlimes() {
        return allowSlimes;
    }

    public int getAdmin() {
        return admin;
    }

    public HashMap<Material, Integer> getContainers() {
        return containers;
    }
}
