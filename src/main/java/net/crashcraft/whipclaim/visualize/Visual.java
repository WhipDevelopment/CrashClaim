package net.crashcraft.whipclaim.visualize;

import javax.xml.stream.Location;
import java.util.HashMap;
import java.util.UUID;

public abstract class Visual {
    private VisualType type;

    private VisualGroup parent;

    public Visual(VisualType type, VisualGroup parent) {
        this.type = type;
        this.parent = parent;
    }

    abstract void spawn();

    public void remove(){
        parent.signalRemoval(this);
    }

    protected void spawnEntity(Location location, TeamColor color){

    }

    public VisualType getType() {
        return type;
    }

    public VisualGroup getParent() {
        return parent;
    }
}
