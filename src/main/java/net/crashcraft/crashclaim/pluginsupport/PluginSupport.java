package net.crashcraft.crashclaim.pluginsupport;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public interface PluginSupport {
    boolean isUnSupportedVersion(String version);

    void init(Plugin plugin);

    void disable();

    boolean canClaim(Location minLoc, Location maxLoc);
}
