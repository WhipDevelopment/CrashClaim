package net.crashcraft.crashclaim.pluginsupport;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class PluginSupportDistributor implements PluginSupport{
    private final List<PluginSupport> enabled;

    public PluginSupportDistributor(PluginSupportManager manager){
        enabled = manager.getEnabledSupport();
    }

    @Override
    public boolean isUnSupportedVersion(String version) {
        throw new RuntimeException("Unsupported Operation.");
    }

    @Override
    public void init(Plugin plugin) {
        throw new RuntimeException("Unsupported Operation.");
    }

    @Override
    public void disable() {
        throw new RuntimeException("Unsupported Operation.");
    }

    @Override
    public boolean canClaim(Location minLoc, Location maxLoc) {
        for (PluginSupport support : enabled){
            if (!support.canClaim(minLoc, maxLoc)){
                return false;
            }
        }

        return true;
    }
}
