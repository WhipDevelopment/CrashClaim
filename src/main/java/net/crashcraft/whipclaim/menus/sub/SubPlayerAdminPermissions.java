package net.crashcraft.whipclaim.menus.sub;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.whipclaim.menus.ClaimMenu;
import net.crashcraft.whipclaim.menus.SubClaimMenu;
import net.crashcraft.whipclaim.menus.helpers.MenuListHelper;
import net.crashcraft.whipclaim.menus.helpers.MenuSwitchType;
import net.crashcraft.whipclaim.menus.player.PlayerContainerPermissionMenu;
import net.crashcraft.whipclaim.menus.player.PlayerPermListMenu;
import net.crashcraft.whipclaim.menus.player.PlayerPermissionMenu;
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
import java.util.UUID;

public class SubPlayerAdminPermissions extends MenuListHelper {
    private UUID target;
    private PlayerPermissionSet permissionSet;
    private PermissionGroup group;

    public SubPlayerAdminPermissions(Player player, PermissionGroup group, UUID target) {
        super(player, "Admin Permissions", 54);
        this.target = target;
        this.group = group;
        this.permissionSet = group.getPlayerPermissionSet(target);
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

        menuList.put(PermissionRoute.MODIFY_PERMISSIONS, MenuSwitchType.TRIPLE);
        menuList.put(PermissionRoute.MODIFY_CLAIM, MenuSwitchType.TRIPLE);

        setup(menuList, permissionSet, player.getUniqueId(), group);
    }

    @Override
    public void loadItems(){
        super.loadItems();

        inv.setItem(16, createPlayerHead(target, new ArrayList<>(Arrays.asList(ChatColor.GREEN + "You are currently editing",
                ChatColor.GREEN + "this players permissions."))));

        inv.setItem(25, createGuiItem(ChatColor.GREEN + "General Permissions", Material.CRAFTING_TABLE));
        inv.setItem(34, createGuiItem(ChatColor.GREEN + "Container Permissions", Material.CHEST));
        inv.setItem(43, createGuiItem(ChatColor.GRAY + "Admin Permissions", Material.GRAY_STAINED_GLASS_PANE));

        inv.setItem(45, createGuiItem(ChatColor.GOLD + "Back", Material.ARROW));
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        super.onClick(event, rawItemName);

        switch (rawItemName){
            case "general permissions":
                new PlayerPermissionMenu(getPlayer(), group, target).open();
                break;
            case "container permissions":
                new PlayerContainerPermissionMenu(getPlayer(), group, target).open();
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
}
