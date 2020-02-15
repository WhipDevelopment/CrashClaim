package net.crashcraft.whipclaim.menus.global;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.menus.ClaimMenu;
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

public class GlobalContainerMenu extends GUI {
    private static final int itemOffset = 10;

    private ArrayList<Material> containers;
    private PermissionGroup group;
    private int page;

    private GlobalPermissionSet set;
    private HashMap<Integer, Material> trackingMap;
    private PermissionHelper helper;

    public GlobalContainerMenu(Player player, PermissionGroup group) {
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

        BaseClaim claim = group.getOwner();

        int offset = (5 * page);

        for (int x = 0; x < 5; x++){
            if (offset + x > containers.size() - 1)
                break;

            Material material = containers.get(x + offset);

            inv.setItem(itemOffset + x, createGuiItem(
                    ChatColor.GOLD + WhipClaim.getPlugin().getMaterialName().getMaterialName(material), material
            ));

            trackingMap.put(x, material);

            switch (PermissionRoute.CONTAINERS.getPerm(set, material)){
                case 0:
                    inv.setItem(itemOffset + x + 27, createGuiItem(ChatColor.RED + "Disabled", Material.RED_CONCRETE));
                    break;
                case 1:
                    inv.setItem(itemOffset + x + 18, createGuiItem(  ChatColor.GREEN + "Enabled", Material.GREEN_CONCRETE));
                    break;
            }


            ItemStack itemStack = inv.getItem(itemOffset + x + 18);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)){
                inv.setItem(itemOffset + x + 18, createGuiItem(ChatColor.GREEN + "Enable", Material.GREEN_STAINED_GLASS));
            }

            itemStack = inv.getItem(itemOffset + x + 27);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)){
                inv.setItem(itemOffset + x + 27, createGuiItem(ChatColor.GREEN + "Disable", Material.RED_STAINED_GLASS));
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
                Material.OAK_FENCE));

        inv.setItem(25, createGuiItem(ChatColor.GREEN + "General Permissions", Material.CRAFTING_TABLE));
        inv.setItem(34, createGuiItem(ChatColor.GRAY + "Container Permissions", Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(43, createGuiItem(ChatColor.YELLOW + "Advanced Permissions", Material.NETHER_STAR));

        inv.setItem(45, createGuiItem(ChatColor.GOLD + "Back", Material.ARROW));
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        int slot = event.getSlot();
        if (slot >= 28 && slot <= 32){
            clickPermOption(trackingMap.get(slot - itemOffset - 18), PermState.ENABLED);
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
                new GlobalPermissionMenu(getPlayer(), (Claim) group.getOwner()).open();
                break;
            case "advanced permissions":
                new GlobalAdvancedPermissions(getPlayer(), group).open();
                break;
            case "back":
                new ClaimMenu(getPlayer(), (Claim) group.getOwner()).open();
                break;
        }
    }

    private void clickPermOption(Material material, int value) {
        if (material == null)
            return;

        if (!helper.hasPermission(group.getOwner(), player.getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)){
            player.sendMessage(ChatColor.RED + "You no longer have sufficient permissions to continue");
            forceClose();
            return;
        }

        group.setContainerPermission(value, material);
        loadItems();
    }
}