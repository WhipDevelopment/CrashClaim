package net.crashcraft.whipclaim.claimobjects.permission;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import org.bukkit.Material;

import java.io.Serializable;
import java.util.HashMap;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class,
        property = "@object_id")
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,
        include=JsonTypeInfo.As.PROPERTY,
        property="name")
@JsonSubTypes({
        @JsonSubTypes.Type(value= PlayerPermissionSet.class, name = "PlayerPermissionSet"),
        @JsonSubTypes.Type(value= GlobalPermissionSet.class, name = "GlobalPermissionSet")
})
public abstract class PermissionSet implements Cloneable{
    private int build;
    private int interactions;
    private int entities;
    private int explosions;
    private int teleportation;
    private int viewSubClaims;

    private HashMap<Material, Integer> containers;

    public PermissionSet(){

    }

    public PermissionSet(int build, int interactions, int entities, int explosions, int teleportation, int viewSubClaims, HashMap<Material, Integer> containers) {
        this.build = build;
        this.interactions = interactions;
        this.entities = entities;
        this.explosions = explosions;
        this.teleportation = teleportation;
        this.viewSubClaims = viewSubClaims;
        this.containers = containers;
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

    public int getViewSubClaims() {
        return viewSubClaims;
    }

    public void setViewSubClaims(int viewSubClaims) {
        this.viewSubClaims = viewSubClaims;
    }

    public void setContainer(Material material, int value){
        this.containers.put(material, value);
    }

    public HashMap<Material, Integer> getContainers() {
        return containers;
    }

    public PermissionSet clone() {
        try {
            return (PermissionSet) super.clone();
        } catch (CloneNotSupportedException e){
            e.printStackTrace();
        }
        return null;
    }

    //JSON needs this

    public void setContainers(HashMap<Material, Integer> containers) {
        this.containers = containers;
    }


}
