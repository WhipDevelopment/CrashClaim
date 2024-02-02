package net.crashcraft.crashclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.crashutils.menusystem.defaultmenus.ConfirmationMenu;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.menus.ClaimMenu;
import net.crashcraft.crashclaim.menus.list.ClaimListMenu;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MenuCommand extends BaseCommand {
    private final ClaimDataManager manager;
    private final VisualizationManager visualizationManager;

    public MenuCommand(ClaimDataManager manager, VisualizationManager visualizationManager){
        this.manager = manager;
        this.visualizationManager = visualizationManager;
    }

    @CommandAlias("claims")
    @CommandPermission("crashclaim.user.claims")
    public void onClaimMenu(Player player){
        new ClaimListMenu(player, null).open();
    }

    @CommandAlias("claimsettings")
    @CommandPermission("crashclaim.user.claimsettings")
    public void onClaimMenuSingle(Player player){
        Location location = player.getLocation();
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim != null){
            new ClaimMenu(player, claim, null).open();
        } else {
            player.spigot().sendMessage(Localization.CLAIM_SETTINGS__NO_CLAIM.getMessage(player));
        }
    }

    @CommandAlias("unclaimsubclaim|removesubclaim")
    @CommandPermission("crashclaim.user.unclaimsubclaim")
    public void unSubClaim(Player player){
        Location location = player.getLocation();
        SubClaim claim = (manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID()) != null)
                ? manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID()).getSubClaim(location.getBlockX(), location.getBlockZ()) : null;
        if (claim != null) {
            ItemStack message = Localization.UN_SUBCLAIM__MENU__CONFIRMATION__MESSAGE.getItem(player);
            message.setType(GlobalConfig.visual_menu_items.getOrDefault(claim.getWorld(), Material.OAK_FENCE));

            new ConfirmationMenu(player,
                    Localization.UN_SUBCLAIM__MENU__CONFIRMATION__TITLE.getMessage(player),
                    message,
                    Localization.UN_SUBCLAIM__MENU__CONFIRMATION__ACCEPT.getItem(player),
                    Localization.UN_SUBCLAIM__MENU__CONFIRMATION__DENY.getItem(player),
                    (p, aBoolean) -> {
                        if (aBoolean) {
                            if (PermissionHelper.getPermissionHelper().hasPermission(claim, p.getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                                CrashClaim.getPlugin().getDataManager().deleteSubClaim(claim);

                                VisualGroup group = visualizationManager.fetchVisualGroup(player, false);
                                if (group != null){
                                    group.removeAllVisuals();

                                    BaseVisual visual = visualizationManager.getProvider(p.getUniqueId()).spawnClaimVisual(VisualColor.GREEN, group, claim.getParent(), player.getLocation().getBlockY() - 1);
                                    visual.spawn();

                                    visualizationManager.deSpawnAfter(visual, 10);
                                }
                            } else {
                                player.spigot().sendMessage(Localization.UN_SUBCLAIM__MENU__NO_PERMISSION.getMessage(player));
                            }
                        }
                        return "";
                    }, p -> "").open();
        } else {
            player.spigot().sendMessage(Localization.UN_SUBCLAIM__MENU__NO_CLAIM.getMessage(player));
        }
    }
}
