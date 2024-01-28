package net.crashcraft.crashclaim.crashutils.menusystem;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

//https://github.com/CrashCraftNetwork/CrashUtils/blob/master/src/main/java/dev/whip/crashutils/menusystem/CrashMenuController.java
public class CrashMenuController implements Listener {
    private final Plugin plugin;

    public CrashMenuController(Plugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e){
        /* temporarily (permanently) disable due to a bug that allows
           players to put items into the menu's with number keys.
           (and probably some other ways too?)

        if (e.getCurrentItem() == null)
            return;

         */
        if (e.getInventory().getHolder() instanceof CrashGuiHolder){
            CrashGuiHolder holder = ((CrashGuiHolder) e.getInventory().getHolder());

            if (!holder.getPlugin().equals(plugin)){
                return;
            }
            GUI gui = holder.getManager();

            gui.rawInventoryClickEvent(e);

            if (gui.isLockGUI()){
                e.setCancelled(true);
            }
            ItemStack clickedItem = e.getCurrentItem();

            if (clickedItem == null || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
                return;
            }
            gui.onClick(e, ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase()));
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof CrashGuiHolder) {
            ((CrashGuiHolder) e.getInventory().getHolder()).getManager().onClose();
        }
    }
}