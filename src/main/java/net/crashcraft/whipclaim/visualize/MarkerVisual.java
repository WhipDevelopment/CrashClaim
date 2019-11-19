package net.crashcraft.whipclaim.visualize;

import org.bukkit.Location;

public class MarkerVisual extends Visual{
    private Location location;

    public MarkerVisual(Location location) {
        super(VisualType.MARKER);
        this.location = location;
    }

    @Override
    void spawn() {

    }
}
