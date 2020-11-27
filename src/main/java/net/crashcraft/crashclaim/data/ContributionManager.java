package net.crashcraft.crashclaim.data;

import net.crashcraft.crashpayment.payment.TransactionType;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.config.GlobalConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ContributionManager {
    public static int getArea(int minCornerX, int minCornerZ, int maxCornerX, int maxCornerZ){
        return ((maxCornerX - minCornerX) * (maxCornerZ - minCornerZ));
    }

    public static void addContribution(Claim claim, int minCornerX, int minCornerZ, int maxCornerX, int maxCornerZ, UUID player){
        int area = getArea(minCornerX, minCornerZ, maxCornerX, maxCornerZ);
        int originalArea = getArea(claim.getMinX(), claim.getMinZ(), claim.getMaxX(), claim.getMaxZ());

        int difference = area - originalArea;

        if (difference == 0)
            return;

        if (difference > 0){
            claim.addContribution(player, difference);  //add to contribution
        } else {
            int value = (int) Math.floor(Math.floor(difference * GlobalConfig.money_per_block) / claim.getContribution().size());

            if (value == 0){
                return;
            }

            for (Map.Entry<UUID, Integer> entry : claim.getContribution().entrySet()){
                CrashClaim.getPlugin().getPayment().makeTransaction(entry.getKey(), TransactionType.DEPOSIT, "Claim Refund", value, (transaction) -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(transaction.getOwner());
                    if (offlinePlayer.isOnline()){
                        Player p = offlinePlayer.getPlayer();
                        if (p != null) {
                            p.sendMessage(ChatColor.GREEN + "You have received " + ChatColor.GOLD +
                                    ((int) Math.floor(transaction.getAmount()))
                                    + ChatColor.GREEN + " for a refunded contribution to a claim.");
                        }
                    }
                });
            }
        }
    }

    public static void refundContributors(Claim claim){
        int area = getArea(claim.getMinX(), claim.getMinZ(), claim.getMaxX(), claim.getMaxZ());
        int value = (int) Math.floor(Math.floor(area * GlobalConfig.money_per_block) / claim.getContribution().size());

        if (value == 0){
            return;
        }

        for (Map.Entry<UUID, Integer> entry : claim.getContribution().entrySet()){
            CrashClaim.getPlugin().getPayment().makeTransaction(entry.getKey(), TransactionType.DEPOSIT, "Claim Refund", value, (transaction) -> {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(transaction.getOwner());
                if (offlinePlayer.isOnline()){
                    Player p = offlinePlayer.getPlayer();
                    if (p != null) {
                        p.sendMessage(ChatColor.GREEN + "You have received " + ChatColor.GOLD +
                                ((int) Math.floor(transaction.getAmount()))
                                + ChatColor.GREEN + " for a refunded contribution to a claim.");
                    }
                }
            });
        }
    }
}
