package net.crashcraft.whipclaim.menus.global;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.menus.ClaimMenu;
import net.crashcraft.whipclaim.menus.helpers.MenuListHelper;
import net.crashcraft.whipclaim.menus.helpers.MenuSwitchType;
import net.crashcraft.whipclaim.menus.helpers.StaticItemLookup;
import net.crashcraft.whipclaim.permissions.PermissionHelper;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class GlobalAdvancedPermissions extends MenuListHelper {
    private GlobalPermissionSet permissionSet;
    private PermissionGroup group;
    private PermissionHelper helper;

    public GlobalAdvancedPermissions(Player player, PermissionGroup group) {
        super(player, "Advanced Permissions", 54);
        this.group = group;
        this.permissionSet = group.getGlobalPermissionSet();
        this.helper = PermissionHelper.getPermissionHelper();
        setupGUI();
    }

    @Override
    public void invalidPermissions() {
        player.sendMessage(ChatColor.RED + "You no longer have sufficient permissions to continue");
        forceClose();
    }

    @Override
    public void setPermission(PermissionRoute route, int value) {
        group.setPermission(route, value);
    }

    @Override
    public void initialize() {
        LinkedHashMap<PermissionRoute, MenuSwitchType> menuList = new LinkedHashMap<>();

        menuList.put(PermissionRoute.PISTONS, MenuSwitchType.DOUBLE);
        menuList.put(PermissionRoute.FLUIDS, MenuSwitchType.DOUBLE);
        menuList.put(PermissionRoute.VIEW_SUB_CLAIMS, MenuSwitchType.DOUBLE);

        setup(menuList, permissionSet, player.getUniqueId(), group);
    }

    @Override
    public void loadItems() {
        super.loadItems();

        BaseClaim claim = group.getOwner();

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
        super.onClick(event, rawItemName);

        switch (rawItemName) {
            case "general permissions":
                new GlobalPermissionMenu(getPlayer(), (Claim) group.getOwner()).open();
                break;
            case "container permissions":
                new GlobalContainerMenu(getPlayer(), group).open();
                break;
            case "back":
                new ClaimMenu(getPlayer(), (Claim) group.getOwner()).open();
                break;
        }
    }
}