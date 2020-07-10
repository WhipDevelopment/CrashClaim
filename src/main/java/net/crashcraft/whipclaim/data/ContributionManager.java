package net.crashcraft.whipclaim.data;

import dev.whip.crashutils.Payment.PaymentProcessor;
import dev.whip.crashutils.Payment.TransactionType;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.config.GlobalConfig;
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
            PaymentProcessor processor = WhipClaim.getPlugin().getPayment();  //refund

            int value = (int) Math.floor(Math.floor(difference * GlobalConfig.money_per_block) / claim.getContribution().size());

            for (Map.Entry<UUID, Integer> entry : claim.getContribution().entrySet()){
                processor.makeTransaction(entry.getKey(), TransactionType.DEPOSIT, "Claim Refund", value, (transaction) -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(transaction.getOwner());
                    if (offlinePlayer.isOnline()){
                        Player p = offlinePlayer.getPlayer();
                        if (p != null) {
                            p.sendMessage(ChatColor.GREEN + "You have received " + ChatColor.GOLD + transaction.getAmount()
                                    + ChatColor.GREEN + " for a refunded contribution to a claim.");
                        }
                    }
                });
            }
        }
    }
}
