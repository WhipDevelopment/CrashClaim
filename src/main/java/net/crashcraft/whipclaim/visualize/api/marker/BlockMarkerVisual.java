package net.crashcraft.whipclaim.visualize.api.marker;

import net.crashcraft.whipclaim.visualize.api.VisualColor;
import net.crashcraft.whipclaim.visualize.api.VisualGroup;
import net.crashcraft.whipclaim.visualize.api.VisualType;
import net.crashcraft.whipclaim.visualize.api.visuals.BaseBlockVisual;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BlockMarkerVisual extends BaseBlockVisual {
    private Location location;

    public BlockMarkerVisual(VisualColor color, VisualGroup parent, Player player, Location location) {
        super(VisualType.MARKER, color, parent, player, location.getBlockY());
        this.location = location;
    }

    @Override
    public void spawn() {
        setBlock(getPlayer(), location, getColor().getMaterial());
    }

    @Override
    public void remove() {
        revertBlock(getPlayer(), location);
    }

    @Override
    public VisualColor getColor() {
        if (getDefaultColor() == null){
            return VisualColor.WHITE;
        }
        return getDefaultColor();
    }
}
