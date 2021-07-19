package net.crashcraft.crashclaim.menus.permissions;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.crashclaim.claimobjects.permission.PermissionSet;
import net.crashcraft.crashclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.menus.helpers.MenuListHelper;
import net.crashcraft.crashclaim.menus.helpers.MenuSwitchType;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.LinkedHashMap;
import java.util.UUID;

public class AdvancedPermissionMenu extends MenuListHelper {
    private final BaseClaim claim;
    private final PermissionSet permissionSet;
    private final boolean isPlayerPermission;
    private final UUID uuid;

    private SUBMENU submenu;

    public AdvancedPermissionMenu(Player player, BaseClaim claim, UUID uuid, GUI prevMenu) {
        super(player, "Claim Permissions", 54, prevMenu);

        this.uuid = uuid;
        this.isPlayerPermission = uuid != null;
        this.claim = claim;

        if (isPlayerPermission){
            permissionSet = claim.getPerms().getPlayerPermissionSet(uuid);
        } else {
            permissionSet = claim.getPerms().getGlobalPermissionSet();
        }

        render(SUBMENU.GENERAL);
    }

    private void render(SUBMENU menu){
        this.submenu = menu;

        inv.clear();

        switch (submenu) {
            case GENERAL:
                LinkedHashMap<PermissionRoute, MenuSwitchType> menuList = new LinkedHashMap<>();

                MenuSwitchType type = isPlayerPermission || claim instanceof SubClaim ? MenuSwitchType.TRIPLE : MenuSwitchType.DOUBLE;

                menuList.put(PermissionRoute.BUILD, type);
                menuList.put(PermissionRoute.INTERACTIONS, type);
                menuList.put(PermissionRoute.ENTITIES, type);
                if (!isPlayerPermission) {
                    menuList.put(PermissionRoute.EXPLOSIONS, type);
                }
                menuList.put(PermissionRoute.TELEPORTATION, type);
                if (isPlayerPermission){
                    menuList.put(PermissionRoute.VIEW_SUB_CLAIMS, type);
                }

                setup(menuList, 5, permissionSet, player.getUniqueId(), claim.getPerms());
                return;
            case CONTAINERS:
                setupContainerList(permissionSet, player.getUniqueId(), claim.getPerms(), 5, 0);
                return;
            case ADMIN:
                LinkedHashMap<PermissionRoute, MenuSwitchType> list = new LinkedHashMap<>();

                UUID owner = null;
                if (claim instanceof SubClaim){
                    owner = ((SubClaim) claim).getParent().getOwner();
                } else if (claim instanceof Claim){
                    owner = ((Claim) claim).getOwner();
                }

                if (player.getUniqueId().equals(owner)) {
                    list.put(PermissionRoute.MODIFY_PERMISSIONS, MenuSwitchType.DOUBLE);
                    list.put(PermissionRoute.MODIFY_CLAIM, MenuSwitchType.DOUBLE);
                } else {
                    list.put(PermissionRoute.MODIFY_PERMISSIONS, MenuSwitchType.DOUBLE_DISABLED);
                    list.put(PermissionRoute.MODIFY_CLAIM, MenuSwitchType.DOUBLE_DISABLED);
                }

                setup(list, 5, permissionSet, player.getUniqueId(), claim.getPerms());
                return;
            case MISC:
                LinkedHashMap<PermissionRoute, MenuSwitchType> menuItemlist = new LinkedHashMap<>();

                menuItemlist.put(PermissionRoute.PISTONS, MenuSwitchType.DOUBLE);
                menuItemlist.put(PermissionRoute.FLUIDS, MenuSwitchType.DOUBLE);
                menuItemlist.put(null, null);
                menuItemlist.put(PermissionRoute.VIEW_SUB_CLAIMS, MenuSwitchType.DOUBLE);

                setup(menuItemlist, 5, permissionSet, player.getUniqueId(), claim.getPerms());
                return;
        }

        loadItems();
    }

    private void drawSidebar(){
        switch (submenu) {
            case GENERAL:
                inv.setItem(24, Localization.MENU__PERMISSION_OPTION__GENERAL_GLOWING.getItem(player));
                inv.setItem(33, Localization.MENU__PERMISSION_OPTION__CONTAINERS.getItem(player));

                if (isPlayerPermission) {
                    inv.setItem(42, Localization.MENU__PERMISSION_OPTION__ADMIN.getItem(player));
                } else {
                    inv.setItem(42, Localization.MENU__PERMISSION_OPTION__MISC.getItem(player));
                }
                return;
            case CONTAINERS:
                inv.setItem(24, Localization.MENU__PERMISSION_OPTION__GENERAL.getItem(player));
                inv.setItem(33, Localization.MENU__PERMISSION_OPTION__CONTAINERS_GLOWING.getItem(player));

                if (isPlayerPermission) {
                    inv.setItem(42, Localization.MENU__PERMISSION_OPTION__ADMIN.getItem(player));
                } else {
                    inv.setItem(42, Localization.MENU__PERMISSION_OPTION__MISC.getItem(player));
                }
                break;
            case ADMIN:
                inv.setItem(24, Localization.MENU__PERMISSION_OPTION__GENERAL.getItem(player));
                inv.setItem(33, Localization.MENU__PERMISSION_OPTION__CONTAINERS.getItem(player));

                inv.setItem(42, Localization.MENU__PERMISSION_OPTION__ADMIN_GLOWING.getItem(player));
                break;
            case MISC:
                inv.setItem(24, Localization.MENU__PERMISSION_OPTION__GENERAL.getItem(player));
                inv.setItem(33, Localization.MENU__PERMISSION_OPTION__CONTAINERS.getItem(player));

                inv.setItem(42, Localization.MENU__PERMISSION_OPTION__MISC_GLOWING.getItem(player));
                break;
        }
    }

    @Override
    public void loadItems() {
        super.loadItems();

        drawSidebar();

        inv.setItem(17, descItem);

        inv.setItem(35, Localization.MENU__PERMISSION_OPTION__SIMPLE.getItem(player));
        inv.setItem(44, Localization.MENU__PERMISSION_OPTION__ADVANCED_GLOWING.getItem(player));
    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        super.onClick(event, rawItemName);

        switch (event.getSlot()){
            case 35:
                new SimplePermissionMenu(player, claim, uuid, prevMenu).open();
                break;
            case 24:
                render(SUBMENU.GENERAL);
                break;
            case 33:
                render(SUBMENU.CONTAINERS);
                break;
            case 42:
                if (isPlayerPermission){
                    render(SUBMENU.ADMIN);
                } else {
                    render(SUBMENU.MISC);
                }
                break;
        }
    }

    @Override
    public void invalidPermissions() {

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

    private enum SUBMENU {
        GENERAL,
        CONTAINERS,
        ADMIN,
        MISC
    }
}
