package net.crashcraft.whipclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.UUID;

@CommandAlias("claim")
@CommandPermission("whipclaim.user.claim")
public class ClaimModeCommand extends BaseCommand implements Listener {
    private HashMap<UUID, Boolean> enabledMap;
    private HashMap<UUID, Location> clickMap;

    public ClaimModeCommand(){
        enabledMap = new HashMap<>();
        clickMap = new HashMap<>();
    }

    @Default
    public void onClaim(Player player){
        UUID uuid = player.getUniqueId();
        if (enabledMap.containsKey(uuid)){
            enabledMap.put(uuid, !enabledMap.get(uuid));
        } else {
            enabledMap.put(uuid, true);
        }

        if (enabledMap.get(uuid)){
            player.sendMessage(ChatColor.GREEN + "Claim mode enabled, click 2 corners to claim.");
        } else {
            player.sendMessage(ChatColor.RED + "Claim mode disabled");
            clickMap.remove(uuid);
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e){
        if (e.getHand() != null && e.getHand().equals(EquipmentSlot.HAND) && e.getClickedBlock() != null){
            click(e.getPlayer(), e.getClickedBlock().getLocation());
        }
    }

    public void click(Player player, Location loc1){
        //call clicked existing claim if they did

        UUID uuid = player.getUniqueId();
        if (!clickMap.containsKey(uuid)){
            clickMap.put(uuid, loc1);
            player.sendMessage(ChatColor.GREEN + "Click the an opposite corner to form a new claim.");
            return;
        }

        Location loc2 = clickMap.get(uuid);


    }

    public void clickedExistingClaim(){

    }
}
