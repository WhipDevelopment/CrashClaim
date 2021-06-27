package net.crashcraft.crashclaim.pluginsupport.plugins;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
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
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardSupport implements PluginSupport {
    private StateFlag ALLOW_CLAIMING;

    @Override
    public boolean isUnSupportedVersion(String version) {
        return (int) version.charAt(0) < 7;
    }

    @Override
    public void init(Plugin plugin) {
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
    public void disable() {

    }

    @Override
    public boolean canClaim(Location minLoc, Location maxLoc){
        ProtectedRegion test = new ProtectedCuboidRegion("dummy",
                BlockVector3.at(minLoc.getX(), CrashClaim.getPlugin().getWrapper().getMinWorldHeight(minLoc.getWorld()), minLoc.getZ()),
                BlockVector3.at(maxLoc.getX(), maxLoc.getWorld().getMaxHeight(), maxLoc.getZ())
        );

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get((World) minLoc.getWorld());

        if (regions == null){
            return true;
        }

        ApplicableRegionSet set = regions.getApplicableRegions(test);

        return set.testState(null, ALLOW_CLAIMING);
    }
}
