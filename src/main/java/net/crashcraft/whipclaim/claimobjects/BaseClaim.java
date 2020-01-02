package net.crashcraft.whipclaim.claimobjects;

import java.io.Serializable;
import java.util.UUID;

public abstract class BaseClaim implements Serializable {
    private static final long serialVersionUID = 10L;

    private int id;

    private int minCornerX;
    private int minCornerZ;
    private int maxCornerX;
    private int maxCornerZ;

    private UUID world;

    private PermissionGroup perms;

    private String name;
    private String entryMessage;
    private String exitMessage;

    private transient boolean isEditing = false;

    public BaseClaim(){

    }

    public BaseClaim(int id, int maxCornerX, int maxCornerZ, int minCornerX, int minCornerZ, UUID world, PermissionGroup perms) {
        this.id = id;
        this.maxCornerX = maxCornerX;
        this.maxCornerZ = maxCornerZ;
        this.minCornerX = minCornerX;
        this.minCornerZ = minCornerZ;
        this.world = world;
        this.perms = perms;
    }

    abstract void setToSave(boolean toSave);

    abstract boolean isToSave();

    public int getId() {
        return id;
    }

    /*
    public int getMinX() {
        return maxCornerX;
    }

    public int getMinZ() {
        return maxCornerZ;
    }

    public int getMaxX() {
        return minCornerX;
    }

    public int getMaxZ() {
        return minCornerZ;
    }
     */

    public int getMinX() {
        return minCornerX;
    }

    public int getMinZ() {
        return minCornerZ;
    }

    public int getMaxX() {
        return maxCornerX;
    }

    public int getMaxZ() {
        return maxCornerZ;
    }

    public UUID getWorld() {
        return world;
    }

    public PermissionGroup getPerms() {
        return perms;
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void setEditing(boolean editing) {
        isEditing = editing;
    }

    public String getName() {
        return name == null ? Integer.toString(id) : name;
    }

    public String getEntryMessage() {
        return entryMessage;
    }

    public String getExitMessage() {
        return exitMessage;
    }

    public void setName(String name) {
        this.name = name;
        setToSave(true);
    }

    public void setEntryMessage(String entryMessage) {
        this.entryMessage = entryMessage;
        setToSave(true);
    }

    public void setExitMessage(String exitMessage) {
        this.exitMessage = exitMessage;
        setToSave(true);
    }

    //Check these
    public void setMinCornerX(int minCornerX) {
        this.minCornerX = minCornerX;
    }

    public void setMinCornerZ(int minCornerZ) {
        this.minCornerZ = minCornerZ;
    }

    public void setMaxCornerX(int maxCornerX) {
        this.maxCornerX = maxCornerX;
    }

    public void setMaxCornerZ(int maxCornerZ) {
        this.maxCornerZ = maxCornerZ;
    }
}

