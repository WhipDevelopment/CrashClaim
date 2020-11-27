package net.crashcraft.crashclaim.claimobjects;

import com.fasterxml.jackson.annotation.*;

import java.util.UUID;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class,
        property = "@object_id")
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,
        include=JsonTypeInfo.As.PROPERTY,
        property="name")
@JsonSubTypes({
        @JsonSubTypes.Type(value= Claim.class, name = "Claim"),
        @JsonSubTypes.Type(value= SubClaim.class, name = "SubClaim")
})
public abstract class BaseClaim {
    private int id;

    @JsonProperty("minX")
    private int minCornerX;
    @JsonProperty("minZ")
    private int minCornerZ;
    @JsonProperty("maxX")
    private int maxCornerX;
    @JsonProperty("maxZ")
    private int maxCornerZ;

    private UUID world;

    private PermissionGroup perms;

    private String name;
    private String entryMessage;
    private String exitMessage;

    @JsonIgnore
    private boolean isEditing = false;

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

    public void setToSave(boolean toSave){
        throw new RuntimeException("Not implemented");
    }

    public boolean isToSave(){
        throw new RuntimeException("Not implemented");
    }

    public int getId() {
        return id;
    }

    @JsonProperty("minX")
    public int getMinX() {
        return minCornerX;
    }

    @JsonProperty("minZ")
    public int getMinZ() {
        return minCornerZ;
    }

    @JsonProperty("maxX")
    public int getMaxX() {
        return maxCornerX;
    }

    @JsonProperty("maxZ")
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

    @JsonProperty("minX")
    public void setMinCornerX(int minCornerX) {
        this.minCornerX = minCornerX;
    }

    @JsonProperty("minZ")
    public void setMinCornerZ(int minCornerZ) {
        this.minCornerZ = minCornerZ;
    }

    @JsonProperty("maxX")
    public void setMaxCornerX(int maxCornerX) {
        this.maxCornerX = maxCornerX;
    }

    @JsonProperty("maxZ")
    public void setMaxCornerZ(int maxCornerZ) {
        this.maxCornerZ = maxCornerZ;
    }

    //JSON needs this

    public void setId(int id) {
        this.id = id;
    }

    public void setWorld(UUID world) {
        this.world = world;
    }

    public void setPerms(PermissionGroup perms) {
        this.perms = perms;
    }
}

