package net.crashcraft.crashclaim.pluginsupport.plugins;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.pluginsupport.PluginSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class WorldGuardSupport implements PluginSupport {

    private StateFlag ALLOW_CLAIMING;

    @Override
    public boolean isUnsupportedVersion(String version) {
        return (int) version.charAt(0) < 7;
    }

    @Override
    public boolean canLoad() {
        return Bukkit.getPluginManager().getPlugin(getPluginName()) != null;
    }

    @Override
    public String getPluginName() {
        return "WorldGuard";
    }

    @Override
    public void load(Plugin plugin) {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("allow-claiming", true);
            registry.register(flag);
            ALLOW_CLAIMING = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("allow-claiming");
            if (existing instanceof StateFlag) {
                ALLOW_CLAIMING = (StateFlag) existing;
            } else {
                StateFlag flag = new StateFlag("crashclaim-allow-claiming", true);

                CrashClaim.getPlugin().getLogger().warning("[WorldGuard] The flag 'allow-claiming' was already initialized by another plugin, using 'crashclaim-allow-claiming' now.");

                registry.register(flag);
                ALLOW_CLAIMING = flag;
            }
        }
    }

    @Override
    public void enable(Plugin plugin) {

    }

    @Override
    public void disable() {

    }

    @Override
    public boolean canClaim(Location minLoc, Location maxLoc){
        ProtectedRegion test = new ProtectedCuboidRegion("dummy",
                BlockVector3.at(minLoc.getX(), minLoc.getWorld().getMinHeight(), minLoc.getZ()),
                BlockVector3.at(maxLoc.getX(), maxLoc.getWorld().getMaxHeight(), maxLoc.getZ())
        );

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(minLoc.getWorld()));

        if (regions == null){
            return true;
        }

        ApplicableRegionSet set = regions.getApplicableRegions(test);

        return set.testState(null, ALLOW_CLAIMING);
    }
}
