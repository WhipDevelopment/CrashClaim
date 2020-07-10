package net.crashcraft.whipclaim.listeners;

import com.destroystokyo.paper.event.entity.ThrownEggHatchEvent;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.permissions.PermissionHelper;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import net.crashcraft.whipclaim.permissions.PermissionSetup;
import net.crashcraft.whipclaim.visualize.VisualizationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

    @EventHandler
    public void onThrownEggHatchEvent(ThrownEggHatchEvent e){
        if (!e.isHatching()){
            return;
        }

        if (e.getEgg().getShooter() instanceof Player) {
            Player player = (Player) e.getEgg().getShooter();
            if (!helper.hasPermission(player.getUniqueId(), e.getEgg().getLocation(), PermissionRoute.ENTITIES)){
                e.setHatching(false);
                visuals.sendAlert(player, "You do not have permission to interact with entities in this claim");
            }
        } else {
            if (!helper.hasPermission(e.getEgg().getLocation(), PermissionRoute.ENTITIES)){
                e.setHatching(false);
            }
        }
    }
}
