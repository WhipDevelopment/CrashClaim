package net.crashcraft.crashclaim.pluginsupport.plugins;

import com.google.auto.service.AutoService;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.config.GroupSettings;
import net.crashcraft.crashclaim.pluginsupport.PluginSupport;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.PermissionHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@AutoService(PluginSupport.class)
public class LuckPermsSupport implements PluginSupport {
    private LuckPerms luckPerms;

    @Override
    public boolean isUnSupportedVersion(String version) {
        return false;
    }

    @Override
    public boolean canLoad() {
        return Bukkit.getPluginManager().isPluginEnabled(getPluginName());
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
    public boolean canClaim(Location minLoc, Location maxLoc) {
        return true;
    }

    @Override
    public boolean canInteract(Player player, Location location) {
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
