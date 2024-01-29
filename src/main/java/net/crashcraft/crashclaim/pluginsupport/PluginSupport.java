package net.crashcraft.crashclaim.pluginsupport;

import net.crashcraft.crashclaim.config.GroupSettings;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface PluginSupport {
    boolean isUnSupportedVersion(String version);

    boolean canLoad();

    String getPluginName();

    void load(Plugin plugin);

    void enable(Plugin plugin);

    void disable();

    default boolean canClaim(Location minLoc, Location maxLoc) {
        return true;
    }

    default boolean canInteract(Player player, Location location) {
        return true;
    }

    default GroupSettings getPlayerGroupSettings(Player player) {
        return null;
    };
}
