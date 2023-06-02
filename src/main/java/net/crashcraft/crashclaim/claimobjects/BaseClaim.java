package net.crashcraft.crashclaim.claimobjects;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.localization.Localization;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.UUID;

public abstract class BaseClaim {
    private final int id;

    private int minCornerX;
    private int minCornerZ;
    private int maxCornerX;
    private int maxCornerZ;

    private final UUID world;

    private final PermissionGroup perms;

    private String name;
    private String entryMessage;
    private BaseComponent[] parsedEntryMessage; // Cached for efficiency
    private String exitMessage;
    private BaseComponent[] parsedExitMessage; // Cached for efficiency

    private boolean isEditing = false;
    private boolean deleted = false;

    public BaseClaim(int id, int maxCornerX, int maxCornerZ, int minCornerX, int minCornerZ, UUID world, PermissionGroup perms) {
        this.id = id;
        this.maxCornerX = maxCornerX;
        this.maxCornerZ = maxCornerZ;
        this.minCornerX = minCornerX;
        this.minCornerZ = minCornerZ;
        this.world = world;
        this.perms = perms;
    }

    public void setToSave(boolean toSave){
        throw new RuntimeException("Not implemented");
    }

    public boolean isToSave(){
        throw new RuntimeException("Not implemented");
    }

    public int getId() {
        return id;
    }

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
        return name == null ?
                Localization.NEW_CLAIM__DEFAULT_CLAIM_NAME.getRawMessage().replace("<id>", Integer.toString(id)) : name;
    }

    public String getEntryMessage() {
        return entryMessage;
    }

    public String getExitMessage() {
        return exitMessage;
    }

    public void setName(String name, boolean save) {
        this.name = name;
        if (save) setToSave(true);
    }

    public void setEntryMessage(String entryMessage, boolean save) {
        this.entryMessage = entryMessage;
        this.parsedEntryMessage = entryMessage == null ? null : Localization.parseRawUserInput(entryMessage);
        if (save) setToSave(true);
    }

    public void setExitMessage(String exitMessage, boolean save) {
        this.exitMessage = exitMessage;
        this.parsedExitMessage = exitMessage == null ? null :  Localization.parseRawUserInput(exitMessage);
        if (save) setToSave(true);
    }

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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted() {
        this.deleted = true;
    }

    public BaseComponent[] getParsedEntryMessage() {
        return parsedEntryMessage;
    }

    public BaseComponent[] getParsedExitMessage() {
        return parsedExitMessage;
    }
}

