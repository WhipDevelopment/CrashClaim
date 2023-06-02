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
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.LinkedHashMap;
import java.util.UUID;

public class SimplePermissionMenu extends MenuListHelper {
    private final PermissionSet permissionSet;
    private final BaseClaim claim;
    private final UUID uuid;

    public SimplePermissionMenu(Player player, BaseClaim claim, UUID uuid, GUI prevMenu) {
        super(player,
                BaseComponent.toLegacyText(claim instanceof SubClaim ?
                        Localization.MENU__SUB_CLAIM_SIMPLE_PERMISSIONS__TITLE.getMessage(null) : Localization.MENU__SIMPLE_PERMISSIONS__TITLE.getMessage(null)
                ), 54, prevMenu);

        this.uuid = uuid;
        this.claim = claim;
        boolean isPlayerPermission = uuid != null;

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
            UUID owner = null;
            if (claim instanceof SubClaim){
                owner = ((SubClaim) claim).getParent().getOwner();
            } else if (claim instanceof Claim){
                owner = ((Claim) claim).getOwner();
            }

            if (claim instanceof SubClaim){
                if (player.getUniqueId().equals(owner)) {
                    menuList.put(PermissionRoute.SUBCLAIM_ADMIN, MenuSwitchType.TRIPLE);
                } else {
                    menuList.put(PermissionRoute.SUBCLAIM_ADMIN, MenuSwitchType.TRIPLE_DISABLED);
                }
            } else {
                if (player.getUniqueId().equals(owner)) {
                    menuList.put(PermissionRoute.ADMIN, MenuSwitchType.DOUBLE);
                } else {
                    menuList.put(PermissionRoute.ADMIN, MenuSwitchType.DOUBLE_DISABLED);
                }
            }
        } else {
            menuList.put(PermissionRoute.EXPLOSIONS, type);

            if (claim instanceof Claim){
                menuList.put(PermissionRoute.MISC, MenuSwitchType.DOUBLE);
            } else if (claim instanceof SubClaim){
                menuList.put(PermissionRoute.TELEPORTATION, MenuSwitchType.TRIPLE);
            }
        }

        setup(menuList, 6, permissionSet, player.getUniqueId(), claim.getPerms(), uuid);
    }

    @Override
    public void loadItems() {
        super.loadItems();

        inv.setItem(17, descItem);

        inv.setItem(35, Localization.MENU__PERMISSION_OPTION__SIMPLE_GLOWING.getItem(player));
        inv.setItem(44, Localization.MENU__PERMISSION_OPTION__ADVANCED.getItem(player));
    }

    @Override
    public void invalidPermissions() {
        player.spigot().sendMessage(Localization.MENU__ADVANCED_PERMISSIONS__NO_PERMISSION.getMessage(player));
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
        claim.setToSave(true);
    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        super.onClick(event, rawItemName);

        if (event.getSlot() == 44) {
            new AdvancedPermissionMenu(player, claim, uuid, prevMenu).open();
        }
    }
}
