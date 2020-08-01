package net.crashcraft.whipclaim.menus.permissions;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.PermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.whipclaim.menus.helpers.MenuListHelper;
import net.crashcraft.whipclaim.menus.helpers.MenuSwitchType;
import net.crashcraft.whipclaim.menus.helpers.StaticItemLookup;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.LinkedHashMap;
import java.util.UUID;

public class SimplePermissionMenu extends MenuListHelper {
    private final PermissionSet permissionSet;
    private final boolean isPlayerPermission;
    private final BaseClaim claim;
    private final UUID uuid;

    public SimplePermissionMenu(Player player, BaseClaim claim, UUID uuid, GUI prevMenu) {
        super(player, "Claim Permissions", 54, prevMenu);

        this.uuid = uuid;
        this.claim = claim;
        this.isPlayerPermission = uuid != null;

        if (isPlayerPermission){
            permissionSet = claim.getPerms().getPlayerPermissionSet(uuid);
        } else {
            permissionSet = claim.getPerms().getGlobalPermissionSet();
        }

        LinkedHashMap<PermissionRoute, MenuSwitchType> menuList = new LinkedHashMap<>();

        MenuSwitchType type = isPlayerPermission || claim instanceof SubClaim ? MenuSwitchType.TRIPLE : MenuSwitchType.DOUBLE;

        menuList.put(PermissionRoute.BUILD, type);
        menuList.put(PermissionRoute.CONTAINERS, type);
        menuList.put(PermissionRoute.ENTITIES, type);
        menuList.put(PermissionRoute.INTERACTIONS, type);

        if (isPlayerPermission){
            menuList.put(PermissionRoute.TELEPORTATION, type);
            menuList.put(PermissionRoute.ADMIN, MenuSwitchType.DOUBLE);
        } else {
            menuList.put(PermissionRoute.EXPLOSIONS, type);
            menuList.put(PermissionRoute.TELEPORTATION, type);
        }

        if (claim instanceof Claim){
            menuList.put(PermissionRoute.MISC, MenuSwitchType.DOUBLE);
        }

        setup(menuList, 6, permissionSet, player.getUniqueId(), claim.getPerms());
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void loadItems() {
        super.loadItems();

        inv.setItem(17, descItem);

        inv.setItem(35, StaticItemLookup.SIMPLE_MENU_ITEM_GLOWING);
        inv.setItem(44, StaticItemLookup.ADVANCED_MENU_ITEM);
    }

    @Override
    public void invalidPermissions() {
        player.sendMessage(ChatColor.RED + "You no longer have sufficient permissions to continue");
        forceClose();
    }

    @Override
    public void setPermission(PermissionRoute route, int value) {
        if (permissionSet instanceof PlayerPermissionSet) {
            route.setPerm((PlayerPermissionSet) permissionSet, value);
        } else if (permissionSet instanceof GlobalPermissionSet){
            route.setPerm((GlobalPermissionSet) permissionSet, value);
        }
    }

    @Override
    public void setContainerPermission(int value, Material material) {
        if (permissionSet instanceof PlayerPermissionSet) {
            PermissionRoute.CONTAINERS.setPerm((PlayerPermissionSet) permissionSet, value, material);
        } else if (permissionSet instanceof GlobalPermissionSet){
            PermissionRoute.CONTAINERS.setPerm((GlobalPermissionSet) permissionSet, value, material);
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        super.onClick(event, rawItemName);

        switch (event.getSlot()){
            case 44:
                new AdvancedPermissionMenu(player, claim, uuid, prevMenu).open();
                break;
        }
    }
}
