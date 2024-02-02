package net.crashcraft.crashclaim.pluginsupport.plugins;

import dev.norska.dsw.api.DeluxeSellwandPreSellEvent;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.pluginsupport.PluginSupport;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.plugin.Plugin;

public class DeluxeSellwandSupport implements PluginSupport, Listener {

    private VisualizationManager visual;

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
        return "DeluxeSellwands";
    }

    @Override
    public void load(Plugin plugin) {

    }

    @Override
    public void enable(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, CrashClaim.getPlugin());
        visual = CrashClaim.getPlugin().getVisualizationManager();
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onWandUse(DeluxeSellwandPreSellEvent event) {
        Player player = event.getPlayer();
        PermissionHelper helper = PermissionHelper.getPermissionHelper();
        Location location = event.getClickedBlock().getLocation();

        if (event.getClickedBlock().getState() instanceof BlockInventoryHolder){
            if (helper.hasPermission(player.getUniqueId(), location, event.getClickedBlock().getType())){
                return;
            }

            event.setCancelled(true);
            visual.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__CONTAINERS.getMessage(player));
        } else if (!helper.hasPermission(player.getUniqueId(), location, PermissionRoute.INTERACTIONS)){
            event.setCancelled(true);
            visual.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__INTERACTION.getMessage(player));
        }
    }
}
