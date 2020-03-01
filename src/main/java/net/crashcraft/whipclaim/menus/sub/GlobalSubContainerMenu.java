package net.crashcraft.whipclaim.menus.sub;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.menus.SubClaimMenu;
import net.crashcraft.whipclaim.permissions.PermissionHelper;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@SuppressWarnings("Duplicates")
public class GlobalSubContainerMenu extends GUI {
    private static final int itemOffset = 10;

    private ArrayList<Material> containers;
    private PermissionGroup group;
    private int page;

    private GlobalPermissionSet set;

    private HashMap<Integer, Material> trackingMap;
    private PermissionHelper helper;

    public GlobalSubContainerMenu(Player player, PermissionGroup group) {
        super(player, "Container Permissions", 54);
        this.group = group;
        this.trackingMap = new HashMap<>();
        this.set = group.getGlobalPermissionSet();
        this.helper = PermissionHelper.getPermissionHelper();
        setupGUI();
    }

    @Override
    public void initialize() {
        containers = WhipClaim.getPlugin().getDataManager().getPermissionSetup().getTrackedContainers();

        page = 0;
    }

    @Override
    public void loadItems() {
        inv.clear();
        trackingMap.clear();

        SubClaim claim = (SubClaim) group.getOwner();

        int offset = (5 * page);

        for (int x = 0; x < 5; x++){
            if (offset + x > containers.size() - 1)
                break;

            Material material = containers.get(x + offset);

            inv.setItem(itemOffset + x, createGuiItem(
                    ChatColor.GOLD + WhipClaim.getPlugin().getMaterialName().getMaterialName(material), material
            ));

            trackingMap.put(x, material);

            boolean allow = helper.hasPermission(group.getOwner(), player.getUniqueId(), material);

            switch (PermissionRoute.CONTAINERS.getPerm(set, material)){
                case 0:
                    inv.setItem(x + itemOffset + 27, createGuiItem(ChatColor.RED + "Disabled",
                            allow ? Material.RED_CONCRETE : Material.GRAY_CONCRETE));

                    inv.setItem(x + itemOffset + 9, createGuiItem(ChatColor.DARK_GREEN + "Enable",
                            allow ? Material.GREEN_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE));
                    inv.setItem(x + itemOffset + 18, createGuiItem(ChatColor.DARK_GRAY + "Neutral",
                            allow ?  Material.GRAY_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE));
                    break;
                case 1:
                    inv.setItem(x + itemOffset + 9, createGuiItem(ChatColor.GREEN + "Enabled",
                            allow ? Material.GREEN_CONCRETE : Material.GRAY_CONCRETE));

                    inv.setItem(x + itemOffset + 18, createGuiItem(ChatColor.DARK_GRAY + "Neutral",
                            allow ? Material.GRAY_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE));
                    inv.setItem(x + itemOffset + 27, createGuiItem(ChatColor.DARK_RED + "Disable",
                            allow ? Material.RED_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE));
                    break;
                case 2:
                    inv.setItem(x + itemOffset + 18, createGuiItem(ChatColor.GRAY + "Neutral",
                            Material.GRAY_CONCRETE));

                    inv.setItem(x + itemOffset + 9, createGuiItem(ChatColor.DARK_GREEN + "Enable",
                            allow ? Material.GREEN_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE));
                    inv.setItem(x + itemOffset + 27, createGuiItem(ChatColor.DARK_RED + "Disable",
                            allow ? Material.RED_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE));
                    break;
            }
        }

        if (containers.size() > 5) {
            if ((offset - 5) >= 0){
                inv.setItem(47, createGuiItem(ChatColor.GOLD + "Previous Page", Material.ARROW));
            }

            inv.setItem(48, createGuiItem(ChatColor.GOLD + Integer.toString(page + 1) + " / " + (int) (Math.floor(containers.size() / 5) + 1), Material.PAPER));

            if ((offset + 5) < containers.size() - 1){
                inv.setItem(49, createGuiItem(ChatColor.GOLD + "Next Page", Material.ARROW));
            }
        }

        inv.setItem(16, createGuiItem(ChatColor.GOLD + claim.getName(),
                new ArrayList<>(Arrays.asList(
                        ChatColor.GREEN + "NW Corner: " + ChatColor.YELLOW + claim.getMinX() +
                                ", " + claim.getMinZ(),
                        ChatColor.GREEN + "SE Corner: " + ChatColor.YELLOW + claim.getMaxX() +
                                ", " + claim.getMaxZ())),
                Material.PAPER));

        inv.setItem(25, createGuiItem(ChatColor.GREEN + "General Permissions", Material.CRAFTING_TABLE));
        inv.setItem(34, createGuiItem(ChatColor.GRAY + "Container Permissions", Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(43, createGuiItem(ChatColor.DARK_GRAY + "Advanced Permissions", Material.BLACK_STAINED_GLASS_PANE));

        inv.setItem(45, createGuiItem(ChatColor.GOLD + "Back", Material.ARROW));
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        int slot = event.getSlot();
        if (slot >= 19 && slot <= 23){
            clickPermOption(trackingMap.get(slot - itemOffset - 9), PermState.ENABLED);
            return;
        } else if (slot >= 28 && slot <= 32){
            clickPermOption(trackingMap.get(slot - itemOffset - 18), PermState.NEUTRAL);
            return;
        } else if (slot >= 37 && slot <= 41){
            clickPermOption(trackingMap.get(slot - itemOffset - 27), PermState.DISABLE);
            return;
        }

        switch (rawItemName){
            case "previous page":
                page--;
                loadItems();
                break;
            case "next page":
                page++;
                loadItems();
                break;
            case "general permissions":
                new GlobalSubClaimPermissionsMenu(getPlayer(), group).open();
                break;
            case "back":
                new SubClaimMenu(getPlayer(), (SubClaim) group.getOwner()).open();
                break;
        }
    }


    private void clickPermOption(Material material, int value) {
        if (material == null)
            return;

        if (!helper.hasPermission(group.getOwner(), player.getUniqueId(), material)){
            return;
        }

        if (!helper.hasPermission(group.getOwner(), player.getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)){
            player.sendMessage(ChatColor.RED + "You no longer have sufficient permissions to continue");
            forceClose();
            return;
        }

        group.setContainerPermission(value, material);
        loadItems();
    }
}