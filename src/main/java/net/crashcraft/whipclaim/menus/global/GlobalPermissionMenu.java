package net.crashcraft.whipclaim.menus.global;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.menus.ClaimMenu;
import net.crashcraft.whipclaim.menus.helpers.MenuListHelper;
import net.crashcraft.whipclaim.menus.helpers.MenuSwitchType;
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

public class GlobalPermissionMenu extends MenuListHelper {
    private Claim claim;
    private GlobalPermissionSet permissionSet;

    public GlobalPermissionMenu(Player player, Claim claim) {
        super(player, "General Permissions", 54);
        this.claim = claim;
        this.permissionSet = claim.getPerms().getGlobalPermissionSet();
        setupGUI();
    }

    @Override
    public void invalidPermissions() {
        player.sendMessage(ChatColor.RED + "You no longer have sufficient permissions to continue");
        forceClose();
    }

    @Override
    public void setPermission(PermissionRoute route, int value) {
        claim.getPerms().setPermission(route, value);
    }

    @Override
    public void initialize() {
        LinkedHashMap<PermissionRoute, MenuSwitchType> menuList = new LinkedHashMap<>();

        menuList.put(PermissionRoute.BUILD, MenuSwitchType.DOUBLE);
        menuList.put(PermissionRoute.ENTITIES, MenuSwitchType.DOUBLE);
        menuList.put(PermissionRoute.INTERACTIONS, MenuSwitchType.DOUBLE);
        menuList.put(PermissionRoute.EXPLOSIONS, MenuSwitchType.DOUBLE);
        menuList.put(PermissionRoute.TELEPORTATION, MenuSwitchType.DOUBLE);

        setup(menuList, permissionSet, player.getUniqueId(), claim.getPerms());
    }

    @Override
    public void loadItems() {
        super.loadItems();

        inv.setItem(16, createGuiItem(ChatColor.GOLD + claim.getName(),
                new ArrayList<>(Arrays.asList(
                        ChatColor.GREEN + "NW Corner: " + ChatColor.YELLOW + claim.getMinX() +
                                ", " + claim.getMinZ(),
                        ChatColor.GREEN + "SE Corner: " + ChatColor.YELLOW + claim.getMaxX() +
                                ", " + claim.getMaxZ())),
                Material.OAK_FENCE));

        inv.setItem(25, createGuiItem(ChatColor.GRAY + "General Permissions", Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(34, createGuiItem(ChatColor.GREEN + "Container Permissions", Material.CHEST));
        inv.setItem(43, createGuiItem(ChatColor.YELLOW + "Advanced Permissions", Material.NETHER_STAR));

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
                new GlobalContainerMenu(player, claim.getPerms()).open();
                break;
            case "advanced permissions":
                new GlobalAdvancedPermissions(getPlayer(), claim.getPerms()).open();
                break;
            case "back":
                new ClaimMenu(getPlayer(), claim, null).open();
                break;
        }
    }
}
