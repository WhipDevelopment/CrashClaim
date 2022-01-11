package net.crashcraft.crashclaim.visualize.api;

import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class BaseVisual {
    private final VisualColor color;
    private final VisualGroup parent;
    private final Player player;
    private final VisualType type;
    private BaseClaim claim;
    private final int y;

    public BaseVisual(VisualType type, VisualColor color, VisualGroup parent, Player player, int y) {
        this.color = color;
        this.parent = parent;
        this.player = player;
        this.type = type;
        this.y = y;

        parent.addVisual(this);
    }

    public BaseVisual(VisualType type, VisualColor color, VisualGroup parent, Player player, int y, BaseClaim claim) {
        this.color = color;
        this.parent = parent;
        this.player = player;
        this.type = type;
        this.y = y;
        this.claim = claim;

        parent.addVisual(this);
    }

    public abstract void spawn();

    public abstract void remove();

    public abstract VisualColor getColor();

    public VisualColor getDefaultColor() {
        return color;
    }

    public VisualGroup getParent() {
        return parent;
    }

    public Player getPlayer() {
        return player;
    }

    public VisualType getType() {
        return type;
    }

    public int getY() {
        return y;
    }

    public BaseClaim getClaim() {
        return claim;
    }
}
