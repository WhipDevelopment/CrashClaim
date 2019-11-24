package net.crashcraft.whipclaim;

import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.data.ClaimResponse;
import net.crashcraft.whipclaim.data.StaticClaimLogic;
import net.crashcraft.whipclaim.visualize.VisualizationManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class WhipClaim extends JavaPlugin {
    private static WhipClaim plugin;

    private ClaimDataManager manager;
    private VisualizationManager visualizationManager;

    @Override
    public void onLoad() {
        plugin = this;

        visualizationManager = new VisualizationManager(this);
    }

    @Override
    public void onEnable() {
        manager = new ClaimDataManager(this);

        World world = Bukkit.getWorlds().get(0);

        Location loc1 = new Location(world, 50, 100, 40);
        Location loc2 = new Location(world, 20, 100, 30);

        ClaimResponse claimResponse = manager.createClaim(StaticClaimLogic.calculateUpperCorner(loc1, loc2), StaticClaimLogic.calculateLowerCorner(loc1, loc2), null);
        Claim claim = claimResponse.getClaim();

        System.out.println(claim.getUpperCornerX());
        System.out.println(claim.getUpperCornerZ());
        System.out.println(claim.getLowerCornerX());
        System.out.println(claim.getLowerCornerZ());
        System.out.println(claim.getId());
        System.out.println(claim.getWorld());
        System.out.println(claim.getPerms());

        manager.getClaim(1);
        manager.getClaim(2);

        for (Map.Entry<Integer, Claim> claimEntry : manager.temporaryTestGetClaimMap().entrySet()){
            System.out.println("id: " + claimEntry.getKey());
        }
    }

    @Override
    public void onDisable() {

    }

    public static WhipClaim getPlugin() {
        return plugin;
    }

    public ClaimDataManager getManager() {
        return manager;
    }

    public VisualizationManager getVisualizationManager() {
        return visualizationManager;
    }
}
