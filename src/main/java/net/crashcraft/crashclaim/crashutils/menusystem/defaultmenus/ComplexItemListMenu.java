package net.crashcraft.crashclaim.crashutils.menusystem.defaultmenus;

import net.crashcraft.crashclaim.crashutils.menusystem.GUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;


//https://github.com/CrashCraftNetwork/CrashUtils/blob/master/src/main/java/dev/whip/crashutils/menusystem/defaultmenus/ComplexItemListMenu.java
public class ComplexItemListMenu extends GUI {
    private int page = 1;
    private GUI previousMenu;
    private ChatColor color;

    private ArrayList<String> itemNames = new ArrayList<>();
    private ArrayList<Material> itemMaterials = new ArrayList<>();
    private boolean reopenOnExit;

    private BiFunction<Player, ItemMeta, String> function;

    public ComplexItemListMenu(Player player, GUI previousMenu, String name, HashMap<String, Material> items, ChatColor color, boolean reopenOnExit, BiFunction<Player, ItemMeta, String> function) {
        super(player, name, 54);
        this.previousMenu = previousMenu;
        this.color = color;

        for (Map.Entry<String, Material> entry : items.entrySet()) {
            itemNames.add(entry.getKey());
            itemMaterials.add(entry.getValue());
        }

        this.reopenOnExit = reopenOnExit;
        this.function = function;
        setupGUI();
    }

    @Override
    public void initialize() {

    }

    @SuppressWarnings("Duplicates")
    @Override
    public void loadItems() {
        inv.clear();

        int slot = 10;
        for (Map.Entry<String, Material> item : getPageFromArray().entrySet()) {
            while ((slot % 9) < 1 || (slot % 9) > 7) {
                slot++;
            }
            inv.setItem(slot, createGuiItem(color + item.getKey(),
                    new ArrayList<>(), item.getValue()));
            slot++;
        }

        //Controls
        if (page > 1) {
            inv.setItem(48, createGuiItem(ChatColor.GOLD + "Page Down",
                    new ArrayList<String>(), Material.ARROW));
        }

        inv.setItem(49, createGuiItem(ChatColor.GOLD + "Page " + page + " / " + ((int) Math.ceil((float) itemNames.size() / 21) + 1),
                new ArrayList<String>(), Material.ARROW));

        if (itemNames.size() > page * 21) {
            inv.setItem(50, createGuiItem(ChatColor.GOLD + "Page Up",
                    new ArrayList<String>(), Material.ARROW));
        }

        inv.setItem(45, createGuiItem(ChatColor.GOLD + "Back",
                new ArrayList<String>(), Material.ARROW));
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent e, String rawItemName) {
        switch (rawItemName) {
            case "page down":
                if (page > 1) {
                    page--;
                    loadItems();
                }
                break;
            case "page up":
                page++;
                loadItems();
                break;
            case "back":
                previousMenu.open();
                break;
            default:
                if (e.getCurrentItem().getType() == Material.AIR)
                    break;

                if (reopenOnExit) {
                    previousMenu.initialize();
                    previousMenu.open();
                } else {
                    getPlayer().closeInventory();
                }

                function.apply(getPlayer(), e.getCurrentItem().getItemMeta());
                break;
        }
    }

    private HashMap<String, Material> getPageFromArray() {
        HashMap<String, Material> pageItems = new LinkedHashMap<>();

        for (int x = 21 * (page - 1); x < 21 * page && x < itemNames.size(); x++) {
            pageItems.put(itemNames.get(x), itemMaterials.get(x));
        }

        return pageItems;
    }
}