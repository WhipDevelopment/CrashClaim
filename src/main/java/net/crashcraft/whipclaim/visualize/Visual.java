package net.crashcraft.whipclaim.visualize;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Visual {
    private VisualType type;

    private HashMap<Integer, String> fakeEntities; // id - uuid
    private HashMap<Integer, Location> entityLocations; // check for hit collision on custom interact event

    private VisualGroup parent;

    public Visual(VisualType type, VisualGroup parent) {
        this.type = type;
        this.parent = parent;

        this.fakeEntities = new HashMap<>();
        this.entityLocations = new HashMap<>();
    }

    abstract void color(TeamColor color);

    abstract void spawn();

    protected void spawnEntity(int x, int z, int y){
        parent.getManager().spawnEntity(parent.getPlayer(),
                x,
                z,
                y,
                parent.generateUiniqueID(),
                parent.generateUiniqueUUID(),
                this);
    }

    protected void addSpawnData( int id, String uuid, Location location){
        fakeEntities.put(id, uuid);
        entityLocations.put(id, location);
    }

    public void remove(){
        getParent().getManager().despawnEntities(parent.getPlayer(), getEntityIDs());

        parent.removeVisual(this);
    }

    public VisualType getType() {
        return type;
    }

    public VisualGroup getParent() {
        return parent;
    }

    public ArrayList<Integer> getEntityIDs(){
        return new ArrayList<>(fakeEntities.keySet());
    }

    public ArrayList<String> getEntityUUIDs(){
        return new ArrayList<>(fakeEntities.values());
    }

    public Location getEntityLocation(int id){
        return entityLocations.get(id);
    }

    boolean containsID(int id){
        return fakeEntities.containsKey(id);
    }

    boolean containsUUID(String uuid){
        return fakeEntities.containsValue(uuid);
    }
}
