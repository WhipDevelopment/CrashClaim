package net.crashcraft.crashclaim.localization;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LocalizationUtils {
    public static void sendMessageList(CommandSender player, List<BaseComponent[]> message){
        for (BaseComponent[] line : message){
            player.sendMessage(line);
        }
    }
}
