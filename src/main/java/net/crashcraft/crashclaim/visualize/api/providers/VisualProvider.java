package net.crashcraft.crashclaim.visualize.api.providers;

import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import org.bukkit.Location;

public interface VisualProvider {
    BaseVisual spawnClaimVisual(VisualColor color, VisualGroup parent, BaseClaim claim, int y);

    BaseVisual spawnMarkerVisual(VisualColor color, VisualGroup parent, Location location);
}
