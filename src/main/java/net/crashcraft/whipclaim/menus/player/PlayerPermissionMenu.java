package net.crashcraft.whipclaim.menus.player;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.whipclaim.menus.ClaimMenu;
import net.crashcraft.whipclaim.menus.SubClaimMenu;
import net.crashcraft.whipclaim.menus.sub.SubPlayerAdminPermissions;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class PlayerPermissionMenu extends GUI {
    private PermissionGroup group;
    private PlayerPermissionSet permissionSet;
    private UUID target;

    public PlayerPermissionMenu(Player player, PermissionGroup group, UUID target) {
        super(player, "General Permissions", 54);
        this.group = group;
        this.target = target;
        this.permissionSet = group.getPlayerPermissionSet(target);
        setupGUI();
    }

    @Override
    public void initialize() {

    }

    @SuppressWarnings("Duplicates")
    @Override
    public void loadItems() {
        inv.clear();

        inv.setItem(10, createGuiItem(ChatColor.GOLD + "Build", Material.GRASS_BLOCK));
        inv.setItem(11, createGuiItem(ChatColor.GOLD + "Entities", Material.CREEPER_HEAD));
        inv.setItem(12, createGuiItem(ChatColor.GOLD + "Interactions", Material.OAK_FENCE_GATE));
        inv.setItem(13, createGuiItem(ChatColor.GOLD + "Explosions", Material.TNT));
        inv.setItem(14, createGuiItem(ChatColor.GOLD + "Teleportation", Material.ENDER_PEARL));

        switch (PermissionRoute.BUILD.getPerm(permissionSet)){
            case 1:
                inv.setItem(19, createGuiItem(ChatColor.GREEN + "Enabled", Material.GREEN_CONCRETE));
                break;
            case 2:
                inv.setItem(28, createGuiItem(ChatColor.GRAY + "Neutral", Material.GRAY_CONCRETE));
                break;
            case 0:
                inv.setItem(37, createGuiItem(ChatColor.RED + "Disabled", Material.RED_CONCRETE));
                break;
        }

        switch (PermissionRoute.ENTITIES.getPerm(permissionSet)){
            case 1:
                inv.setItem(20, createGuiItem(ChatColor.GREEN + "Enabled", Material.GREEN_CONCRETE));
                break;
            case 2:
                inv.setItem(29, createGuiItem(ChatColor.GRAY + "Neutral", Material.GRAY_CONCRETE));
                break;
            case 0:
                inv.setItem(38, createGuiItem(ChatColor.RED + "Disabled", Material.RED_CONCRETE));
                break;
        }

        switch (PermissionRoute.INTERACTIONS.getPerm(permissionSet)){
            case 1:
                inv.setItem(21, createGuiItem(ChatColor.GREEN + "Enabled", Material.GREEN_CONCRETE));
                break;
            case 2:
                inv.setItem(30, createGuiItem(ChatColor.GRAY + "Neutral", Material.GRAY_CONCRETE));
                break;
            case 0:
                inv.setItem(39, createGuiItem(ChatColor.RED + "Disabled", Material.RED_CONCRETE));
                break;
        }

        switch (PermissionRoute.EXPLOSIONS.getPerm(permissionSet)){
            case 1:
                inv.setItem(22, createGuiItem(ChatColor.GREEN + "Enabled", Material.GREEN_CONCRETE));
                break;
            case 2:
                inv.setItem(31, createGuiItem(ChatColor.GRAY + "Neutral", Material.GRAY_CONCRETE));
                break;
            case 0:
                inv.setItem(40, createGuiItem(ChatColor.RED + "Disabled", Material.RED_CONCRETE));
                break;
        }

        switch (PermissionRoute.TELEPORTATION.getPerm(permissionSet)){
            case 1:
                inv.setItem(23, createGuiItem(ChatColor.GREEN + "Enabled", Material.GREEN_CONCRETE));
                break;
            case 2:
                inv.setItem(32, createGuiItem(ChatColor.GRAY + "Neutral", Material.GRAY_CONCRETE));
                break;
            case 0:
                inv.setItem(41, createGuiItem(ChatColor.RED + "Disabled", Material.RED_CONCRETE));
                break;
        }

        for (int start = 19; start < 24; start++){
            ItemStack itemStack = inv.getItem(start);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)){
                inv.setItem(start, createGuiItem(ChatColor.DARK_GREEN + "Enable", Material.GREEN_STAINED_GLASS));
            }
        }

        for (int start = 28; start < 33; start++){
            ItemStack itemStack = inv.getItem(start);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)){
                inv.setItem(start, createGuiItem(ChatColor.DARK_GRAY + "Neutral", Material.GRAY_STAINED_GLASS));
            }
        }

        for (int start = 37; start < 42; start++){
            ItemStack itemStack = inv.getItem(start);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)){
                inv.setItem(start, createGuiItem(ChatColor.DARK_RED + "Disable", Material.RED_STAINED_GLASS));
            }
        }

        inv.setItem(16, createPlayerHead(target, new ArrayList<>(Arrays.asList(ChatColor.GREEN + "You are currently editing",
                ChatColor.GREEN + "this players permissions."))));
        inv.setItem(25, createGuiItem(ChatColor.GRAY + "General Permissions", Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(34, createGuiItem(ChatColor.GREEN + "Container Permissions", Material.CHEST));
        inv.setItem(43, createGuiItem(ChatColor.YELLOW + "Admin Permissions", Material.BEACON));

        inv.setItem(45, createGuiItem(ChatColor.GOLD + "Back", Material.ARROW));
    }

    @Override
    public void onClose() {

    }

    @SuppressWarnings("Duplicates")
    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        int slot = event.getSlot();
        if (slot >= 19 && slot <= 23){
            clickPermOption(getRoute(slot - 19), PermState.ENABLED);
            return;
        } else if (slot >= 28 && slot <= 32){
            clickPermOption(getRoute(slot - 28), PermState.NEUTRAL);
            return;
        } else if (slot >= 37 && slot <= 41){
            clickPermOption(getRoute(slot - 37), PermState.DISABLE);
            return;
        }

        switch (rawItemName){
            case "container permissions":
                new PlayerContainerPermissionMenu(player, group, target).open();
                break;
            case "admin permissions":
                if (group.getOwner() instanceof SubClaim){
                    new SubPlayerAdminPermissions(getPlayer(), group, target).open();
                } else {
                    new AdminPermissionMenu(getPlayer(), group, target).open();
                }
                break;
            case "back":
                GUI menu = null;
                BaseClaim temp = group.getOwner();
                if (temp instanceof SubClaim){
                    menu = new SubClaimMenu(getPlayer(), (SubClaim) temp);
                } else if (temp instanceof Claim){
                    menu = new ClaimMenu(getPlayer(), (Claim) temp);
                }
                new PlayerPermListMenu(group.getOwner(), getPlayer(), menu);
                break;
        }
    }

    private PermissionRoute getRoute(int slot){
        switch (slot){
            case 0:
                return PermissionRoute.BUILD;
            case 1:
                return PermissionRoute.ENTITIES;
            case 2:
                return PermissionRoute.INTERACTIONS;
            case 3:
                return PermissionRoute.EXPLOSIONS;
            case 4:
                return PermissionRoute.TELEPORTATION;
        }
        return null;
    }

    private void clickPermOption(PermissionRoute route, int value) {
        if (route == null)
            return;

        group.setPlayerPermission(target, route, value);
        loadItems();
    }
}
