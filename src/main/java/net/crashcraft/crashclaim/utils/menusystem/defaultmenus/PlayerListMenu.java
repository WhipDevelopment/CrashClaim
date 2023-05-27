package net.crashcraft.crashclaim.utils.menusystem.defaultmenus;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.papermc.lib.PaperLib;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.utils.menusystem.GUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class PlayerListMenu extends GUI {
    private int page = 1;
    private final GUI previousMenu;
    private final BiMap<UUID, String> lookupMap = HashBiMap.create();
    private final ArrayList<UUID> arrayList;
    private final BiFunction<GUI, UUID, String> function;

    public PlayerListMenu(String title, Player player, GUI previousMenu, ArrayList<UUID> arrayList, BiFunction<GUI, UUID, String> function){
        super(player, title == null ? "Select Player" : title, 54);
        this.previousMenu = previousMenu;
        this.arrayList = arrayList;
        this.function = function;
        for (UUID uuid : arrayList) {
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            if (lookupMap.containsValue(name)) {
                lookupMap.put(uuid, name + " - " + uuid.toString());
            } else {
                lookupMap.put(uuid, name);
            }
        }
        setupGUI();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void loadItems() {
        inv.clear();

        HashMap<Integer, UUID> headMap = new HashMap<>();

        final boolean isPaper = PaperLib.isPaper();

        int slot = 10;
        for (UUID uuid : getPageFromArray()){
            while ((slot%9)<1 || (slot%9)>7){
                slot++;
            }

            if (isPaper) {
                inv.setItem(slot, createGuiItem(lookupMap.get(uuid), Material.PLAYER_HEAD));
                headMap.put(slot, uuid);
            } else {
                inv.setItem(slot, createPlayerHead(uuid, lookupMap.get(uuid)));
            }

            slot++;
        }

        if (isPaper) {
            Bukkit.getScheduler().runTaskAsynchronously(CrashClaim.getPlugin(), () -> {
                HashMap<Integer, ItemStack> realHeadMap = new HashMap<>();

                for (Map.Entry<Integer, UUID> entry : headMap.entrySet()) {
                    PlayerProfile profile = Bukkit.createProfile(entry.getValue());

                    if (profile.complete()) {
                        ItemStack item = createGuiItem(profile.getName(), Material.PLAYER_HEAD);
                        SkullMeta meta = (SkullMeta) item.getItemMeta();
                        meta.setPlayerProfile(profile);
                        item.setItemMeta(meta);

                        realHeadMap.put(entry.getKey(), item);
                    }
                }

                Bukkit.getScheduler().runTask(CrashClaim.getPlugin(), () -> {
                    for (Map.Entry<Integer, ItemStack> entry : realHeadMap.entrySet()) {
                        inv.setItem(entry.getKey(), entry.getValue());
                    }
                });
            });
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
    public void onClick(InventoryClickEvent event, String rawItemName) {
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
                previousMenu.initialize();
                previousMenu.open();
                break;
            default:
                if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                    function.apply(this, lookupMap.inverse().get(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName())));
                }
                break;
        }
    }

    private ArrayList<UUID> getPageFromArray(){
        ArrayList<UUID> pageItems = new ArrayList<>();

        for (int x = 21 * (page - 1); x < 21 * page && x < arrayList.size(); x++){
            pageItems.add(arrayList.get(x));
        }

        return pageItems;
    }
}