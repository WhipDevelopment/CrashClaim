package net.crashcraft.whipclaim.menus.helpers;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.claimobjects.PermissionGroup;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.PermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.whipclaim.permissions.PermissionHelper;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;

public abstract class MenuListHelper extends GUI{
    private static PermissionHelper helper = PermissionHelper.getPermissionHelper();

    private PermissionSet set;

    private LinkedHashMap<PermissionRoute, MenuSwitchType> itemDisplay;

    private ArrayList<PermissionRoute> items = new ArrayList<>();
    private int page = 0;
    private UUID setter;
    private PermissionGroup group;

    public MenuListHelper(Player player, String title, int slots) {
        super(player, title, slots);
    }

    public void setup(LinkedHashMap<PermissionRoute, MenuSwitchType> itemDisplay, PermissionSet set,
                      UUID setter, PermissionGroup group){
        this.itemDisplay = itemDisplay;
        this.set = set;
        this.setter = setter;
        this.group = group;

        items = new ArrayList<>(itemDisplay.keySet());
    }

    @Override
    public void initialize() {

    }

    @Override
    public void loadItems() {
        inv.clear();
        setupInventory();
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        if (event.getCurrentItem() == null){
            return;
        }

        Material clickedItem = event.getCurrentItem().getType();
        int value;

        switch (clickedItem){
            case GREEN_STAINED_GLASS:
                value = 1;
                break;
            case GRAY_STAINED_GLASS:
                value = 2;
                break;
            case RED_STAINED_GLASS:
                value = 0;
                break;
            default: return;
        }

        int index = ((event.getSlot() - 1) % 9) - page * 5;
        if (index > items.size() || index < 0){
            return;
        }

        PermissionRoute route = items.get(index);

        if (route == null){
            return;
        }

        if (!helper.hasPermission(group.getOwner(), setter, PermissionRoute.MODIFY_PERMISSIONS)){
            invalidPermissions();
            return;
        }

        if (!helper.hasPermission(group.getOwner(), setter, route)){
            return;
        }

        setPermission(route, value);

        loadItems();
    }

    /*
        [0 ] [1 ] [2 ] [3 ] [4 ] [5 ] [6 ] [7 ] [8 ]
        [9 ] [10] [11] [12] [13] [14] [15] [16] [17]
        [18] [19] [20] [21] [22] [23] [24] [25] [26]
        [27] [28] [29] [30] [31] [32] [33] [34] [35]
        [36] [37] [38] [39] [40] [41] [42] [43] [44]
        [45] [46] [47] [48] [49] [50] [51] [52] [53]

        Needs to be called no matter what cause this sets up the menu
        Not in conductor to allow class to have logic before init
     */

    public abstract void invalidPermissions();

    public abstract void setPermission(PermissionRoute route, int value);

    private void setupInventory(){
        final int startMenu = 10;

        int itemOffset = 0;

        for (int x = page * 5; x < (page * 5 + 5 < items.size() ? page * 5 + 5 : items.size()); x++){
            PermissionRoute route = items.get(x);

            if (route == null){
                itemOffset++;
                continue;
            }

            inv.setItem(startMenu + itemOffset, StaticItemLookup.getItem(route));

            boolean allow = helper.hasPermission(group.getOwner(), setter, route);

            switch (itemDisplay.get(route)) {
                case TRIPLE:
                    switch (getUniversalPerm(route)) {
                        case 0:
                            inv.setItem(startMenu + itemOffset + 27, createGuiItem(ChatColor.RED + "Disabled",
                                    allow ? Material.RED_CONCRETE : Material.GRAY_CONCRETE));

                            inv.setItem(startMenu + itemOffset + 9, createGuiItem(ChatColor.DARK_GREEN + "Enable",
                                    allow ? Material.GREEN_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE));
                            inv.setItem(startMenu + itemOffset + 18, createGuiItem(ChatColor.DARK_GRAY + "Neutral",
                                    allow ?  Material.GRAY_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE));
                            break;
                        case 1:
                            inv.setItem(startMenu + itemOffset + 9, createGuiItem(ChatColor.GREEN + "Enabled",
                                    allow ? Material.GREEN_CONCRETE : Material.GRAY_CONCRETE));

                            inv.setItem(startMenu + itemOffset + 18, createGuiItem(ChatColor.DARK_GRAY + "Neutral",
                                    allow ? Material.GRAY_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE));
                            inv.setItem(startMenu + itemOffset + 27, createGuiItem(ChatColor.DARK_RED + "Disable",
                                    allow ? Material.RED_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE));
                            break;
                        case 2:
                            inv.setItem(startMenu + itemOffset + 18, createGuiItem(ChatColor.GRAY + "Neutral",
                                     Material.GRAY_CONCRETE));

                            inv.setItem(startMenu + itemOffset + 9, createGuiItem(ChatColor.DARK_GREEN + "Enable",
                                    allow ? Material.GREEN_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE));
                            inv.setItem(startMenu + itemOffset + 27, createGuiItem(ChatColor.DARK_RED + "Disable",
                                    allow ? Material.RED_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE));
                            break;
                    }
                    break;
                case DOUBLE:
                    switch (getUniversalPerm(route)) {
                        case 0:
                            inv.setItem(startMenu + itemOffset+ 27, createGuiItem(ChatColor.GREEN + "Disable",
                                    allow ? Material.RED_CONCRETE : Material.GRAY_CONCRETE));

                            inv.setItem(startMenu + itemOffset + 18, createGuiItem(ChatColor.DARK_GREEN + "Enabled",
                                    allow ? Material.GREEN_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE));
                            break;
                        case 1:
                            inv.setItem(startMenu + itemOffset + 18, createGuiItem(ChatColor.GREEN + "Enabled",
                                   allow ? Material.GREEN_CONCRETE : Material.GRAY_CONCRETE));

                            inv.setItem(startMenu + itemOffset + 27, createGuiItem(ChatColor.DARK_RED + "Disabled",
                                    allow ? Material.RED_STAINED_GLASS : Material.GRAY_STAINED_GLASS_PANE));
                            break;
                    }
                    break;
            }

            itemOffset++;
        }
    }

    private int getUniversalPerm(PermissionRoute route){
        if (set instanceof PlayerPermissionSet){
            return route.getPerm((PlayerPermissionSet) set);
        } else if (set instanceof GlobalPermissionSet){
            return route.getPerm((GlobalPermissionSet) set);
        }

        return -1;
    }
}
