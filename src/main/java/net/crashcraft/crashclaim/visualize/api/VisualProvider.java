package net.crashcraft.crashclaim.visualize.api;

import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import org.bukkit.Location;

public interface VisualProvider {
    BaseVisual spawnClaimVisual(VisualColor color, VisualGroup parent, BaseClaim claim, int y);

    BaseVisual spawnMarkerVisual(VisualColor color, VisualGroup parent, Location location);
}
