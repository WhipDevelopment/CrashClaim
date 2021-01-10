package net.crashcraft.crashclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import io.papermc.lib.PaperLib;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import org.bukkit.ChatColor;
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
    public void onDefault(Player player, @Flags("other") Player otherPlayer){
        Location location = player.getLocation();
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim != null){
            if (!PermissionHelper.getPermissionHelper().hasPermission(player.getUniqueId(), player.getLocation(), PermissionRoute.MODIFY_PERMISSIONS)){
                player.sendMessage(ChatColor.RED + "You do not have the modify permission ability inside this claim.");
                return;
            }

            Location otherLocation = otherPlayer.getLocation();
            Claim otherClaim = manager.getClaim(otherLocation.getBlockX(), otherLocation.getBlockZ(), otherLocation.getWorld().getUID());

            if (!claim.equals(otherClaim)){
                player.sendMessage(ChatColor.RED + "That player is not standing in the same claim as you.");
                return;
            }

            if (PermissionHelper.getPermissionHelper().hasPermission(otherPlayer.getUniqueId(), otherPlayer.getLocation(), PermissionRoute.MODIFY_PERMISSIONS)){
                player.sendMessage(ChatColor.RED + "That player has the modify permissions ability inside this claim and cannot be ejected.");
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

            otherPlayer.sendMessage(ChatColor.RED + "You have been ejected from the claim you were standing in by another player.");
            player.sendMessage(ChatColor.GREEN + "You have successfully ejected that player to the edge of the claim.");
        } else {
            player.sendMessage(ChatColor.RED + "There is no claim where you are standing.");
        }
    }
}
