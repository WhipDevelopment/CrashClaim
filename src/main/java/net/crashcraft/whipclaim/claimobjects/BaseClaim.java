package net.crashcraft.whipclaim.claimobjects;

import java.io.Serializable;
import java.util.UUID;

public abstract class BaseClaim implements Serializable {
    private static final long serialVersionUID = 10L;

    private int id;

    private int upperCornerX;
    private int upperCornerZ;
    private int lowerCornerX;
    private int lowerCornerZ;

    private UUID world;

    private PermissionGroup perms;

    private String name;
    private String entryMessage;
    private String exitMessage;

    private transient boolean isEditing = false;

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

    abstract void setToSave(boolean toSave);

    abstract boolean isToSave();

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

    public void setUpperCornerX(int upperCornerX) {
        this.upperCornerX = upperCornerX;
    }

    public void setUpperCornerZ(int upperCornerZ) {
        this.upperCornerZ = upperCornerZ;
    }

    public void setLowerCornerX(int lowerCornerX) {
        this.lowerCornerX = lowerCornerX;
    }

    public void setLowerCornerZ(int lowerCornerZ) {
        this.lowerCornerZ = lowerCornerZ;
    }
}

