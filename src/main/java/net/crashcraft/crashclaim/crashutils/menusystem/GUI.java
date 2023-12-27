package net.crashcraft.crashclaim.crashutils.menusystem;

import net.crashcraft.crashclaim.CrashClaim;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//https://github.com/CrashCraftNetwork/CrashUtils/blob/master/src/main/java/dev/whip/crashutils/menusystem/GUI.java
public abstract class GUI {
    private static final Plugin plugin = CrashClaim.getPlugin();

    private String title;
    private int slots;

    protected Inventory inv;
    protected Player player;

    private boolean lockGUI = true;

    public GUI(Player player, String title, int slots){
        initialize(player, title, slots);
    }

    public GUI(Player player, String title, int slots, boolean lockGUI){
        initialize(player, title, slots);
        this.lockGUI = lockGUI;
    }

    private void initialize(Player player, String title, int slots){
        this.title = title;
        this.slots = slots;
        this.player = player;

        CrashGuiHolder guiHolder = new CrashGuiHolder(player, this, plugin);
        inv = Bukkit.createInventory(guiHolder, slots, title);
        guiHolder.setInventory(inv);
    }

    public void forceClose(){
        ArrayList<HumanEntity> newList = new ArrayList<>(inv.getViewers());
        for (HumanEntity humanEntity : newList){
            if (humanEntity.getOpenInventory().getTopInventory().equals(inv))
                humanEntity.closeInventory();
        }
    }

    public void rawInventoryClickEvent(InventoryClickEvent e){

    }

    public void setupGUI(){
        initialize();
        loadItems();
    }

    public void open(){
        player.openInventory(inv);
    }

    public abstract void initialize();

    public abstract void loadItems();

    public abstract void onClose();

    public abstract void onClick(InventoryClickEvent event, String rawItemName);

    public Player getPlayer() {
        return player;
    }

    public String getTitle() {
        return title;
    }

    public int getSlots() {
        return slots;
    }

    public Inventory getInventory() {
        return inv;
    }

    public boolean isLockGUI(){
        return lockGUI;
    }

    // -- ItemUtils --

    public static ItemStack createGuiItem(BaseComponent[] name, List<BaseComponent[]> desc, Material mat) {
        ItemStack i = new ItemStack(mat, 1);
        ItemMeta iMeta = i.getItemMeta();
        iMeta.setDisplayNameComponent(name);
        iMeta.setLoreComponents(desc);
        i.setItemMeta(iMeta);
        return i;
    }

    public static ItemStack createGuiItem(String name, List<String> desc, Material mat) {
        ItemStack i = new ItemStack(mat, 1);
        ItemMeta iMeta = i.getItemMeta();
        iMeta.setDisplayName(name);
        iMeta.setLore(desc);
        i.setItemMeta(iMeta);
        return i;
    }

    public static ItemStack createGuiItem(String name, Material mat) {
        ItemStack i = new ItemStack(mat, 1);
        ItemMeta iMeta = i.getItemMeta();
        iMeta.setDisplayName(name);
        i.setItemMeta(iMeta);
        return i;
    }

    public static ItemStack createPlayerHead(OfflinePlayer player, String name, List<String> desc) {
        ItemStack item = createGuiItem(player.getName(), desc, Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createPlayerHead(OfflinePlayer player, String name) {
        ItemStack item = createGuiItem(player.getName(), Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createPlayerHead(UUID uuid, ArrayList<String> desc) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return createPlayerHead(player, player.getName(), desc);
    }

    public static ItemStack createPlayerHead(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return createPlayerHead(player, player.getName());
    }

    public static ItemStack createPlayerHead(UUID uuid, String name, ArrayList<String> desc) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return createPlayerHead(player, name, desc);
    }

    public static ItemStack createPlayerHead(UUID uuid, String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return createPlayerHead(player, name);
    }
}