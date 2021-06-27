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
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

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

    private int page;
    private int numPerPage;
    private int controlOffset;

    protected ItemStack descItem;
    protected final GUI prevMenu;

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
        int FIXED_CONTROL_OFFSET = 47;
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

        if (group.getOwner() instanceof SubClaim){
            SubClaim subClaim = (SubClaim) group.getOwner();

            if (!subClaim.getParent().getOwner().equals(getPlayer().getUniqueId())){
                descItem = Localization.MENU__GENERAL__CLAIM_ITEM.getItem(
                        "name", claim.getName(),
                        "min_x", Integer.toString(claim.getMinX()),
                        "min_z", Integer.toString(claim.getMinZ()),
                        "max_x", Integer.toString(claim.getMaxX()),
                        "max_z", Integer.toString(claim.getMaxZ()),
                        "world", Bukkit.getWorld(claim.getWorld()).getName(),
                        "owner", Bukkit.getOfflinePlayer(subClaim.getParent().getOwner()).getName()
                );

                descItem.setType(GlobalConfig.visual_menu_items.get(claim.getWorld()));
                return;
            }
        }

        descItem = Localization.MENU__GENERAL__CLAIM_ITEM_NO_OWNER.getItem(
                "name", claim.getName(),
                "min_x", Integer.toString(claim.getMinX()),
                "min_z", Integer.toString(claim.getMinZ()),
                "max_x", Integer.toString(claim.getMaxX()),
                "max_z", Integer.toString(claim.getMaxZ()),
                "world", Bukkit.getWorld(claim.getWorld()).getName()
        );

        descItem.setType(GlobalConfig.visual_menu_items.get(claim.getWorld()));
    }

    @Override
    public void initialize() {

    }

    @Override
    public void loadItems() {
        inv.clear();

        if (prevMenu != null){
            inv.setItem(45, Localization.MENU__GENERAL__BACK_BUTTON.getItem());
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

            ItemStack item = Localization.MENU__PERMISSIONS__CONTAINERS__DISPLAY.getItem("name", CrashClaim.getPlugin().getMaterialName().getMaterialName(material));
            item.setType(material);
            inv.setItem(startMenu + x, item);

            Boolean allow = helper.hasPermission(group.getOwner(), player.getUniqueId(), material);

            drawSwitch(switchType, getUniversalContainerPerm(material), x, allow);
        }

        if (containers.size() > numPerPage) {
            if ((offset - numPerPage) >= 0){
                inv.setItem(controlOffset, Localization.MENU__GENERAL__PREVIOUS_BUTTON.getItem());
            }

            inv.setItem(controlOffset + 1, Localization.MENU__GENERAL__PAGE_DISPLAY.getItem(
                    "page", Integer.toString(page + 1),
                    "page_total", Integer.toString((int) (Math.floor((float) containers.size() / numPerPage) + 1))
            ));

            if ((offset + numPerPage) < containers.size() - 1){
                inv.setItem(controlOffset + 2, Localization.MENU__GENERAL__NEXT_BUTTON.getItem());
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

            inv.setItem(startMenu + itemOffset, getItemForPermission(route));

            Boolean allow = helper.hasPermission(group.getOwner(), setter, route);

            drawSwitch(itemDisplay.get(route), getUniversalPerm(route), itemOffset, allow);

            itemOffset++;
        }
    }

    private ItemStack getItemForPermission(PermissionRoute route){
        switch (route){
            case MISC:
                return Localization.MENU__PERMISSIONS__MISC.getItem();
            case ADMIN:
                return Localization.MENU__PERMISSIONS__ADMIN.getItem();
            case BUILD:
                return Localization.MENU__PERMISSIONS__BUILD.getItem();
            case FLUIDS:
                return Localization.MENU__PERMISSIONS__FLUIDS.getItem();
            case PISTONS:
                return Localization.MENU__PERMISSIONS__PISTONS.getItem();
            case ENTITIES:
                return Localization.MENU__PERMISSIONS__ENTITIES.getItem();
            case CONTAINERS:
                return Localization.MENU__PERMISSIONS__CONTAINERS.getItem();
            case EXPLOSIONS:
                return Localization.MENU__PERMISSIONS__EXPLOSIONS.getItem();
            case INTERACTIONS:
                return Localization.MENU__PERMISSIONS__INTERACTIONS.getItem();
            case MODIFY_CLAIM:
                return Localization.MENU__PERMISSIONS__MODIFY_CLAIM.getItem();
            case TELEPORTATION:
                return Localization.MENU__PERMISSIONS__TELEPORTATION.getItem();
            case VIEW_SUB_CLAIMS:
                return Localization.MENU__PERMISSIONS__VIEW_SUB_CLAIMS.getItem();
            case MODIFY_PERMISSIONS:
                return Localization.MENU__PERMISSIONS__MODIFY_PERMISSIONS.getItem();
            default:
                return null; // Will never happen
        }
    }

    private void drawSwitch(MenuSwitchType type, int value, int itemOffset, Boolean allow){
        switch (type) {
            case TRIPLE:
                if (allow == null){ // Overridden
                    inv.setItem(startMenu + itemOffset + 9, Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__OVERRODE.getItem());
                    inv.setItem(startMenu + itemOffset + 18, Localization.MENU__PERMISSIONS_TOGGLES__NEUTRAL__OVERRODE.getItem());
                    inv.setItem(startMenu + itemOffset + 27, Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__OVERRODE.getItem());
                    return;
                }

                switch (value) {
                    case 0:
                        inv.setItem(startMenu + itemOffset + 27,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__SELECTED.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__SELECTED_DISABLED.getItem());

                        inv.setItem(startMenu + itemOffset + 9,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__OFF.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__DISABLED.getItem());
                        inv.setItem(startMenu + itemOffset + 18,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__NEUTRAL__OFF.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__NEUTRAL__DISABLED.getItem());
                        break;
                    case 1:
                        inv.setItem(startMenu + itemOffset + 9,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__SELECTED.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__SELECTED_DISABLED.getItem());

                        inv.setItem(startMenu + itemOffset + 18,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__NEUTRAL__OFF.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__NEUTRAL__DISABLED.getItem());
                        inv.setItem(startMenu + itemOffset + 27,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__OFF.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__DISABLED.getItem());
                        break;
                    case 2:
                        inv.setItem(startMenu + itemOffset + 18,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__NEUTRAL__SELECTED.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__NEUTRAL__SELECTED_DISABLED.getItem());

                        inv.setItem(startMenu + itemOffset + 9,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__OFF.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__DISABLED.getItem());
                        inv.setItem(startMenu + itemOffset + 27,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__OFF.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__DISABLED.getItem());
                        break;
                    case 4:
                        inv.setItem(startMenu + itemOffset + 9,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__OFF.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__DISABLED.getItem());
                        inv.setItem(startMenu + itemOffset + 18,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__OFF.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__DISABLED.getItem());
                        inv.setItem(startMenu + itemOffset + 27,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__OFF.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__DISABLED.getItem());
                        break;
                }
                break;
            case DOUBLE:
                if (allow == null){
                    inv.setItem(startMenu + itemOffset + 18, Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__OVERRODE.getItem());
                    inv.setItem(startMenu + itemOffset + 27, Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__OVERRODE.getItem());
                    return;
                }

                switch (value) {
                    case 0:
                        inv.setItem(startMenu + itemOffset+ 27,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__SELECTED.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__SELECTED_DISABLED.getItem());
                        inv.setItem(startMenu + itemOffset + 18,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__OFF.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__DISABLED.getItem());
                        break;
                    case 1:
                        inv.setItem(startMenu + itemOffset + 18,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__SELECTED.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__SELECTED_DISABLED.getItem());
                        inv.setItem(startMenu + itemOffset + 27,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__OFF.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__DISABLED.getItem());
                        break;
                    case 4:
                        inv.setItem(startMenu + itemOffset + 18,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__OFF.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__DISABLED.getItem());
                        inv.setItem(startMenu + itemOffset + 27,
                                allow ? Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__OFF.getItem() : Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__DISABLED.getItem());
                        break;
                }
                break;
            case DOUBLE_DISABLED:
                switch (value) {
                    case 0:
                        inv.setItem(startMenu + itemOffset+ 27, Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__SELECTED_DISABLED.getItem());
                        inv.setItem(startMenu + itemOffset + 18, Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__DISABLED.getItem());
                        break;
                    case 1:
                        inv.setItem(startMenu + itemOffset + 18, Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__SELECTED_DISABLED.getItem());
                        inv.setItem(startMenu + itemOffset + 27, Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__DISABLED.getItem());
                        break;
                    case 4:
                        inv.setItem(startMenu + itemOffset + 18, Localization.MENU__PERMISSIONS_TOGGLES__ENABLE__DISABLED.getItem());
                        inv.setItem(startMenu + itemOffset + 27, Localization.MENU__PERMISSIONS_TOGGLES__DISABLE__DISABLED.getItem());
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
