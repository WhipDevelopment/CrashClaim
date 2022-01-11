package net.crashcraft.crashclaim.visualize.api.providers.glow;

import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.VisualType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GlowMarketVisual extends BaseGlowVisual {
    private final Location location;

    public GlowMarketVisual(VisualColor color, VisualGroup parent, Player player, Location location) {
        super(VisualType.MARKER, color, parent, player, location.getBlockY());
        this.location = location;
    }

    @Override
    public void remove() {
        removeAll();
    }

    @Override
    public void spawn() {
        spawnEntity(getParent().getPlayer(),
                location.getBlockX(),
                location.getBlockZ(),
                location.getBlockY(),
                getParent().generateUniqueID(),
                getParent().generateUiniqueUUID());

        colorEntities(getParent().getPlayer(), getColor(), getEntityUUIDs());
    }

    @Override
    public VisualColor getColor() {
        if (getDefaultColor() == null){
            return VisualColor.WHITE;
        }
        return getDefaultColor();
    }
}
