package net.crashcraft.whipclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.PermState;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.permissions.PermissionHelper;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import net.crashcraft.whipclaim.permissions.PermissionRouter;
import net.crashcraft.whipclaim.visualize.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@CommandAlias("show")
public class ShowClaimsCommand extends BaseCommand {
    private VisualizationManager visualizationManager;
    private ClaimDataManager claimDataManager;

    public ShowClaimsCommand(VisualizationManager visualizationManager, ClaimDataManager claimDataManager){
        this.visualizationManager = visualizationManager;
        this.claimDataManager = claimDataManager;
    }

    @Default
    @Subcommand("claims")
    @CommandPermission("crashclaim.user.show.claims")
    public void showClaims(Player player){
        visualizationManager.visualizeSuroudningClaims(player, claimDataManager);
    }

    @Subcommand("subclaims")
    @CommandPermission("crashclaim.user.show.subclaims")
    public void showSubClaims(Player player){
        Location location = player.getLocation();
        Claim claim = claimDataManager.getClaim(location.getBlockX(), location.getBlockZ(), player.getWorld().getUID());
        if (claim != null) {
            if (!PermissionHelper.getPermissionHelper().hasPermission(claim, player.getUniqueId(), PermissionRoute.VIEW_SUB_CLAIMS)){
                player.sendMessage(ChatColor.RED + "You need permission to view sub claims.");
                return;
            }

            if (claim.getSubClaims().size() != 0){
                visualizationManager.visualizeSuroudningSubClaims(claim, player);
            } else {
                player.sendMessage(ChatColor.GREEN + "There are no sub claims to visualize");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You need to stand in a claim to visualize its sub claims.");
        }
    }
}
