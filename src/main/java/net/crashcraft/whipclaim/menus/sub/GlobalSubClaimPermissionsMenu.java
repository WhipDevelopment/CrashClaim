package net.crashcraft.whipclaim.menus.sub;

import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.menus.SubClaimMenu;
import net.crashcraft.whipclaim.menus.helpers.MenuListHelper;
import net.crashcraft.whipclaim.menus.helpers.MenuSwitchType;
import net.crashcraft.whipclaim.permissions.PermissionHelper;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class GlobalSubClaimPermissionsMenu extends MenuListHelper {
    private PermissionGroup group;
    private GlobalPermissionSet permissionSet;

    public GlobalSubClaimPermissionsMenu(Player player, PermissionGroup group) {
        super(player, "General Permissions", 54);
        this.group = group;
        this.permissionSet = group.getGlobalPermissionSet();
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

        menuList.put(PermissionRoute.BUILD, MenuSwitchType.TRIPLE);
        menuList.put(PermissionRoute.ENTITIES, MenuSwitchType.TRIPLE);
        menuList.put(PermissionRoute.INTERACTIONS, MenuSwitchType.TRIPLE);
        menuList.put(PermissionRoute.EXPLOSIONS, MenuSwitchType.TRIPLE);
        menuList.put(PermissionRoute.TELEPORTATION, MenuSwitchType.TRIPLE);

        setup(menuList, permissionSet, player.getUniqueId(), group);
    }

    @Override
    public void loadItems() {
        super.loadItems();

        SubClaim claim = (SubClaim) group.getOwner();

        inv.setItem(16, createGuiItem(ChatColor.GOLD + claim.getName(),
                new ArrayList<>(Arrays.asList(
                        ChatColor.GREEN + "NW Corner: " + ChatColor.YELLOW + claim.getMinX() +
                                ", " + claim.getMinZ(),
                        ChatColor.GREEN + "SE Corner: " + ChatColor.YELLOW + claim.getMaxX() +
                                ", " + claim.getMaxZ())),
                Material.PAPER));

        inv.setItem(25, createGuiItem(ChatColor.GRAY + "General Permissions", Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(34, createGuiItem(ChatColor.GREEN + "Container Permissions", Material.CHEST));
        inv.setItem(43, createGuiItem(ChatColor.DARK_GRAY + "Advanced Permissions", Material.BLACK_STAINED_GLASS_PANE));

        inv.setItem(45, createGuiItem(ChatColor.GOLD + "Back", Material.ARROW));
    }

    @Override
    public void onClose() {

    }

    @SuppressWarnings("Duplicates")
    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        super.onClick(event, rawItemName);

        switch (rawItemName){
            case "container permissions":
                new GlobalSubContainerMenu(player, group).open();
                break;
            case "back":
                new SubClaimMenu(getPlayer(), (SubClaim) group.getOwner()).open();
                break;
        }
    }
}
