package net.crashcraft.whipclaim.menus.player;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.whipclaim.menus.ClaimMenu;
import net.crashcraft.whipclaim.menus.SubClaimMenu;
import net.crashcraft.whipclaim.menus.helpers.MenuListHelper;
import net.crashcraft.whipclaim.menus.helpers.MenuSwitchType;
import net.crashcraft.whipclaim.menus.sub.SubPlayerAdminPermissions;
import net.crashcraft.whipclaim.permissions.PermissionHelper;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;

public class PlayerPermissionMenu extends MenuListHelper {
    private PermissionGroup group;
    private PlayerPermissionSet permissionSet;
    private UUID target;
    private PermissionHelper helper;

    public PlayerPermissionMenu(Player player, PermissionGroup group, UUID target) {
        super(player, "General Permissions", 54);
        this.group = group;
        this.target = target;
        this.permissionSet = group.getPlayerPermissionSet(target);
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
        group.setPlayerPermission(target, route, value);
    }

    @Override
    public void initialize() {
        LinkedHashMap<PermissionRoute, MenuSwitchType> menuList = new LinkedHashMap<>();

        menuList.put(PermissionRoute.BUILD, MenuSwitchType.TRIPLE);
        menuList.put(PermissionRoute.ENTITIES, MenuSwitchType.TRIPLE);
        menuList.put(PermissionRoute.INTERACTIONS, MenuSwitchType.TRIPLE);
        menuList.put(PermissionRoute.TELEPORTATION, MenuSwitchType.TRIPLE);

        setup(menuList, permissionSet, player.getUniqueId(), group);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void loadItems() {
        super.loadItems();

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
        super.onClick(event, rawItemName);

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
                    menu = new ClaimMenu(getPlayer(), (Claim) temp, null);
                }
                new PlayerPermListMenu(group.getOwner(), getPlayer(), menu);
                break;
        }
    }
}
