package net.crashcraft.whipclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Flags;
import io.papermc.lib.PaperLib;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.permissions.PermissionHelper;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import org.bukkit.*;
import org.bukkit.entity.Player;

@CommandAlias("claimeject|eject")
@CommandPermission("crashclaim.user.claimeject")
public class EjectCommand extends BaseCommand {
    private final ClaimDataManager manager;

    public EjectCommand(ClaimDataManager manager){
        this.manager = manager;
    }

    @Default
    public void onDefault(Player player, @Flags("other") Player otherPlayer){
        Location location = player.getLocation();
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim != null){
            if (!PermissionHelper.getPermissionHelper().hasPermission(player.getUniqueId(), player.getLocation(), PermissionRoute.MODIFY_PERMISSIONS)){
                player.sendMessage(ChatColor.RED + "You do not have the modify permission node.");
                return;
            }

            Location otherLocation = otherPlayer.getLocation();
            Claim otherClaim = manager.getClaim(otherLocation.getBlockX(), otherLocation.getBlockZ(), otherLocation.getWorld().getUID());

            if (!claim.equals(otherClaim)){
                player.sendMessage(ChatColor.RED + "That player is not standing in the same claim as you.");
                return;
            }

            if (PermissionHelper.getPermissionHelper().hasPermission(otherPlayer.getUniqueId(), otherPlayer.getLocation(), PermissionRoute.MODIFY_PERMISSIONS)){
                player.sendMessage(ChatColor.RED + "That player has the modify permissions node and cannot be ejected.");
                return;
            }

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

            player.sendMessage(ChatColor.GREEN + "You have successfully ejected that player to the edge of the claim.");
        } else {
            player.sendMessage(ChatColor.RED + "There is no claim where you are standing.");
        }
    }
}
