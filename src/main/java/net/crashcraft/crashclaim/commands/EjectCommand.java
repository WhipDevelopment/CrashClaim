package net.crashcraft.crashclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Flags;
import io.papermc.lib.PaperLib;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

@CommandAlias("claimeject|eject")
@CommandPermission("crashclaim.user.claimeject")
public class EjectCommand extends BaseCommand {
    private final ClaimDataManager manager;

    public EjectCommand(ClaimDataManager manager){
        this.manager = manager;
    }

    @Default
    @CommandCompletion("@players @nothing")
    public void onDefault(Player player, @Flags("other") String value){
        Player otherPlayer = CrashClaim.getPlugin().getServer().getPlayer(value);

        if (otherPlayer == null) {
            player.spigot().sendMessage(Localization.EJECT__INVALID_PLAYER.getMessage(player));
            return;
        }

        Location location = player.getLocation();
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim != null){
            if (!PermissionHelper.getPermissionHelper().hasPermission(player.getUniqueId(), player.getLocation(), PermissionRoute.MODIFY_PERMISSIONS)){
                player.spigot().sendMessage(Localization.EJECT__NO_PERMISSION.getMessage(player));
                return;
            }

            Location otherLocation = otherPlayer.getLocation();
            Claim otherClaim = manager.getClaim(otherLocation.getBlockX(), otherLocation.getBlockZ(), otherLocation.getWorld().getUID());

            if (!claim.equals(otherClaim)){
                player.spigot().sendMessage(Localization.EJECT__NOT_SAME_CLAIM.getMessage(player));
                return;
            }

            if (PermissionHelper.getPermissionHelper().hasPermission(otherPlayer.getUniqueId(), otherPlayer.getLocation(), PermissionRoute.MODIFY_PERMISSIONS)){
                player.spigot().sendMessage(Localization.EJECT__HAS_PERMISSION.getMessage(player));
                return;
            }

            if (GlobalConfig.useCommandInsteadOfEdgeEject){
                otherPlayer.performCommand(GlobalConfig.claimEjectCommand);
            } else {
                int distMax = Math.abs(location.getBlockX() - claim.getMaxX());
                int distMin = Math.abs(location.getBlockX() - claim.getMinX());

                World world = location.getWorld();
                if (distMax > distMin) {    //Find closest side
                    PaperLib.teleportAsync(otherPlayer, new Location(world, claim.getMinX() - 1,
                            world.getHighestBlockYAt(claim.getMinX() - 1,
                                    location.getBlockZ()), location.getBlockZ()));
                } else {
                    PaperLib.teleportAsync(otherPlayer, new Location(world, claim.getMaxX() + 1,
                            world.getHighestBlockYAt(claim.getMaxX() + 1,
                                    location.getBlockZ()), location.getBlockZ()));
                }
            }

            otherPlayer.spigot().sendMessage(Localization.EJECT__BEEN_EJECTED.getMessage(otherPlayer));
            player.spigot().sendMessage(Localization.EJECT__SUCCESS.getMessage(player));
        } else {
            player.spigot().sendMessage(Localization.EJECT__NO_CLAIM.getMessage(player));
        }
    }
}
