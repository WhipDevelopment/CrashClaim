package net.crashcraft.crashclaim.utils.menusystem.defaultmenus;

import net.crashcraft.crashclaim.utils.menusystem.GUI;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ConfirmationMenu extends GUI {
    private final BiFunction<Player, Boolean, String> function;
    private final Function<Player, String> onCloseFunction;
    private final Player player;

    private final ItemStack item;
    private final ItemStack acceptItem;
    private final ItemStack cancelItem;

    public ConfirmationMenu(Player player, BaseComponent[] title, BaseComponent[] message,
                            List<BaseComponent[]> description, Material material,
                            BiFunction<Player, Boolean, String> function, Function<Player, String> onCloseFunction){
        super(player, BaseComponent.toLegacyText(title), 45);

        this.player = player;
        this.function = function;
        this.onCloseFunction = onCloseFunction;

        item = createGuiItem(message, description, material);
        acceptItem = createGuiItem(ChatColor.GOLD + "Accept", Material.GREEN_CONCRETE);
        cancelItem = createGuiItem(ChatColor.GOLD + "Cancel", Material.RED_CONCRETE);

        setupGUI();
    }

    public ConfirmationMenu(Player player,
                            BaseComponent[] title,
                            ItemStack item,
                            ItemStack acceptItem,
                            ItemStack cancelItem,
                            BiFunction<Player, Boolean, String> function, Function<Player, String> onCloseFunction){
        super(player, BaseComponent.toLegacyText(title), 45);

        this.player = player;
        this.function = function;
        this.onCloseFunction = onCloseFunction;

        this.item = item;
        this.acceptItem = acceptItem;
        this.cancelItem = cancelItem;

        setupGUI();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void loadItems() {
        inv.setItem(13, item);

        inv.setItem(29, cancelItem);

        inv.setItem(33, acceptItem);
    }

    @Override
    public void onClose() {
        onCloseFunction.apply(player);
    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        switch (event.getSlot()){
            case 33:
                player.closeInventory();
                function.apply(player, true);
                break;
            case 29:
                player.closeInventory();
                function.apply(player, false);
                break;
        }
    }
}