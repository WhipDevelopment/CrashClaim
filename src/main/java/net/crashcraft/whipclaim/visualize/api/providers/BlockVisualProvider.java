package net.crashcraft.whipclaim.visualize.api.providers;

import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.visualize.api.*;
import net.crashcraft.whipclaim.visualize.api.claim.BlockClaimVisual;
import net.crashcraft.whipclaim.visualize.api.marker.BlockMarkerVisual;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BlockVisualProvider implements VisualProvider {
    @Override
    public BaseVisual spawnClaimVisual(VisualColor color, VisualGroup parent, BaseClaim claim, int y) {
        return new BlockClaimVisual(color, parent, parent.getPlayer(), y, claim);
    }

    @Override
    public BaseVisual spawnMarkerVisual(VisualColor color, VisualGroup parent, Location location) {
        return new BlockMarkerVisual(color, parent, parent.getPlayer(), location);
    }
}
