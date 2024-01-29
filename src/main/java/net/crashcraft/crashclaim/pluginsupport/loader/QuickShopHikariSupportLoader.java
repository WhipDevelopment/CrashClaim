package net.crashcraft.crashclaim.pluginsupport.loader;

import com.google.auto.service.AutoService;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.pluginsupport.PluginSupportLoader;
import net.crashcraft.crashclaim.pluginsupport.plugins.QuickShopHikariSupport;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@AutoService(PluginSupportLoader.class)
public class QuickShopHikariSupportLoader implements PluginSupportLoader {
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
    public void load(Plugin plugin) {
        CrashClaim.getPlugin().getPluginSupportManager().register(new QuickShopHikariSupport());
    }
}
