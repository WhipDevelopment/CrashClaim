package net.crashcraft.crashclaim.update;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.localization.Localization;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateManager implements Listener {
   private final UpdateChecker updateChecker;

    public UpdateManager(CrashClaim crashClaim){
        updateChecker = UpdateChecker.init(crashClaim, 94037);

        // Run every 12 hours for servers that do not restart
        Bukkit.getScheduler().runTaskTimerAsynchronously(crashClaim, () ->
                updateChecker.requestUpdateCheck().whenComplete((result, exception) -> {
                    if (result.requiresUpdate()) {
                        return;
                    }

                    UpdateChecker.UpdateReason reason = result.getReason();
                    if (reason != UpdateChecker.UpdateReason.UP_TO_DATE && reason != UpdateChecker.UpdateReason.UNRELEASED_VERSION) {
                        crashClaim.getLogger().warning("Could not check for a new version of CrashClaim. Reason: " + reason);
                    }
                }), 0, 20 * 60 * 60 * 12);

        // Run every 1 hour for servers to fetch latest version for login notify.
        Bukkit.getScheduler().runTaskTimerAsynchronously(crashClaim, updateChecker::requestUpdateCheck, 0, 20 * 60 * 60);

        // Notify console every 12 hours for servers that do restart often. Offset 2 minutes to provide enough time for a response.
        Bukkit.getScheduler().runTaskTimerAsynchronously(crashClaim, () -> {
            UpdateChecker.UpdateResult result = updateChecker.getLastResult();

            if (result != null && result.requiresUpdate()) {
                crashClaim.getLogger().info(String.format("CrashClaim has a new version available to download on SpigotMC! %s", result.getNewestVersion()));
                crashClaim.getLogger().info("Download now at https://whips.dev/crashclaim");
            }
        }, 20 * 60 * 2, 20 * 60 * 60 * 24);

        Bukkit.getPluginManager().registerEvents(this, crashClaim);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        if (updateChecker.getLastResult() == null || !updateChecker.getLastResult().requiresUpdate()){
            return;
        }

        Player player = event.getPlayer();

        if (player.hasPermission("crashclaim.admin.notifyupdate")){
            player.spigot().sendMessage(Localization.UPDATE_AVAILABLE.getMessage(player,
                    "version", updateChecker.getLastResult().getNewestVersion()));
        }
    }
}
