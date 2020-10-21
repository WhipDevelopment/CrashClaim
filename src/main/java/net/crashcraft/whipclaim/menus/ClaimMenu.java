package net.crashcraft.whipclaim.menus;

import dev.whip.crashutils.menusystem.GUI;
import dev.whip.crashutils.menusystem.defaultmenus.ConfirmationMenu;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import net.crashcraft.whipclaim.config.GlobalConfig;
import net.crashcraft.whipclaim.menus.list.PlayerPermListMenu;
import net.crashcraft.whipclaim.menus.list.SubClaimListMenu;
import net.crashcraft.whipclaim.menus.permissions.SimplePermissionMenu;
import net.crashcraft.whipclaim.permissions.PermissionHelper;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ClaimMenu extends GUI {
    private final Claim claim;
    private Material material;
    private final PermissionHelper helper;
    private final GUI previousMenu;

    public ClaimMenu(Player player, Claim claim, GUI previousMenu) {
        super(player, "Claim Menu", 54);
        this.claim = claim;
        this.previousMenu = previousMenu;
        this.helper = PermissionHelper.getPermissionHelper();
        setupGUI();
    }

    @Override
    public void initialize() {
        material = GlobalConfig.visual_menu_items.get(claim.getWorld());
    }

    @Override
    public void loadItems() {
        inv.setItem(13, createGuiItem(ChatColor.GOLD + claim.getName(),
                new ArrayList<>(Arrays.asList(
                        ChatColor.GREEN + "NW Corner: " + ChatColor.YELLOW + claim.getMinX() +
                                ", " + claim.getMinZ(),
                        ChatColor.GREEN + "SE Corner: " + ChatColor.YELLOW + claim.getMaxX() +
                                ", " + claim.getMaxZ())),
                material));

        if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
            inv.setItem(28, createGuiItem(ChatColor.GOLD + "Per Player settings",
                    new ArrayList<>(Collections.singleton(ChatColor.GREEN + "Edit claim permissions on a per player basis")), Material.PLAYER_HEAD));

            inv.setItem(29, createGuiItem(ChatColor.GOLD + "Global Claim settings",
                    new ArrayList<>(Collections.singleton(ChatColor.GREEN + "Set global permissions for your claim")), Material.COMPASS));
        } else {
            inv.setItem(28, createGuiItem(ChatColor.GRAY + "Per Player settings",
                    new ArrayList<>(Collections.singleton(ChatColor.DARK_GRAY + "Edit claim permissions on a per player basis")), Material.PLAYER_HEAD));

            inv.setItem(29, createGuiItem(ChatColor.GRAY + "Global Claim settings",
                    new ArrayList<>(Collections.singleton(ChatColor.DARK_GRAY + "Set global permissions for your claim")), Material.COMPASS));
        }

        inv.setItem(30, createGuiItem(ChatColor.GRAY + "No Sub Claims",
                new ArrayList<>(Collections.singleton(ChatColor.DARK_GRAY + "There are no sub claims you have permission to list.")), Material.WRITABLE_BOOK));

        for (SubClaim subClaim : claim.getSubClaims()){
            if (helper.hasPermission(subClaim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)
            || helper.hasPermission(subClaim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                inv.setItem(30, createGuiItem(ChatColor.GOLD + "Sub Claims",
                        new ArrayList<>(Collections.singleton(ChatColor.GREEN + "View the a list of the sub claims for this claim")), Material.WRITABLE_BOOK));
                break;
            }
        }

        if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
            inv.setItem(32, createGuiItem(ChatColor.GOLD + "Rename Claim",
                    new ArrayList<>(Collections.singleton(ChatColor.GREEN + "Rename your claim to easily identify it")), Material.ANVIL));

            inv.setItem(33, createGuiItem(ChatColor.GOLD + "Edit Entry Message",
                    new ArrayList<>(Collections.singleton(ChatColor.GREEN + "Edit the entry message of your claim")), Material.ANVIL));

            inv.setItem(34, createGuiItem(ChatColor.GOLD + "Edit Exit Message",
                    new ArrayList<>(Collections.singleton(ChatColor.GREEN + "Edit the exit message of your claim")), Material.ANVIL));

            inv.setItem(49, createGuiItem(ChatColor.GOLD + "Delete Claim",
                    new ArrayList<>(Collections.singleton(ChatColor.GREEN + "Delete your claim permanently")), Material.RED_CONCRETE));
        } else {
            inv.setItem(32, createGuiItem(ChatColor.GRAY + "Rename Claim",
                    new ArrayList<>(Collections.singleton(ChatColor.DARK_GRAY + "Rename your claim to easily identify it")), Material.ANVIL));

            inv.setItem(33, createGuiItem(ChatColor.GRAY + "Edit Entry Message",
                    new ArrayList<>(Collections.singleton(ChatColor.DARK_GRAY + "Edit the entry message of your claim")), Material.ANVIL));

            inv.setItem(34, createGuiItem(ChatColor.GRAY + "Edit Exit Message",
                    new ArrayList<>(Collections.singleton(ChatColor.DARK_GRAY + "Edit the exit message of your claim")), Material.ANVIL));

            inv.setItem(49, createGuiItem(ChatColor.GRAY + "Delete Claim",
                    new ArrayList<>(Collections.singleton(ChatColor.DARK_GRAY + "Delete your claim permanently")), Material.GRAY_CONCRETE));
        }

        if (previousMenu != null){
            inv.setItem(45, createGuiItem(ChatColor.GOLD + "Back", Material.ARROW));
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        if (event.getCurrentItem().getItemMeta().getDisplayName().charAt(1) == ChatColor.GRAY.getChar())
            return;

        switch (rawItemName){
            case "per player settings":
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
                    new PlayerPermListMenu(claim, getPlayer(), this);
                } else {
                    player.sendMessage(ChatColor.RED + "You no longer have sufficient permissions to continue");
                    forceClose();
                }
                break;
            case "global claim settings":
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
                    new SimplePermissionMenu(player, claim, null, this).open();
                } else {
                    player.sendMessage(ChatColor.RED + "You no longer have sufficient permissions to continue");
                    forceClose();
                }
                break;
            case "sub claims":
                new SubClaimListMenu(getPlayer(), this, claim).open();
                break;
            case "rename claim":
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    new AnvilGUI(WhipClaim.getPlugin(), getPlayer(), "Enter new claim name", (player, reply) -> {
                        claim.setName(reply);
                        player.sendMessage(ChatColor.GREEN + "Change claim name to " + ChatColor.GOLD + reply);

                        //TODO Make sure they cant set duplicate names maybe? might not matter because 2 claims can be named the same by 2 diffferent people then shared

                        return null;
                    });
                } else {
                    player.sendMessage(ChatColor.RED + "You no longer have sufficient permissions to continue");
                    forceClose();
                }
                break;
            case "edit entry message":
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    new AnvilGUI(WhipClaim.getPlugin(), getPlayer(), "Enter new claim entry message", (player, reply) -> {
                        claim.setEntryMessage(reply);
                        player.sendMessage(ChatColor.GREEN + "Change claim entry message to " + ChatColor.GOLD + reply);

                        return null;
                    });
                } else {
                    player.sendMessage(ChatColor.RED + "You no longer have sufficient permissions to continue");
                    forceClose();
                }

                break;
            case "edit exit message":
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    new AnvilGUI(WhipClaim.getPlugin(), getPlayer(), "Enter new claim exit message", (player, reply) -> {
                        claim.setExitMessage(reply);
                        player.sendMessage(ChatColor.GREEN + "Change claim exit message to " + ChatColor.GOLD + reply);

                        return null;
                    });
                } else {
                    player.sendMessage(ChatColor.RED + "You no longer have sufficient permissions to continue");
                    forceClose();
                }
                break;
            case "delete claim":
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    new ConfirmationMenu(getPlayer(),"Confirm Delete Claim",
                            ChatColor.DARK_RED + "Permanently Delete this claim?",
                            new ArrayList<>(Arrays.asList(ChatColor.RED + "Claim Blocks will be restored to ",
                                    ChatColor.RED + "the contributing parties")),
                            material,
                            (player, aBoolean) -> {
                                if (aBoolean) {
                                    if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
                                        WhipClaim.getPlugin().getDataManager().deleteClaim(claim);
                                    } else {
                                        player.sendMessage(ChatColor.RED + "You do not have permission to modify this claim.");
                                    }
                                }
                                return "";
                            }, player -> "").open();
                } else {
                    player.sendMessage(ChatColor.RED + "You no longer have sufficient permissions to continue");
                    forceClose();
                }
                break;
            case "back":
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
