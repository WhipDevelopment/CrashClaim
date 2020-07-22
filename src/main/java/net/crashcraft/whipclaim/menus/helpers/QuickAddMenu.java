package net.crashcraft.whipclaim.menus.helpers;

import dev.whip.crashutils.menusystem.GUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class QuickAddMenu extends GUI {
    private GUI previousMenu;

    public QuickAddMenu(Player player, boolean isPlayer) {
        super(player, "Quick Add Permissions", 54);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void loadItems() {
        inv.setItem(10, createGuiItem(ChatColor.GRAY + "General Permissions", Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(11, createGuiItem(ChatColor.GREEN + "Container Permissions", Material.CHEST));
        inv.setItem(43, createGuiItem(ChatColor.YELLOW + "Advanced Permissions", Material.NETHER_STAR));
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {

    }
}
