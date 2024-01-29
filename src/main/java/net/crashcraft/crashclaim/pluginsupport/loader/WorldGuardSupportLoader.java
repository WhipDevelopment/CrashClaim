package net.crashcraft.crashclaim.pluginsupport.loader;

import com.google.auto.service.AutoService;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.pluginsupport.PluginSupportLoader;
import net.crashcraft.crashclaim.pluginsupport.plugins.WorldGuardSupport;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@AutoService(PluginSupportLoader.class)
public class WorldGuardSupportLoader implements PluginSupportLoader {
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
        CrashClaim.getPlugin().getPluginSupportManager().register(new WorldGuardSupport());
    }
}
