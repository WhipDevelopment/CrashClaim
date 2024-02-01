package net.crashcraft.crashclaim.crashutils.menusystem.defaultmenus;

import net.crashcraft.crashclaim.crashutils.menusystem.GUI;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

//https://github.com/CrashCraftNetwork/CrashUtils/blob/master/src/main/java/dev/whip/crashutils/menusystem/defaultmenus/ConfirmationMenu.java
public class ConfirmationMenu extends GUI {
    private final BiFunction<Player, Boolean, String> function;
    private final Function<Player, String> onCloseFunction;
    private final Player player;

    private final ItemStack item;
    private final ItemStack acceptItem;
    private final ItemStack cancelItem;

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