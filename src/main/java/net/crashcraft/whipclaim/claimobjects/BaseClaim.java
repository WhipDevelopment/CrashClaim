package net.crashcraft.whipclaim.claimobjects;

import java.io.Serializable;
import java.util.UUID;

public class BaseClaim implements Serializable {
    private static final long serialVersionUID = 10L;

    private int id;

    private int upperCornerX;
    private int upperCornerZ;
    private int lowerCornerX;
    private int lowerCornerZ;

    private UUID world;

    private PermissionGroup perms;

    public BaseClaim(){

    }

    public BaseClaim(int id, int upperCornerX, int upperCornerZ, int lowerCornerX, int lowerCornerZ, UUID world, PermissionGroup perms) {
        this.id = id;
        this.upperCornerX = upperCornerX;
        this.upperCornerZ = upperCornerZ;
        this.lowerCornerX = lowerCornerX;
        this.lowerCornerZ = lowerCornerZ;
        this.world = world;
        this.perms = perms;
    }

    public int getId() {
        return id;
    }

    public int getUpperCornerX() {
        return upperCornerX;
    }

    public int getUpperCornerZ() {
        return upperCornerZ;
    }

    public int getLowerCornerX() {
        return lowerCornerX;
    }

    public int getLowerCornerZ() {
        return lowerCornerZ;
    }

    public UUID getWorld() {
        return world;
    }

    public PermissionGroup getPerms() {
        return perms;
    }
}

