package net.crashcraft.whipclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import dev.whip.crashutils.menusystem.defaultmenus.ConfirmationMenu;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import net.crashcraft.whipclaim.config.ValueConfig;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.menus.AllClaimListMenu;
import net.crashcraft.whipclaim.menus.ClaimMenu;
import net.crashcraft.whipclaim.permissions.PermissionHelper;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

public class MenuCommand extends BaseCommand {
    private ClaimDataManager manager;

    public MenuCommand(ClaimDataManager manager){
        this.manager = manager;
    }

    @CommandAlias("claims")
    @CommandPermission("crashclaim.user.claims")
    public void onClaimMenu(Player player){
        new AllClaimListMenu(player, null);
    }

    @CommandAlias("claimsettings")
    @CommandPermission("crashclaim.user.claimsettings")
    public void onClaimMenuSingle(Player player){
        Location location = player.getLocation();
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim != null){
            new ClaimMenu(player, claim).open();
        } else {
            player.sendMessage(ChatColor.RED + "There is no claim where you are standing.");
        }
    }

    @CommandAlias("unclaim|removeclaim")
    @CommandPermission("crashclaim.user.unclaim")
    public void unClaim(Player player){
        Location location = player.getLocation();
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim != null){
            new ConfirmationMenu(player,"Confirm Delete Claim",
                    ChatColor.DARK_RED + "Permanently Delete this claim?",
                    new ArrayList<>(Arrays.asList(ChatColor.RED + "Claim Blocks will be restored to ",
                            ChatColor.RED + "the contributing parties")),
                    ValueConfig.MENU_VISUAL_CLAIM_ITEMS.get(claim.getWorld()),
                    (p, aBoolean) -> {
                        if (aBoolean) {
                            if (PermissionHelper.getPermissionHelper().hasPermission(claim, p.getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
                                WhipClaim.getPlugin().getDataManager().deleteClaim(claim);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to modify this claim.");
                            }
                        }
                        return "";
                    }, p -> "").open();
        } else {
            player.sendMessage(ChatColor.RED + "There is no claim where you are standing.");
        }
    }

    @CommandAlias("unclaimsubclaim|removesubclaim")
    @CommandPermission("crashclaim.user.unclaimsubclaim")
    public void unSubClaim(Player player){
        Location location = player.getLocation();
        SubClaim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID()).getSubClaim(location.getBlockX(), location.getBlockZ());
        if (claim != null){
            new ConfirmationMenu(player,"Confirm Delete Claim",
                    ChatColor.DARK_RED + "Permanently Delete this claim?",
                    new ArrayList<>(Arrays.asList(ChatColor.RED + "Claim Blocks will be restored to ",
                            ChatColor.RED + "the contributing parties")),
                    ValueConfig.MENU_VISUAL_CLAIM_ITEMS.get(claim.getWorld()),
                    (p, aBoolean) -> {
                        if (aBoolean) {
                            if (PermissionHelper.getPermissionHelper().hasPermission(claim, p.getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
                                WhipClaim.getPlugin().getDataManager().deleteSubClaim(claim);
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to modify this sub claim.");
                            }
                        }
                        return "";
                    }, p -> "").open();
        } else {
            player.sendMessage(ChatColor.RED + "There is no sub claim where you are standing.");
        }
    }
}
