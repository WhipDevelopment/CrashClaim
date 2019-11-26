package net.crashcraft.whipclaim.visualize;

import org.bukkit.Location;

public class MarkerVisual extends Visual{
    private Location location;

    public MarkerVisual(Location location) {
        super(VisualType.MARKER);
        this.location = location;
    }

    @Override
    public void spawn() {
        getParent().getManager().spawnEntity(getParent().getPlayer(),
                location.getBlockX(),
                location.getBlockZ(),
                location.getBlockY(),
                getParent().generateUiniqueID(),
                getParent().generateUiniqueUUID(),
                this);
    }

    @Override
    public void color(TeamColor color) {
        getParent().getManager().colorEntities(getParent().getPlayer(), color, getEntityUUIDs());
    }
}
