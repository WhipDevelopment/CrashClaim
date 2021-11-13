package net.crashcraft.crashclaim.menus.list;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.menus.ClaimMenu;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ClaimListMenu extends GUI {
    private final GUI previousMenu;

    private int page = 1;
    private final ArrayList<Claim> claims;
    private final HashMap<Integer, Claim> pageItemsDisplay;

    public ClaimListMenu(Player player, GUI previousMenu) {
        super(player, BaseComponent.toLegacyText(Localization.MENU__CLAIM_LIST__TITLE.getMessage(null)), 54);
        this.previousMenu = previousMenu;
        this.claims = new ArrayList<>();
        this.pageItemsDisplay = new HashMap<>();

        initialize();
    }

    @Override
    public void initialize() {
        claims.clear();

        CrashClaim.newChain().async(() -> {
            try {
                ClaimDataManager manager = CrashClaim.getPlugin().getDataManager();
                claims.addAll(manager.getOwnedClaims(player.getUniqueId()));
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }).sync(this::loadItems)
        .execute();
    }

    @Override
    public void loadItems() {
        inv.clear();

        ArrayList<Claim> currentPageItems = getPageFromArray();
        pageItemsDisplay.clear();

        int slot = 10;
        for (Claim claim : currentPageItems) {
            while ((slot % 9) < 1 || (slot % 9) > 7) {
                slot++;
            }

            pageItemsDisplay.put(slot, claim);

            ItemStack descItem;

            if (claim.getOwner().equals(getPlayer().getUniqueId())) {
                descItem = Localization.MENU__GENERAL__CLAIM_ITEM_NO_OWNER.getItem(player,
                        "name", claim.getName(),
                        "min_x", Integer.toString(claim.getMinX()),
                        "min_z", Integer.toString(claim.getMinZ()),
                        "max_x", Integer.toString(claim.getMaxX()),
                        "max_z", Integer.toString(claim.getMaxZ()),
                        "world", Bukkit.getWorld(claim.getWorld()).getName()
                );
            } else {
                descItem = Localization.MENU__GENERAL__CLAIM_ITEM.getItem(player,
                        "name", claim.getName(),
                        "min_x", Integer.toString(claim.getMinX()),
                        "min_z", Integer.toString(claim.getMinZ()),
                        "max_x", Integer.toString(claim.getMaxX()),
                        "max_z", Integer.toString(claim.getMaxZ()),
                        "world", Bukkit.getWorld(claim.getWorld()).getName(),
                        "owner", Bukkit.getOfflinePlayer(claim.getOwner()).getName()
                );
            }

            descItem.setType(GlobalConfig.visual_menu_items.getOrDefault(claim.getWorld(), Material.OAK_FENCE));

            inv.setItem(slot, descItem);

            slot++;
        }

        //Controls
        if (page > 1) {
            inv.setItem(48, Localization.MENU__GENERAL__PREVIOUS_BUTTON.getItem(player));
        }

        int maxPages = (int) Math.ceil((float) claims.size() / 21);

        inv.setItem(49, Localization.MENU__GENERAL__PAGE_DISPLAY.getItem(player,
                "page", Integer.toString(page),
                "page_total", Integer.toString(maxPages == 0 ? 1 : maxPages)
        ));

        if (claims.size() > page * 21) {
            inv.setItem(50, Localization.MENU__GENERAL__NEXT_BUTTON.getItem(player));
        }

        if (previousMenu != null) {
            inv.setItem(45, Localization.MENU__GENERAL__BACK_BUTTON.getItem(player));
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent e, String rawItemName) {
        switch (e.getSlot()) {
            case 48:
                if (page > 1) {
                    page--;
                    loadItems();
                }
                break;
            case 50:
                page++;
                loadItems();
                break;
            case 45:
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