package net.crashcraft.crashclaim.pluginsupport.plugins;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import net.crashcraft.crashclaim.pluginsupport.PluginSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class QuickShopHikariSupport implements PluginSupport {
    private QuickShopAPI quickShopAPI;

    @Override
    public void load(Plugin plugin) {

    }

    @Override
    public boolean isUnsupportedVersion(String version) {
        return false;
    }

    @Override
    public boolean canLoad() {
        return Bukkit.getPluginManager().getPlugin(getPluginName()) != null;
    }

    @Override
    public String getPluginName() {
        return "QuickShop-Hikari";
    }

    @Override
    public void enable(Plugin plugin) {
        this.quickShopAPI = QuickShopAPI.getInstance();
    }

    @Override
    public void disable() {

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