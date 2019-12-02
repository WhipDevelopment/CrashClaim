package net.crashcraft.whipclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.PermissionGroup;
import net.crashcraft.whipclaim.claimobjects.PermissionSet;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@CommandAlias("test")
public class TestCommand extends BaseCommand {
    private ClaimDataManager manager;

    public TestCommand(ClaimDataManager manager){
        this.manager = manager;

    }

    @Subcommand("add")
    public void add(Player player){
        Location location = player.getLocation();
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim != null){
            player.sendMessage(ChatColor.GREEN + "Adding permission");

            PermissionGroup group = claim.getPerms();
            group.setPlayerPermissionSet(player.getUniqueId(), manager.getPermissionSetup().getOwnerPermissionSet());
        } else {
            player.sendMessage(ChatColor.RED + "No claim");
        }
    }

    @Subcommand("show")
    public void remove(Player player){
        Location location = player.getLocation();
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim != null){
            for (SubClaim subClaim : claim.getSubClaims()){
                player.sendMessage("Sub Claim: " + subClaim.getId());
            }
        } else {
            player.sendMessage(ChatColor.RED + "No claim");
        }
    }
}
