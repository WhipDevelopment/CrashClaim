package net.crashcraft.crashclaim.menus;

import dev.whip.crashutils.menusystem.GUI;
import dev.whip.crashutils.menusystem.defaultmenus.ConfirmationMenu;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.menus.list.PlayerPermListMenu;
import net.crashcraft.crashclaim.menus.list.SubClaimListMenu;
import net.crashcraft.crashclaim.menus.permissions.SimplePermissionMenu;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.md_5.bungee.api.chat.BaseComponent;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class ClaimMenu extends GUI {
    private final Claim claim;
    private final PermissionHelper helper;
    private final GUI previousMenu;

    private boolean hasSubClaims;

    public ClaimMenu(Player player, Claim claim, GUI previousMenu) {
        super(player, BaseComponent.toLegacyText(Localization.MENU__CLAIM__TITLE.getMessage(null)), 54);
        this.claim = claim;
        this.previousMenu = previousMenu;
        this.helper = PermissionHelper.getPermissionHelper();
        setupGUI();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void loadItems() {
        hasSubClaims = false;

        ItemStack descItem;

        if (claim.getOwner().equals(getPlayer().getUniqueId())){
            descItem = Localization.MENU__GENERAL__CLAIM_ITEM_NO_OWNER.getItem(player,
                    "name", claim.getName(),
                    "min_x", Integer.toString(claim.getMinX()),
                    "min_z", Integer.toString(claim.getMinZ()),
                    "max_x", Integer.toString(claim.getMaxX()),
                    "max_z", Integer.toString(claim.getMaxZ()),
                    "world", Bukkit.getWorld(claim.getWorld()).getName()
            );
        } else {
            descItem = Localization.MENU__GENERAL__CLAIM_ITEM.getItem(player,
                    "name", claim.getName(),
                    "min_x", Integer.toString(claim.getMinX()),
                    "min_z", Integer.toString(claim.getMinZ()),
                    "max_x", Integer.toString(claim.getMaxX()),
                    "max_z", Integer.toString(claim.getMaxZ()),
                    "world", Bukkit.getWorld(claim.getWorld()).getName(),
                    "owner", Bukkit.getOfflinePlayer(claim.getOwner()).getName()
            );
        }

        descItem.setType(GlobalConfig.visual_menu_items.getOrDefault(claim.getWorld(), Material.OAK_FENCE));
        inv.setItem(13, descItem);

        if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
            inv.setItem(28, Localization.MENU__PERMISSIONS__BUTTONS__PER_PLAYER.getItem(player));
            inv.setItem(29, Localization.MENU__PERMISSIONS__BUTTONS__GLOBAL.getItem(player));
        } else {
            inv.setItem(28, Localization.MENU__PERMISSIONS__BUTTONS__PER_PLAYER_DISABLED.getItem(player));
            inv.setItem(29, Localization.MENU__PERMISSIONS__BUTTONS__GLOBAL_DISABLED.getItem(player));
        }

        if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
            inv.setItem(32, Localization.MENU__PERMISSIONS__BUTTONS__RENAME.getItem(player));
            inv.setItem(33, Localization.MENU__PERMISSIONS__BUTTONS__EDIT_ENTRY.getItem(player));
            inv.setItem(34, Localization.MENU__PERMISSIONS__BUTTONS__EDIT_EXIT.getItem(player));
            inv.setItem(49, Localization.MENU__PERMISSIONS__BUTTONS__DELETE.getItem(player));
        } else {
            inv.setItem(32, Localization.MENU__PERMISSIONS__BUTTONS__RENAME_DISABLED.getItem(player));
            inv.setItem(33, Localization.MENU__PERMISSIONS__BUTTONS__EDIT_ENTRY_DISABLED.getItem(player));
            inv.setItem(34, Localization.MENU__PERMISSIONS__BUTTONS__EDIT_EXIT_DISABLED.getItem(player));
            inv.setItem(49, Localization.MENU__PERMISSIONS__BUTTONS__DELETE_DISABLED.getItem(player));
        }

        hasSubClaims = false;
        for (SubClaim subClaim : claim.getSubClaims()){
            if (helper.hasPermission(subClaim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)
                    || helper.hasPermission(subClaim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                inv.setItem(30, Localization.MENU__PERMISSIONS__BUTTONS_SUBCLAIMS.getItem(player));
                hasSubClaims = true;
                break;
            }
        }
        if (!hasSubClaims){
            inv.setItem(30, Localization.MENU__PERMISSIONS__BUTTONS_NO_SUBCLAIMS.getItem(player));
        }

        if (previousMenu != null){
            inv.setItem(45, Localization.MENU__GENERAL__BACK_BUTTON.getItem(player));
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        switch (event.getSlot()){
            case 28:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
                    new PlayerPermListMenu(claim, getPlayer(), this);
                } else {
                    player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                    forceClose();
                }
                break;
            case 29:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
                    new SimplePermissionMenu(player, claim, null, this).open();
                } else {
                    player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                    forceClose();
                }
                break;
            case 30:
                if (hasSubClaims) {
                    new SubClaimListMenu(getPlayer(), this, claim).open();
                }
                break;
            case 32:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    new AnvilGUI.Builder()
                            .plugin(CrashClaim.getPlugin())
                            .itemLeft(Localization.MENU__CLAIM__RENAME__MESSAGE.getItem(player))
                            .onClick((integer, snapshot) -> {
                                claim.setName(snapshot.getText(), true);
                                snapshot.getPlayer().spigot().sendMessage(Localization.MENU__CLAIM__RENAME__CONFIRMATION.getMessage(snapshot.getPlayer(),
                                        "name", snapshot.getText()));
                                return Collections.singletonList(AnvilGUI.ResponseAction.close());
                            }).open(getPlayer());
                } else {
                    player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                    forceClose();
                }
                break;
            case 33:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    new AnvilGUI.Builder()
                            .plugin(CrashClaim.getPlugin())
                            .itemLeft(Localization.MENU__CLAIM__ENTRY_MESSAGE__MESSAGE.getItem(player))
                            .onClick((integer, snapshot) -> {
                                claim.setEntryMessage(snapshot.getText(), true);
                                snapshot.getPlayer().spigot().sendMessage(Localization.MENU__CLAIM__ENTRY_MESSAGE__CONFIRMATION.getMessage(snapshot.getPlayer(),
                                        "entry_message", snapshot.getText()));
                                return Collections.singletonList(AnvilGUI.ResponseAction.close());
                            }).open(getPlayer());
                } else {
                    player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                    forceClose();
                }
                break;
            case 34:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    new AnvilGUI.Builder()
                            .plugin(CrashClaim.getPlugin())
                            .itemLeft(Localization.MENU__CLAIM__EXIT_MESSAGE__MESSAGE.getItem(player))
                            .onClick((integer, snapshot) -> {
                                claim.setExitMessage(snapshot.getText(), true);
                                snapshot.getPlayer().spigot().sendMessage(Localization.MENU__CLAIM__EXIT_MESSAGE__CONFIRMATION.getMessage(snapshot.getPlayer(),
                                        "exit_message", snapshot.getText()));
                                return Collections.singletonList(AnvilGUI.ResponseAction.close());
                            }).open(getPlayer());
                } else {
                    player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                    forceClose();
                }
                break;
            case 49:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    ItemStack message = Localization.UN_CLAIM__MENU__CONFIRMATION__MESSAGE.getItem(player);
                    message.setType(GlobalConfig.visual_menu_items.getOrDefault(claim.getWorld(), Material.OAK_FENCE));

                    new ConfirmationMenu(player,
                            Localization.UN_CLAIM__MENU__CONFIRMATION__TITLE.getMessage(player),
                            message,
                            Localization.UN_CLAIM__MENU__CONFIRMATION__ACCEPT.getItem(player),
                            Localization.UN_CLAIM__MENU__CONFIRMATION__DENY.getItem(player),
                            (player, aBoolean) -> {
                                if (aBoolean) {
                                    if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
                                        CrashClaim.getPlugin().getDataManager().deleteClaim(claim);
                                    } else {
                                        player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                                    }
                                }
                                return "";
                            }, player -> "").open();
                } else {
                    player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                    forceClose();
                }
                break;
            case 45:
                if (previousMenu == null){
                    return;
                }
                previousMenu.open();
                break;
        }
    }

    public GUI getPreviousMenu() {
        return previousMenu;
    }
}
