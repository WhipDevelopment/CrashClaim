package net.crashcraft.crashclaim.pluginsupport.plugins;

import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.config.GroupSettings;
import net.crashcraft.crashclaim.pluginsupport.PluginSupport;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LuckPermsSupport implements PluginSupport {
    private LuckPerms luckPerms;

    @Override
    public boolean isUnSupportedVersion(String version) {
        return false;
    }

    @Override
    public void init(Plugin plugin) {
        luckPerms = LuckPermsProvider.get();
    }

    @Override
    public void disable() {
        luckPerms = null;
    }

    @Override
    public boolean canClaim(Location minLoc, Location maxLoc) {
        return true;
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
