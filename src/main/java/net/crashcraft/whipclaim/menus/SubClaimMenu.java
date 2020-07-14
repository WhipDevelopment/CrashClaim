package net.crashcraft.whipclaim.menus;

import dev.whip.crashutils.menusystem.GUI;
import dev.whip.crashutils.menusystem.defaultmenus.ConfirmationMenu;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import net.crashcraft.whipclaim.menus.list.SubClaimListMenu;
import net.crashcraft.whipclaim.menus.player.PlayerPermListMenu;
import net.crashcraft.whipclaim.menus.sub.GlobalSubClaimPermissionsMenu;
import net.crashcraft.whipclaim.permissions.PermissionHelper;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class SubClaimMenu extends GUI {
    private final SubClaim claim;
    private Material material;
    private final PermissionHelper helper;

    public SubClaimMenu(Player player, SubClaim claim) {
        super(player, "Claim Menu", 54);
        this.claim = claim;
        this.helper = PermissionHelper.getPermissionHelper();
        setupGUI();
    }

    @Override
    public void initialize() {
        material = Material.PAPER;
    }

    @Override
    public void loadItems() {
        ArrayList<String> desc = new ArrayList<>(Arrays.asList(
                ChatColor.GREEN + "Coordinates: " + ChatColor.YELLOW +
                        ChatColor.YELLOW + claim.getMinX() + ", " + claim.getMinZ()
                        + ChatColor.GOLD + ", " +
                        ChatColor.YELLOW + claim.getMaxX() + ", " + claim.getMaxZ(),
                ChatColor.GREEN + "World: " + ChatColor.YELLOW + Bukkit.getWorld(claim.getWorld()).getName()
        ));

        if (!claim.getParent().getOwner().equals(getPlayer().getUniqueId())){
            desc.add(ChatColor.GREEN + "Owner: " + ChatColor.YELLOW + Bukkit.getOfflinePlayer(claim.getParent().getOwner()).getName());
        }

        inv.setItem(13, createGuiItem(ChatColor.GOLD + claim.getName(), desc, material));

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

        inv.setItem(45, createGuiItem(ChatColor.GOLD + "Back", Material.ARROW));
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        if (event.getCurrentItem().getItemMeta().getDisplayName().charAt(1) == ChatColor.GRAY.getChar())
            return;

        switch (rawItemName) {
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
                    new GlobalSubClaimPermissionsMenu(getPlayer(), claim.getPerms()).open();
                } else {
                    player.sendMessage(ChatColor.RED + "You no longer have sufficient permissions to continue");
                    forceClose();
                }
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
                    new ConfirmationMenu(getPlayer(), "Confirm Delete Claim",
                            ChatColor.DARK_RED + "Permanently Delete this sub claim?",
                            new ArrayList<>(Arrays.asList(ChatColor.RED + "Claim Blocks will be restored to ",
                                    ChatColor.RED + "the contributing parties")),
                            material,
                            (player, aBoolean) -> {
                                if (aBoolean) {
                                    if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                                        WhipClaim.getPlugin().getDataManager().deleteSubClaim(claim);
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
                Claim main = claim.getParent();
                new SubClaimListMenu(getPlayer(), new ClaimMenu(getPlayer(), main, null), main).open();
                break;
        }
    }
}
