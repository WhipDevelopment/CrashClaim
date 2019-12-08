package net.crashcraft.whipclaim.menus;

import com.google.common.base.CaseFormat;
import dev.whip.crashutils.menusystem.GUI;
import it.unimi.dsi.fastutil.Hash;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.PermState;
import net.crashcraft.whipclaim.claimobjects.PermissionGroup;
import net.crashcraft.whipclaim.claimobjects.PermissionSet;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import net.crashcraft.whipclaim.permissions.PermissionSetup;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("Duplicates")
public class PlayerContainerPermissionMenu extends GUI {
    private static final int itemOffset = 10;

    private ArrayList<Material> containers;
    private PermissionGroup group;
    private int page;
    private GUI previousMenu;
    private UUID target;

    private PermissionSet set;

    private HashMap<Integer, Material> trackingMap;

    public PlayerContainerPermissionMenu(Player player, PermissionGroup group, UUID target, GUI previousMenu) {
        super(player, "Player Container Permissions", 54);
        this.group = group;
        this.trackingMap = new HashMap<>();
        this.previousMenu = previousMenu;
        this.target = target;
        this.set = group.getPlayerPermissionSet(target);
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

        int offset = (5 * page);

        for (int x = 0; x < 5; x++){
            if (offset + x > containers.size() - 1)
                break;

            Material material = containers.get(x + offset);

            inv.setItem(itemOffset + x, createGuiItem(
                    CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, material.name()), material
            ));

            trackingMap.put(x, material);

            switch (PermissionRoute.CONTAINERS.getPerm(set, material)){
                case 0:
                    inv.setItem(itemOffset + x + 27, createGuiItem(ChatColor.RED + "Disabled", Material.RED_CONCRETE));
                    break;
                case 1:
                    inv.setItem(itemOffset + x + 9, createGuiItem(  ChatColor.GREEN + "Enabled", Material.GREEN_CONCRETE));
                    break;
                case 2:
                    inv.setItem(itemOffset + x + 18, createGuiItem(ChatColor.GRAY + "Neutral", Material.GRAY_CONCRETE));
                    break;
            }

            ItemStack itemStack = inv.getItem(itemOffset + x + 9);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)){
                inv.setItem(itemOffset + x + 9, createGuiItem(ChatColor.GREEN + "Enable", Material.GREEN_STAINED_GLASS));
            }

            itemStack = inv.getItem(itemOffset + x + 18);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)){
                inv.setItem(itemOffset + x + 18, createGuiItem(ChatColor.GREEN + "Neutral", Material.GRAY_STAINED_GLASS));
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

            inv.setItem(48, createGuiItem(ChatColor.GOLD + Integer.toString(page) + " / " + (Math.floor(containers.size() / 5)), Material.PAPER));

            if ((offset + 5) < containers.size() - 1){
                inv.setItem(49, createGuiItem(ChatColor.GOLD + "Next Page", Material.ARROW));
            }
        }

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
            case "admin permissions":

                break;
            case "previous page":
                page--;
                loadItems();
                break;
            case "next page":
                page++;
                loadItems();
                break;
            case "back":
                previousMenu.initialize();
                previousMenu.open();
                break;
        }
    }


    private void clickPermOption(Material material, int value) {
        if (material == null)
            return;

        group.setContainerPlayerPermission(target, value, material);
        loadItems();
    }
}
