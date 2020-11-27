package net.crashcraft.crashclaim.visualize.api.providers;

import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.VisualProvider;
import net.crashcraft.crashclaim.visualize.api.claim.GlowClaimVisual;
import net.crashcraft.crashclaim.visualize.api.marker.GlowMarketVisual;
import org.bukkit.Location;

public class GlowVisualProvider implements VisualProvider {
    @Override
    public BaseVisual spawnClaimVisual(VisualColor color, VisualGroup parent, BaseClaim claim, int y) {
        return new GlowClaimVisual(color, parent, parent.getPlayer(), y, claim);
    }

    @Override
    public BaseVisual spawnMarkerVisual(VisualColor color, VisualGroup parent, Location location) {
        return new GlowMarketVisual(color, parent, parent.getPlayer(), location);
    }
}
