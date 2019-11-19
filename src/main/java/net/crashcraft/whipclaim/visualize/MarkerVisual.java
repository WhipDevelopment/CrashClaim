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

    }
}
