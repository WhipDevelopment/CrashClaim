package net.crashcraft.crashclaim.visualize.api.providers.block;

import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.providers.VisualProvider;
import org.bukkit.Location;

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
