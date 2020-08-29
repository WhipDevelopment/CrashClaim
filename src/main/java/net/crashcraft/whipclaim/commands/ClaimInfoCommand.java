package net.crashcraft.whipclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.PermState;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;

@CommandAlias("claiminfo")
@CommandPermission("crashclaim.admin.bypass")
public class ClaimInfoCommand extends BaseCommand {
    private final ClaimDataManager manager;

    public ClaimInfoCommand(ClaimDataManager manager){
        this.manager = manager;
    }

    @Default
    public void onClaimInfo(Player player){
        Location location = player.getLocation();
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim != null){
            StringBuilder sb = new StringBuilder();

            GlobalPermissionSet set = claim.getPerms().getGlobalPermissionSet();

            String enabled = ChatColor.DARK_GREEN + "Enabled";
            String disabled = ChatColor.RED + "Disabled";

            sb.append(ChatColor.GOLD).append("Claim Info | ").append(ChatColor.YELLOW)
                    .append("[").append(claim.getMinX()).append(", ").append(claim.getMinZ())
                    .append("], [").append(claim.getMaxX()).append(", ").append(claim.getMaxZ()).append("]\n")
                    .append(ChatColor.GREEN).append("Owner: ").append(ChatColor.WHITE).append(Bukkit.getOfflinePlayer(claim.getOwner()).getName()).append("\n")
                    .append(ChatColor.GOLD).append("Global Permissions\n")
                    .append(ChatColor.GREEN).append("Build").append(set.getBuild() == PermState.ENABLED ? enabled : disabled).append("\n")
                    .append(ChatColor.GREEN).append("Entities").append(set.getEntities() == PermState.ENABLED ? enabled : disabled).append("\n")
                    .append(ChatColor.GREEN).append("Interactions").append(set.getInteractions() == PermState.ENABLED ? enabled : disabled).append("\n")
                    .append(ChatColor.GREEN).append("View Sub Claims").append(set.getViewSubClaims() == PermState.ENABLED ? enabled : disabled).append("\n")
                    .append(ChatColor.GREEN).append("Teleportations").append(set.getTeleportation() == PermState.ENABLED ? enabled : disabled).append("\n")
                    .append(ChatColor.GREEN).append("Explosions").append(set.getExplosions() == PermState.ENABLED ? enabled : disabled).append("\n")
                    .append(ChatColor.GREEN).append("Fluids").append(set.getFluids() == PermState.ENABLED ? enabled : disabled).append("\n")
                    .append(ChatColor.GREEN).append("Pistons").append(set.getPistons() == PermState.ENABLED ? enabled : disabled).append("\n")
                    .append(ChatColor.GOLD).append("Global Container Permissions\n");

            for (Map.Entry<Material, Integer> entry :set.getContainers().entrySet()){
                sb.append(ChatColor.YELLOW).append(WhipClaim.getPlugin().getMaterialName().getMaterialName(entry.getKey())).append(": ")
                        .append(entry.getValue() == PermState.ENABLED ? enabled : disabled).append("\n");
            }

            player.sendMessage(sb.toString());
        } else {
            player.sendMessage(ChatColor.RED + "There is no claim where you are standing.");
        }
    }
}
