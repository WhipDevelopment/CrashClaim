package net.crashcraft.whipclaim.visualize;

import javax.xml.stream.Location;
import java.util.HashMap;
import java.util.UUID;

public abstract class Visual {
    private HashMap<Integer, UUID> fakeEntities; // id - uuid
    private HashMap<Integer, Location> entityLocations;
    private VisualType type;

    public Visual(VisualType type) {
        this.type = type;
    }

    abstract void spawn();

    public void remove(){

    }

    public void despawnAfter(int seconds){

    }

    protected void spawnEntity(Location location, ){

    }

    public HashMap<Integer, UUID> getFakeEntities() {
        return fakeEntities;
    }

    public HashMap<Integer, Location> getEntityLocations() {
        return entityLocations;
    }

    public VisualType getType() {
        return type;
    }
}
