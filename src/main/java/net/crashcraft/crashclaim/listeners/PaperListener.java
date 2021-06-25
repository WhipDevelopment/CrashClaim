package net.crashcraft.crashclaim.listeners;

import com.destroystokyo.paper.event.entity.ThrownEggHatchEvent;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.permissions.PermissionSetup;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PaperListener implements Listener {
    private final PermissionHelper helper;
    private final PermissionSetup perms;
    private final VisualizationManager visuals;
    private final ClaimDataManager manager;

    public PaperListener(ClaimDataManager manager, VisualizationManager visuals){
        this.manager = manager;
        this.perms = manager.getPermissionSetup();
        this.visuals = visuals;
        this.helper = PermissionHelper.getPermissionHelper();
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onThrownEggHatchEvent(ThrownEggHatchEvent e){
        if (!e.isHatching()){
            return;
        }

        if (e.getEgg().getShooter() instanceof Player) {
            Player player = (Player) e.getEgg().getShooter();
            if (!helper.hasPermission(player.getUniqueId(), e.getEgg().getLocation(), PermissionRoute.ENTITIES)){
                e.setHatching(false);
                visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__ENTITIES.getMessage());
            }
        } else {
            if (!helper.hasPermission(e.getEgg().getLocation(), PermissionRoute.ENTITIES)){
                e.setHatching(false);
            }
        }
    }
}
