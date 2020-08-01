package net.crashcraft.whipclaim.menus.list;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.config.GlobalConfig;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.menus.ClaimMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class ClaimListMenu extends GUI {
    private final GUI previousMenu;

    private int page = 1;
    private final ArrayList<Claim> claims;
    private final HashMap<Integer, Claim> pageItemsDisplay;

    public ClaimListMenu(Player player, GUI previousMenu) {
        super(player, "Claims", 54);
        this.previousMenu = previousMenu;
        this.claims = new ArrayList<>();
        this.pageItemsDisplay = new HashMap<>();

        setupGUI();
    }

    @Override
    public void initialize() {
        claims.clear();

        ClaimDataManager manager = WhipClaim.getPlugin().getDataManager();

        Set<Integer> cla = manager.getOwnedClaims(player.getUniqueId());
        if (cla != null) {
            for (Integer id : cla) {
                Claim claim = manager.getClaim(id);
                if (!claims.contains(claim)){
                    claims.add(claim);
                }
            }
        }

        Set<Integer> subClaims = manager.getOwnedSubClaims(player.getUniqueId());
        if (subClaims != null) {
            for (Integer id : subClaims) {
                Claim claim = manager.getParentClaim(id);
                if (!claims.contains(claim)){
                    claims.add(claim);
                }
            }
        }
    }

    @Override
    public void loadItems() {
        inv.clear();

        ArrayList<Claim> currentPageItems = getPageFromArray();
        pageItemsDisplay.clear();

        int slot = 10;
        for (Claim item : currentPageItems) {
            while ((slot % 9) < 1 || (slot % 9) > 7) {
                slot++;
            }

            ArrayList<String> desc = new ArrayList<>(Arrays.asList(
                    ChatColor.GREEN + "Coordinates: " + ChatColor.YELLOW +
                            ChatColor.YELLOW + item.getMinX() + ", " + item.getMinZ()
                            + ChatColor.GOLD + ", " +
                            ChatColor.YELLOW + item.getMaxX() + ", " + item.getMaxZ(),
                    ChatColor.GREEN + "World: " + ChatColor.YELLOW + Bukkit.getWorld(item.getWorld()).getName()
            ));

            pageItemsDisplay.put(slot, item);

            inv.setItem(slot, createGuiItem(ChatColor.GOLD + item.getName(), desc, GlobalConfig.visual_menu_items.get(item.getWorld())));

            slot++;
        }

        //Controls
        if (page > 1) {
            inv.setItem(48, createGuiItem(ChatColor.GOLD + "Page Down", Material.ARROW));
        }

        inv.setItem(49, createGuiItem(ChatColor.GOLD + "Page " + page + " / " + (int) Math.ceil((float) claims.size() / 21),
                Material.ARROW));

        if (claims.size() > page * 21) {
            inv.setItem(50, createGuiItem(ChatColor.GOLD + "Page Up", Material.ARROW));
        }

        if (previousMenu != null) {
            inv.setItem(45, createGuiItem(ChatColor.GOLD + "Back", Material.ARROW));
        }
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
                if (previousMenu == null) {
                    return;
                }
                previousMenu.open();
                break;
            default:
                Claim claim = pageItemsDisplay.get(e.getSlot());
                if (claim == null){
                    return;
                }
                new ClaimMenu(player, claim, this).open();
                break;
        }
    }

    private ArrayList<Claim> getPageFromArray() {
        ArrayList<Claim> pageItems = new ArrayList<>();

        for (int x = 21 * (page - 1); x < 21 * page && x < claims.size(); x++) {
            Claim claim = claims.get(x);
            pageItems.add(claim);
        }

        return pageItems;
    }
}