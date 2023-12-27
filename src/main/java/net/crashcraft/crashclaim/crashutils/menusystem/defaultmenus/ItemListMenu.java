package net.crashcraft.crashclaim.crashutils.menusystem.defaultmenus;

import net.crashcraft.crashclaim.crashutils.menusystem.GUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.function.BiFunction;

//https://github.com/CrashCraftNetwork/CrashUtils/blob/master/src/main/java/dev/whip/crashutils/menusystem/defaultmenus/ItemListMenu.java
public class ItemListMenu extends GUI {
    private int page = 1;
    private GUI previousMenu;
    private ChatColor color;
    private Material material;
    private ArrayList<String> arrayList;
    private BiFunction<Player, ItemMeta, String> function;

    public ItemListMenu(Player player, GUI previousMenu, String name, ArrayList<String> arrayList, ChatColor color, Material material, BiFunction<Player, ItemMeta, String> function){
        super(player,name, 54);
        this.previousMenu = previousMenu;
        this.color = color;
        this.material = material;
        this.arrayList = arrayList;
        this.function = function;
        setupGUI();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void loadItems() {
        inv.clear();

        int slot = 10;
        for (String item : getPageFromArray()){
            while ((slot%9)<1 || (slot%9)>7){
                slot++;
            }
            inv.setItem(slot, createGuiItem(color + item, material));
            slot++;
        }

        //Controls
        if (page > 1) {
            inv.setItem(48, createGuiItem(ChatColor.GOLD + "Page Down", Material.ARROW));
        }

        inv.setItem(49, createGuiItem(ChatColor.GOLD + "Page " + page + " / " + ((int)Math.ceil((float) arrayList.size() / 21) + 1),
                Material.ARROW));

        if (arrayList.size() > page * 21) {
            inv.setItem(50, createGuiItem(ChatColor.GOLD + "Page Up", Material.ARROW));
        }

        inv.setItem(45, createGuiItem(ChatColor.GOLD + "Back", Material.ARROW));
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
                if (e.getCurrentItem().getType() == material)
                    function.apply(getPlayer(), e.getCurrentItem().getItemMeta());
                break;
        }
    }

    private ArrayList<String> getPageFromArray(){
        ArrayList<String> pageItems = new ArrayList<>();

        for (int x = 21 * (page - 1); x < 21 * page && x < arrayList.size(); x++){
            pageItems.add(arrayList.get(x));
        }

        return pageItems;
    }
}