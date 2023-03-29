package net.crashcraft.crashclaim.pluginsupport.plugins;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.config.GroupSettings;
import net.crashcraft.crashclaim.pluginsupport.PluginSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.maxgamer.quickshop.api.QuickShopAPI;
import org.maxgamer.quickshop.api.shop.Shop;

public class QuickShopSupport implements PluginSupport {

    private QuickShopAPI quickShopAPI;

    @Override
    public boolean isUnSupportedVersion(String version) {
        return (int) version.charAt(0) < 7;
    }

    @Override
    public void onLoad(Plugin plugin) {

    }

    @Override
    public void onEnable(Plugin plugin) {
        Plugin quickshop = Bukkit.getPluginManager().getPlugin("QuickShop");
        quickShopAPI = (QuickShopAPI)quickshop;
    }

    @Override
    public void disable() {

    }

    @Override
    public boolean canClaim(Location minLoc, Location maxLoc) {
        return true;
    }

    @Override
    public boolean canInteract(Player player, Location location) {
        if (!(location.getBlock().getBlockData() instanceof Directional directional)) {
            return false;
        }

        BlockFace face = directional.getFacing();
        BlockFace invertedFace = invertFace(face);

        if (invertedFace == null) {
            return false;
        }

        Block block = location.getBlock().getRelative(invertedFace);
        Shop shop = quickShopAPI.getShopManager().getShop(block.getLocation());

        return shop != null;
    }

    @Override
    public GroupSettings getPlayerGroupSettings(Player player) {
        return null;
    }

    private BlockFace invertFace(BlockFace face){
        return switch (face) {
            case NORTH -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.NORTH;
            case EAST -> BlockFace.WEST;
            case WEST -> BlockFace.EAST;
            case NORTH_EAST -> BlockFace.SOUTH_WEST;
            case NORTH_WEST -> BlockFace.SOUTH_EAST;
            case SOUTH_EAST -> BlockFace.NORTH_WEST;
            case SOUTH_WEST -> BlockFace.NORTH_EAST;
            case EAST_NORTH_EAST -> BlockFace.WEST_SOUTH_WEST;
            case EAST_SOUTH_EAST -> BlockFace.WEST_NORTH_WEST;
            case NORTH_NORTH_EAST -> BlockFace.SOUTH_SOUTH_WEST;
            case NORTH_NORTH_WEST -> BlockFace.SOUTH_SOUTH_EAST;
            case SOUTH_SOUTH_EAST -> BlockFace.NORTH_NORTH_WEST;
            case SOUTH_SOUTH_WEST -> BlockFace.NORTH_NORTH_EAST;
            case WEST_NORTH_WEST -> BlockFace.EAST_SOUTH_EAST;
            case WEST_SOUTH_WEST -> BlockFace.EAST_NORTH_EAST;
            case UP -> BlockFace.DOWN;
            case DOWN -> BlockFace.UP;
            case SELF -> BlockFace.SELF;
        };
    }
}
