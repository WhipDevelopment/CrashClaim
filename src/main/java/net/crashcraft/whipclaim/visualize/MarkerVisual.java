package net.crashcraft.whipclaim.visualize;

import org.bukkit.Location;

public class MarkerVisual extends Visual{
    private Location location;

    public MarkerVisual(Location location, VisualGroup parent) {
        super(VisualType.MARKER, parent);
        this.location = location;
    }

    @Override
    void spawn() {
        getParent().getManager().spawnEntity(getParent().getPlayer(),
                location.getBlockX(),
                location.getBlockZ(),
                location.getBlockY(),
                getParent().generateUiniqueID(),
                getParent().generateUiniqueUUID(),
                this);
    }

    @Override
    void color(TeamColor color) {
        getParent().getManager().colorEntities(getParent().getPlayer(), color, getEntityUUIDs());
    }
}
