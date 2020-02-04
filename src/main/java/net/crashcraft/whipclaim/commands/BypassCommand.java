package net.crashcraft.whipclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import net.crashcraft.whipclaim.permissions.BypassManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class BypassCommand extends BaseCommand {
    private BypassManager manager;

    public BypassCommand(BypassManager manager){
        this.manager = manager;
    }

    @CommandAlias("bypassClaims")
    @CommandPermission("crashclaim.admin.bypass")
    public void onByp(Player player){
        if (manager.toggleBypass(player.getUniqueId())){
            player.sendMessage(ChatColor.GREEN + "Enabled claim bypass. You are now bypassing permissions");
        } else {
            player.sendMessage(ChatColor.RED + "Disabled claim bypass.");
        }
    }
}
