package net.crashcraft.crashclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@CommandAlias("show")
public class ShowClaimsCommand extends BaseCommand {
    private final VisualizationManager visualizationManager;
    private final ClaimDataManager claimDataManager;

    public ShowClaimsCommand(VisualizationManager visualizationManager, ClaimDataManager claimDataManager){
        this.visualizationManager = visualizationManager;
        this.claimDataManager = claimDataManager;
    }

    @Default
    @Subcommand("claims")
    @CommandPermission("crashclaim.user.show.claims")
    public void showClaims(Player player){
        visualizationManager.visualizeSurroundingClaims(player, claimDataManager);
        player.spigot().sendMessage(Localization.SHOW_CLAIMS__SUCCESS.getMessage(player));
    }

    @Subcommand("subclaims")
    @CommandPermission("crashclaim.user.show.subclaims")
    public void showSubClaims(Player player){
        Location location = player.getLocation();
        Claim claim = claimDataManager.getClaim(location.getBlockX(), location.getBlockZ(), player.getWorld().getUID());
        if (claim != null) {
            if (!PermissionHelper.getPermissionHelper().hasPermission(claim, player.getUniqueId(), PermissionRoute.VIEW_SUB_CLAIMS)){
                player.spigot().sendMessage(Localization.SHOW__SUBCLAIM__NO_PERMISSION.getMessage(player));
                return;
            }

            if (claim.getSubClaims().size() != 0){
                visualizationManager.visualizeSurroundingSubClaims(claim, player);
            } else {
                player.spigot().sendMessage(Localization.SHOW__SUBCLAIM__NO_SUBCLAIMS.getMessage(player));
            }
        } else {
            player.spigot().sendMessage(Localization.SHOW__SUBCLAIM__STAND_INSIDE.getMessage(player));
        }
    }
}
