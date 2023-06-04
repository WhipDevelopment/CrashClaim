package net.crashcraft.crashclaim.utils.menusystem;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

public class CrashGuiHolder implements InventoryHolder {
    final private Player player;
    final private GUI manager;
    final private Plugin plugin;

    private Inventory inventory;

    CrashGuiHolder(Player owner, GUI manager, Plugin plugin){
        this.player = owner;
        this.manager = manager;
        this.plugin = plugin;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer(){
        return player;
    }

    public void setInventory(Inventory inventory){
        this.inventory = inventory;
    }

    public GUI getManager(){
        return manager;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}