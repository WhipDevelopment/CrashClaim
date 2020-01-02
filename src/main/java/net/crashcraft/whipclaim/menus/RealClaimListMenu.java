package net.crashcraft.whipclaim.menus;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;
import java.util.function.BiFunction;

public class RealClaimListMenu extends GUI{
    private int page = 1;
    private GUI previousMenu;
    private Material material;
    private ArrayList<? extends BaseClaim> claims;
    private BiFunction<Player, BaseClaim, String> function;

    private ArrayList<BaseClaim> currentPageItems;

    public RealClaimListMenu(Player player, GUI previousMenu, String name, ArrayList<? extends BaseClaim> claims,
                             Material material, BiFunction<Player, BaseClaim, String> function){
        super(player,name, 54);
        this.previousMenu = previousMenu;
        this.material = material;
        this.claims = claims;
        this.function = function;
        setupGUI();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void loadItems() {
        inv.clear();

        currentPageItems = getPageFromArray();

        int slot = 10;
        for (BaseClaim item : currentPageItems){
            while ((slot%9)<1 || (slot%9)>7){
                slot++;
            }

            ArrayList<String> desc = new ArrayList<>(Arrays.asList(
                    ChatColor.GREEN + "Coordinates: " + ChatColor.YELLOW +
                            ChatColor.YELLOW + item.getMinX() + ", " + item.getMinZ()
                            + ChatColor.GOLD + ", " +
                            ChatColor.YELLOW + item.getMaxX() + ", " + item.getMaxZ(),
                    ChatColor.GREEN + "World: " + ChatColor.YELLOW + Bukkit.getWorld(item.getWorld()).getName()
            ));

            if (item instanceof SubClaim){
                SubClaim subClaim = (SubClaim) item;
                if (!subClaim.getParent().getOwner().equals(getPlayer().getUniqueId())){
                    desc.add(ChatColor.GREEN + "Owner: " + ChatColor.YELLOW + Bukkit.getOfflinePlayer(subClaim.getParent().getOwner()));
                }
            }

            inv.setItem(slot, createGuiItem(ChatColor.GOLD + item.getName(), desc, material));

            slot++;
        }

        //Controls
        if (page > 1) {
            inv.setItem(48, createGuiItem(ChatColor.GOLD + "Page Down", Material.ARROW));
        }

        inv.setItem(49, createGuiItem(ChatColor.GOLD + "Page " + page + " / " + ((int)Math.ceil((float) claims.size() / 21) + 1),
                Material.ARROW));

        if (claims.size() > page * 21) {
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
                    function.apply(getPlayer(), currentPageItems.get(e.getSlot() - 10));
                break;
        }
    }

    private ArrayList<BaseClaim> getPageFromArray(){
        ArrayList<BaseClaim> pageItems = new ArrayList<>();

        for (int x = 21 * (page - 1); x < 21 * page && x < claims.size(); x++){
            pageItems.add(claims.get(x));
        }

        return pageItems;
    }
}
