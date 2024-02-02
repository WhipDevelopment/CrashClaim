package net.crashcraft.crashclaim.pluginsupport.plugins;

import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.config.GroupSettings;
import net.crashcraft.crashclaim.pluginsupport.PluginSupport;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.PermissionHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LuckPermsSupport implements PluginSupport {
    private LuckPerms luckPerms;

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
        return "LuckPerms";
    }

    @Override
    public void load(Plugin plugin) {

    }

    @Override
    public void enable(Plugin plugin) {
        luckPerms = LuckPermsProvider.get();
    }

    @Override
    public void disable() {
        luckPerms = null;
    }

    @Override
    public GroupSettings getPlayerGroupSettings(Player player) {
        PermissionHolder user = luckPerms.getUserManager().getUser(player.getUniqueId());

        if (user == null){
            return null;
        }

        String value = user.getCachedData().getMetaData(user.getQueryOptions()).getPrimaryGroup();
        if (value == null) {
            return null;
        }

        return GlobalConfig.groupSettings.get(value);
    }
}
