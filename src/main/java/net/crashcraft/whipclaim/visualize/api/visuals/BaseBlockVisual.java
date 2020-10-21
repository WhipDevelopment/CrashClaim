package net.crashcraft.whipclaim.visualize.api.visuals;

import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.visualize.api.BaseVisual;
import net.crashcraft.whipclaim.visualize.api.VisualColor;
import net.crashcraft.whipclaim.visualize.api.VisualGroup;
import net.crashcraft.whipclaim.visualize.api.VisualType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public abstract class BaseBlockVisual extends BaseVisual {
    public BaseBlockVisual(VisualType type, VisualColor color, VisualGroup parent, Player player, int y) {
        super(type, color, parent, player, y);
    }

    public BaseBlockVisual(VisualType type, VisualColor color, VisualGroup parent, Player player, int y, BaseClaim claim) {
        super(type, color, parent, player, y, claim);
    }

    public void setBlock(Player player, Location location, Material material){
        player.sendBlockChange(location, material.createBlockData());
    }

    public void revertBlock(Player player, Location location){
        player.sendBlockChange(location, player.getWorld().getBlockAt(location).getBlockData());
    }
}