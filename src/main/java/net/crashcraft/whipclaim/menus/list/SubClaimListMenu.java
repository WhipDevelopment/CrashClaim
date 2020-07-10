package net.crashcraft.whipclaim.menus.list;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import net.crashcraft.whipclaim.config.GlobalConfig;
import net.crashcraft.whipclaim.menus.SubClaimMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class SubClaimListMenu extends GUI {
    private final GUI previousMenu;
    private final Claim claim;

    private int page = 1;
    private ArrayList<SubClaim> claims;
    private ArrayList<SubClaim> currentPageItems;

    public SubClaimListMenu(Player player, GUI previousMenu, Claim claim) {
        super(player, "Sub Claims", 54);
        this.previousMenu = previousMenu;
        this.claim = claim;

        setupGUI();
    }

    @Override
    public void initialize() {
        claims = claim.getSubClaims();
    }

    @Override
    public void loadItems() {
        inv.clear();

        currentPageItems = getPageFromArray();

        int slot = 10;
        for (BaseClaim item : currentPageItems) {
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

            if (item instanceof SubClaim) {
                SubClaim subClaim = (SubClaim) item;
                if (!subClaim.getParent().getOwner().equals(getPlayer().getUniqueId())) {
                    desc.add(ChatColor.GREEN + "Owner: " + ChatColor.YELLOW + Bukkit.getOfflinePlayer(subClaim.getParent().getOwner()).getName());
                }
            }

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
                new SubClaimMenu(player, currentPageItems.get(e.getSlot() - 10)).open();
                break;
        }
    }

    private ArrayList<SubClaim> getPageFromArray() {
        ArrayList<SubClaim> pageItems = new ArrayList<>();

        for (int x = 21 * (page - 1); x < 21 * page && x < claims.size(); x++) {
            pageItems.add(claims.get(x));
        }

        return pageItems;
    }
}
