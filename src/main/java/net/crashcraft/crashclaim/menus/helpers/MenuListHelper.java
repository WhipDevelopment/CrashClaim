package net.crashcraft.crashclaim.menus.helpers;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import net.crashcraft.crashclaim.claimobjects.PermissionGroup;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.crashclaim.claimobjects.permission.PermissionSet;
import net.crashcraft.crashclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public abstract class MenuListHelper extends GUI {
    private static final int startMenu = 10;
    private static final PermissionHelper helper = PermissionHelper.getPermissionHelper();

    private PermissionSet set;

    private LinkedHashMap<PermissionRoute, MenuSwitchType> itemDisplay;
    private List<Material> containers;

    private ArrayList<PermissionRoute> items = new ArrayList<>();
    private UUID setter;
    private PermissionGroup group;

    private boolean isContainerList = false;
    private MenuSwitchType switchType;

    private final int FIXED_CONTROL_OFFSET = 47;

    private int page;
    private int numPerPage;
    private int controlOffset;

    protected ItemStack descItem;
    protected GUI prevMenu;

    public MenuListHelper(Player player, String title, int slots, GUI prevMenu) {
        super(player, title, slots);

        this.prevMenu = prevMenu;
    }

    public void setup(LinkedHashMap<PermissionRoute, MenuSwitchType> itemDisplay, int numPerPage, PermissionSet set,
                      UUID setter, PermissionGroup group){
        isContainerList = false;
        inv.clear();

        this.numPerPage = numPerPage;
        this.itemDisplay = itemDisplay;
        this.set = set;
        this.setter = setter;
        this.group = group;

        items = new ArrayList<>(itemDisplay.keySet());

        this.page = 0;

        init();
        setupGUI();
    }

    public void setupContainerList(PermissionSet set, UUID setter, PermissionGroup group, int numPerPage, int controlOffset){
        isContainerList = true;
        inv.clear();

        this.set = set;
        this.setter = setter;
        this.group = group;
        this.numPerPage = numPerPage;
        this.containers = CrashClaim.getPlugin().getDataManager().getPermissionSetup().getTrackedContainers();
        this.controlOffset = FIXED_CONTROL_OFFSET + controlOffset;

        if (set instanceof PlayerPermissionSet || group.getOwner() instanceof SubClaim){
            switchType = MenuSwitchType.TRIPLE;
        } else if (set instanceof GlobalPermissionSet){
            switchType = MenuSwitchType.DOUBLE;
        }

        this.page = 0;

        init();
        setupGUI();
    }

    private void init(){
        BaseClaim claim = group.getOwner();

        List<String> desc = new ArrayList<>(Arrays.asList(
                ChatColor.GREEN + "Coordinates: " + ChatColor.YELLOW +
                        ChatColor.YELLOW + claim.getMinX() + ", " + claim.getMinZ()
                        + ChatColor.GOLD + ", " +
                        ChatColor.YELLOW + claim.getMaxX() + ", " + claim.getMaxZ()
        ));

        if (group.getOwner() instanceof SubClaim){
            SubClaim subClaim = (SubClaim) group.getOwner();

            if (!subClaim.getParent().getOwner().equals(getPlayer().getUniqueId())){
                desc.add(ChatColor.GREEN + "Owner: " + ChatColor.YELLOW + Bukkit.getOfflinePlayer(subClaim.getParent().getOwner()).getName());
            }
        }

        descItem = createGuiItem(ChatColor.GOLD + claim.getName(),
                desc, GlobalConfig.visual_menu_items.get(claim.getWorld()));
    }

    @Override
    public void initialize() {

    }

    @Override
    public void loadItems() {
        inv.clear();

        if (prevMenu != null){
            inv.setItem(45, StaticItemLookup.BACK_ARROW);
        }

        if (isContainerList){
            setupContainerInventory();
        } else {
            setupInventory();
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        if (prevMenu != null){
            if (event.getSlot() == 45){
                prevMenu.setupGUI();
                prevMenu.open();
                return;
            }
        }

        if (isContainerList){
            onInventoryPreClick(event);
        }

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

        if (!helper.hasPermission(group.getOwner(), setter, PermissionRoute.MODIFY_PERMISSIONS)){
            invalidPermissions();
            return;
        }

        if (isContainerList){
            onInventoryContainerClick(event, value);
        } else {
            onInventoryClick(event, value);
        }
    }

    private void onInventoryClick(InventoryClickEvent event, int value){
        int index = ((event.getSlot() - 1) % 9) - page * numPerPage;
        if (index > items.size() || index < 0){
            return;
        }

        PermissionRoute route = items.get(index);

        if (route == null){
            return;
        }

        if (!helper.hasPermission(group.getOwner(), setter, route)) {
            return;
        }

        if (route.equals(PermissionRoute.CONTAINERS)){
            for (Material material : CrashClaim.getPlugin().getDataManager().getPermissionSetup().getTrackedContainers()) {
                setContainerPermission(value, material);
            }
        } else {
            setPermission(route, value);
        }

        loadItems();
    }

    private void onInventoryPreClick(InventoryClickEvent event){
        int slot = event.getSlot();
        if (slot == controlOffset){
            page--;
            loadItems();
        } else if (slot == controlOffset + 2){
            page++;
            loadItems();
        }
    }

    private void onInventoryContainerClick(InventoryClickEvent event, int value){
        int index = (event.getSlot() - 1) % 9;
        int arrayIndex = index + (page * numPerPage);
        if (index > items.size() || index < 0){
            return;
        }

        if (!helper.hasPermission(group.getOwner(), setter, PermissionRoute.CONTAINERS)) {
            return;
        }

        Material material = containers.get(arrayIndex);

        if (material == null){
            return;
        }

        setContainerPermission(value, material);

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

    private void setupContainerInventory(){
        int offset = (numPerPage * page);

        for (int x = 0; x < numPerPage; x++){
            if (offset + x > containers.size() - 1)
                break;

            Material material = containers.get(x + offset);

            inv.setItem(startMenu + x, createGuiItem(
                    ChatColor.GOLD + CrashClaim.getPlugin().getMaterialName().getMaterialName(material), material
            ));

            Boolean allow = helper.hasPermission(group.getOwner(), player.getUniqueId(), material);

            drawSwitch(switchType, getUniversalContainerPerm(material), x, allow);
        }

        if (containers.size() > numPerPage) {
            if ((offset - numPerPage) >= 0){
                inv.setItem(controlOffset, StaticItemLookup.BACK_BUTTON);
            }

            inv.setItem(controlOffset + 1, createGuiItem(ChatColor.GOLD + Integer.toString(page + 1) + " / " + (int) (Math.floor((float) containers.size() / numPerPage) + 1), Material.PAPER));

            if ((offset + numPerPage) < containers.size() - 1){
                inv.setItem(controlOffset + 2, StaticItemLookup.NEXT_BUTTON);
            }
        }
    }

    private void setupInventory(){
        int itemOffset = 0;

        for (int x = page * numPerPage; x < (Math.min(page * numPerPage + numPerPage, items.size())); x++){
            PermissionRoute route = items.get(x);

            if (route == null){
                itemOffset++;
                continue;
            }

            inv.setItem(startMenu + itemOffset, StaticItemLookup.getItem(route));

            Boolean allow = helper.hasPermission(group.getOwner(), setter, route);

            drawSwitch(itemDisplay.get(route), getUniversalPerm(route), itemOffset, allow);

            itemOffset++;
        }
    }

    private void drawSwitch(MenuSwitchType type, int value, int itemOffset, Boolean allow){
        switch (type) {
            case TRIPLE:
                if (allow == null){
                    inv.setItem(startMenu + itemOffset + 9, createGuiItem(ChatColor.YELLOW + "Enabled",
                           Material.YELLOW_STAINED_GLASS));
                    inv.setItem(startMenu + itemOffset + 18, createGuiItem(ChatColor.YELLOW + "Neutral",
                            Material.YELLOW_STAINED_GLASS));
                    inv.setItem(startMenu + itemOffset + 27, createGuiItem(ChatColor.YELLOW + "Disable",
                            Material.YELLOW_STAINED_GLASS));
                    return;
                }

                switch (value) {
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
                    case 4:
                        inv.setItem(startMenu + itemOffset + 9, createGuiItem(ChatColor.DARK_GREEN + "Enabled",
                                Material.GREEN_STAINED_GLASS));
                        inv.setItem(startMenu + itemOffset + 18, createGuiItem(ChatColor.GRAY + "Neutral",
                                Material.GRAY_STAINED_GLASS));
                        inv.setItem(startMenu + itemOffset + 27, createGuiItem(ChatColor.DARK_RED + "Disable",
                                Material.RED_STAINED_GLASS));
                        break;
                }
                break;
            case DOUBLE:
                if (allow == null){
                    inv.setItem(startMenu + itemOffset + 18, createGuiItem(ChatColor.YELLOW + "Enabled",
                            Material.YELLOW_STAINED_GLASS));
                    inv.setItem(startMenu + itemOffset + 27, createGuiItem(ChatColor.YELLOW + "Disable",
                            Material.YELLOW_STAINED_GLASS));
                    return;
                }

                switch (value) {
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
                    case 4:
                        inv.setItem(startMenu + itemOffset + 18, createGuiItem(ChatColor.DARK_GREEN + "Enabled",
                                Material.GREEN_STAINED_GLASS));
                        inv.setItem(startMenu + itemOffset + 27, createGuiItem(ChatColor.DARK_RED + "Disable",
                                Material.RED_STAINED_GLASS));
                        break;
                }
                break;
            case DOUBLE_DISABLED:
                switch (value) {
                    case 0:
                        inv.setItem(startMenu + itemOffset+ 27, createGuiItem(ChatColor.DARK_RED + "Disable",
                                Material.GRAY_CONCRETE));
                        inv.setItem(startMenu + itemOffset + 18, createGuiItem(ChatColor.DARK_GREEN + "Enabled",
                                Material.GRAY_STAINED_GLASS_PANE));
                        break;
                    case 1:
                        inv.setItem(startMenu + itemOffset + 18, createGuiItem(ChatColor.DARK_GREEN + "Enabled",
                                Material.GRAY_CONCRETE));
                        inv.setItem(startMenu + itemOffset + 27, createGuiItem(ChatColor.DARK_RED + "Disabled",
                                Material.GRAY_STAINED_GLASS_PANE));
                        break;
                    case 4:
                        inv.setItem(startMenu + itemOffset + 18, createGuiItem(ChatColor.DARK_GREEN + "Enabled",
                                Material.GREEN_STAINED_GLASS));
                        inv.setItem(startMenu + itemOffset + 27, createGuiItem(ChatColor.DARK_RED + "Disable",
                                Material.RED_STAINED_GLASS));
                        break;
                }
                break;
        }
    }

    public abstract void invalidPermissions();

    public abstract void setPermission(PermissionRoute route, int value);

    public abstract void setContainerPermission(int value, Material material);

    private int getUniversalPerm(PermissionRoute route){
        if (set instanceof PlayerPermissionSet){
            return route.getPerm((PlayerPermissionSet) set);
        } else if (set instanceof GlobalPermissionSet){
            return route.getPerm((GlobalPermissionSet) set);
        }

        return -1;
    }

    private int getUniversalContainerPerm(Material material){
        if (set instanceof PlayerPermissionSet){
            return PermissionRoute.CONTAINERS.getPerm((PlayerPermissionSet) set, material);
        } else if (set instanceof GlobalPermissionSet){
            return PermissionRoute.CONTAINERS.getPerm((GlobalPermissionSet) set, material);
        }

        return -1;
    }
}
