package net.crashcraft.crashclaim.pluginsupport;

import org.bukkit.plugin.Plugin;

public interface PluginSupportLoader {
    boolean isUnsupportedVersion(String version);

    boolean canLoad();

    String getPluginName();

    void load(Plugin plugin);
}
