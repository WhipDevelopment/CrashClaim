package net.crashcraft.crashclaim.commands.claiming;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.crashutils.menusystem.defaultmenus.ConfirmationMenu;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

@CommandAlias("unclaim|removeclaim")
public class UnClaimCommand extends BaseCommand {
    private final ClaimDataManager manager;
    private final VisualizationManager visualizationManager;

    public UnClaimCommand(ClaimDataManager manager, VisualizationManager visualizationManager){
        this.manager = manager;
        this.visualizationManager = visualizationManager;
    }

    @Default
    @CommandPermission("crashclaim.user.unclaim")
    public void unClaim(Player player){
        Location location = player.getLocation();
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());

        if (claim != null){
            ItemStack message = Localization.UN_CLAIM__MENU__CONFIRMATION__MESSAGE.getItem(player);
            message.setType(GlobalConfig.visual_menu_items.getOrDefault(claim.getWorld(), Material.OAK_FENCE));

            new ConfirmationMenu(player,
                    Localization.UN_CLAIM__MENU__CONFIRMATION__TITLE.getMessage(player),
                    message,
                    Localization.UN_CLAIM__MENU__CONFIRMATION__ACCEPT.getItem(player),
                    Localization.UN_CLAIM__MENU__CONFIRMATION__DENY.getItem(player),
                    (p, aBoolean) -> {
                        if (aBoolean) {
                            if (!PermissionHelper.getPermissionHelper().hasPermission(claim, p.getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                                player.spigot().sendMessage(Localization.UN_CLAIM__NO_PERMISSION.getMessage(player));
                                return "";
                            }

                            for (SubClaim subClaim : claim.getSubClaims()){
                                if (!PermissionHelper.getPermissionHelper().hasPermission(subClaim, p.getUniqueId(), PermissionRoute.MODIFY_CLAIM)){
                                    player.spigot().sendMessage(Localization.UN_CLAIM__NO_PERMISSION_IN_ALL.getMessage(player));
                                    return "";
                                }
                            }

                            CrashClaim.getPlugin().getDataManager().deleteClaim(claim);
                            VisualGroup group = visualizationManager.fetchVisualGroup(player, false);
                            if (group != null){
                                group.removeAllVisuals();
                            }
                        }
                        return "";
                    }, p -> "").open();
        } else {
            player.spigot().sendMessage(Localization.UN_CLAIM__NO_CLAIM.getMessage(player));
        }
    }

    @Subcommand("all")
    @CommandPermission("crashclaim.user.unclaimall")
    @CommandCompletion("@players")
    public void unClaimAll(Player player){
        ArrayList<Claim> claims = manager.getOwnedParentClaims(player.getUniqueId());

        if (claims.size() > 0) {
            new ConfirmationMenu(player,
                    Localization.UN_CLAIM_ALL__MENU__CONFIRMATION__TITLE.getMessage(player),
                    Localization.UN_CLAIM_ALL__MENU__CONFIRMATION__MESSAGE.getItem(player),
                    Localization.UN_CLAIM_ALL__MENU__CONFIRMATION__ACCEPT.getItem(player),
                    Localization.UN_CLAIM_ALL__MENU__CONFIRMATION__DENY.getItem(player),
                    (p, aBoolean) -> {
                        if (aBoolean) {
                            for (Claim claim : claims) {
                                String name = claim.getName();

                                if (!PermissionHelper.getPermissionHelper().hasPermission(claim, p.getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                                    player.spigot().sendMessage(Localization.UN_CLAIM_ALL__NO_PERMISSION.getMessage(player, "name", name));
                                    return "";
                                }

                                for (SubClaim subClaim : claim.getSubClaims()) {
                                    if (!PermissionHelper.getPermissionHelper().hasPermission(subClaim, p.getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                                        player.spigot().sendMessage(Localization.UN_CLAIM_ALL__NO_PERMISSION_IN_ALL.getMessage(player, "name", name));
                                        return "";
                                    }
                                }

                                CrashClaim.getPlugin().getDataManager().deleteClaim(claim);
                                VisualGroup group = visualizationManager.fetchVisualGroup(player, false);
                                if (group != null) {
                                    group.removeAllVisuals();
                                }
                            }
                        }
                        return "";
                    }, p -> "").open();
        } else {
            player.spigot().sendMessage(Localization.UN_CLAIM_ALL__NO_CLAIM.getMessage(player));
        }
    }

    @Subcommand("all")
    @CommandPermission("crashclaim.admin.unclaimall")
    public void unClaimAll(Player player, @Flags("other") OfflinePlayer otherPlayer){
        ArrayList<Claim> claims = manager.getOwnedParentClaims(otherPlayer.getUniqueId());

        if (claims.size() > 0) {
            new ConfirmationMenu(player,
                    Localization.UN_CLAIM_ALL__MENU__CONFIRMATION__TITLE.getMessage(player),
                    Localization.UN_CLAIM_ALL__MENU__CONFIRMATION__MESSAGE.getItem(player),
                    Localization.UN_CLAIM_ALL__MENU__CONFIRMATION__ACCEPT.getItem(player),
                    Localization.UN_CLAIM_ALL__MENU__CONFIRMATION__DENY.getItem(player),
                    (p, aBoolean) -> {
                        if (aBoolean) {
                            for (Claim claim : claims) {
                                // Admin Command no need for permission checks

                                CrashClaim.getPlugin().getDataManager().deleteClaim(claim);
                                VisualGroup group = visualizationManager.fetchVisualGroup(player, false);
                                if (group != null) {
                                    group.removeAllVisuals();
                                }

                                if (otherPlayer.isOnline()){
                                    CrashClaim.getPlugin().getDataManager().deleteClaim(claim);
                                    VisualGroup group2 = visualizationManager.fetchVisualGroup(otherPlayer.getPlayer(), false);
                                    if (group2 != null) {
                                        group2.removeAllVisuals();
                                    }
                                }
                            }
                        }
                        return "";
                    }, p -> "").open();
        } else {
            player.spigot().sendMessage(Localization.UN_CLAIM_ALL__NO_CLAIM.getMessage(player));
        }
    }
}
