package net.crashcraft.whipclaim.menus.sub;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.menus.SubClaimMenu;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("Duplicates")
public class SubClaimAdvancedPermissions  extends GUI {
    private GlobalPermissionSet permissionSet;
    private PermissionGroup group;

    public SubClaimAdvancedPermissions(Player player, PermissionGroup group) {
        super(player, "Advanced Permissions", 54);
        this.group = group;
        this.permissionSet = group.getPermissionSet();
        setupGUI();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void loadItems() {
        inv.clear();
        BaseClaim claim = group.getOwner();

        //TODO define everything with an accurate descitption
        inv.setItem(11, createGuiItem(ChatColor.GOLD + "Allow Pistons", Material.CRAFTING_TABLE));
        inv.setItem(12, createGuiItem(ChatColor.GOLD + "Allow Fluids", Material.OAK_FENCE_GATE));
        inv.setItem(13, createGuiItem(ChatColor.GOLD + "View Sub Claims", Material.SEA_LANTERN));

        switch (PermissionRoute.FLUIDS.getPerm(permissionSet)) {
            case 1:
                inv.setItem(29, createGuiItem(ChatColor.GREEN + "Enabled", Material.GREEN_CONCRETE));
                break;
            case 0:
                inv.setItem(38, createGuiItem(ChatColor.RED + "Disabled", Material.RED_CONCRETE));
                break;
        }

        switch (PermissionRoute.PISTONS.getPerm(permissionSet)) {
            case 1:
                inv.setItem(30, createGuiItem(ChatColor.GREEN + "Enabled", Material.GREEN_CONCRETE));
                break;
            case 0:
                inv.setItem(39, createGuiItem(ChatColor.RED + "Disabled", Material.RED_CONCRETE));
                break;
        }

        switch (PermissionRoute.VIEW_SUB_CLAIMS.getPerm(permissionSet)) {
            case 1:
                inv.setItem(31, createGuiItem(ChatColor.GREEN + "Enabled", Material.GREEN_CONCRETE));
                break;
            case 0:
                inv.setItem(40, createGuiItem(ChatColor.RED + "Disabled", Material.RED_CONCRETE));
                break;
        }

        for (int start = 20; start < 23; start++){
            ItemStack itemStack = inv.getItem(start);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)){
                inv.setItem(start, createGuiItem(ChatColor.DARK_GREEN + "Enable", Material.GREEN_STAINED_GLASS));
            }
        }

        for (int start = 29; start < 32; start++){
            ItemStack itemStack = inv.getItem(start);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)){
                inv.setItem(start, createGuiItem(ChatColor.DARK_GRAY + "Neutral", Material.GRAY_STAINED_GLASS));
            }
        }

        for (int start = 38; start < 41; start++){
            ItemStack itemStack = inv.getItem(start);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)){
                inv.setItem(start, createGuiItem(ChatColor.DARK_RED + "Disable", Material.RED_STAINED_GLASS));
            }
        }

        inv.setItem(16, createGuiItem(ChatColor.GOLD + claim.getName(),
                new ArrayList<>(Arrays.asList(
                        ChatColor.GREEN + "NW Corner: " + ChatColor.YELLOW + claim.getMinX() +
                                ", " + claim.getMinZ(),
                        ChatColor.GREEN + "SE Corner: " + ChatColor.YELLOW + claim.getMaxX() +
                                ", " + claim.getMaxZ())),
                Material.OAK_FENCE));

        inv.setItem(25, createGuiItem(ChatColor.GREEN + "General Permissions", Material.CRAFTING_TABLE));
        inv.setItem(34, createGuiItem(ChatColor.GREEN + "Container Permissions", Material.CHEST));
        inv.setItem(43, createGuiItem(ChatColor.GRAY + "Advanced Permissions", Material.GRAY_STAINED_GLASS_PANE));

        inv.setItem(45, createGuiItem(ChatColor.GOLD + "Back", Material.ARROW));
    }

    @Override
    public void onClose() {

    }

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

        switch (rawItemName) {
            case "general permissions":
                new GlobalSubClaimPermissionsMenu(getPlayer(), group).open();
                break;
            case "container permissions":
                new GlobalSubContainerMenu(getPlayer(), group).open();
                break;
            case "back":
                new SubClaimMenu(getPlayer(), (SubClaim) group.getOwner()).open();
                break;
        }
    }

    private PermissionRoute getRoute(int slot) {
        switch (slot) {
            case 1:
                return PermissionRoute.FLUIDS;
            case 2:
                return PermissionRoute.PISTONS;
            case 3:
                return PermissionRoute.VIEW_SUB_CLAIMS;
        }
        return null;
    }

    private void clickPermOption(PermissionRoute route, int value) {
        if (route == null)
            return;

        group.setPermission(route, value);
        loadItems();
    }
}
