package net.crashcraft.crashclaim.commands.claiming.modes;

import dev.whip.crashutils.menusystem.defaultmenus.ConfirmationMenu;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.commands.claiming.ClaimCommand;
import net.crashcraft.crashclaim.commands.claiming.ClaimMode;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.data.ClaimResponse;
import net.crashcraft.crashclaim.data.ContributionManager;
import net.crashcraft.crashclaim.data.StaticClaimLogic;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.VisualType;
import net.crashcraft.crashpayment.payment.TransactionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class NewClaimMode implements ClaimMode {
    private final ClaimCommand commandManager;
    private final Player player;
    private final Location firstLocation;
    private final VisualizationManager visualizationManager;
    private final ClaimDataManager manager;

    public NewClaimMode(ClaimCommand commandManager, Player player, Location firstLocation){
        this.commandManager = commandManager;
        this.player = player;
        this.firstLocation = firstLocation;
        this.visualizationManager = commandManager.getVisualizationManager();
        this.manager = commandManager.getDataManager();

        player.sendMessage(ChatColor.GREEN + "Click the an opposite corner to form a new claim.");

        VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
        group.removeAllVisualsOfType(VisualType.MARKER);

        visualizationManager.getProvider().spawnMarkerVisual(VisualColor.YELLOW, group, firstLocation).spawn();
    }

    private boolean checkCanCreate(Location min, Location max){
        if ((max.getBlockX() - min.getBlockX()) < 4 || (max.getBlockZ() - min.getBlockZ()) < 4) {
            player.sendMessage(ChatColor.RED + "A claim has to be at least a 5x5");
            return false;
        }

        if (manager.checkOverLapSurroudningClaims(-1, max.getBlockX(), max.getBlockZ(), min.getBlockX(), min.getBlockZ(), min.getWorld().getUID())){
            player.sendMessage(ChatColor.RED + "You cannot claim over an existing claim.");
            return false;
        }

        return true;
    }

    @Override
    public void click(Player player, Location secondLocation) {
        Location min = StaticClaimLogic.calculateMinCorner(firstLocation, secondLocation);
        Location max = StaticClaimLogic.calculateMaxCorner(firstLocation, secondLocation);

        if (!checkCanCreate(min, max)){
            return;
        }

        int area = ContributionManager.getArea(min.getBlockX(), min.getBlockZ(), max.getBlockX(), max.getBlockZ());

        int price = (int) Math.ceil(area * GlobalConfig.money_per_block);

        new ConfirmationMenu(player,
                "Confirm Claim Creation",
                ChatColor.GREEN + "The claim creation will cost: " + ChatColor.YELLOW + price,
                new ArrayList<>(Arrays.asList("Confirm or deny the creation.")),
                Material.EMERALD,
                (p, aBoolean) -> {
                    if (aBoolean){
                        if (!checkCanCreate(min, max)){
                            return "";
                        }

                        CrashClaim.getPlugin().getPayment().makeTransaction(player.getUniqueId(), TransactionType.WITHDRAW, "Claim Purchase", price, (res) -> {
                            if (!res.transactionSuccess()){
                                player.sendMessage(ChatColor.RED + "You need " + price + " coins to claim that area.");
                                cleanup(player.getUniqueId(), true);
                                return;
                            }

                            Bukkit.getScheduler().runTask(CrashClaim.getPlugin(), () -> {
                                ClaimResponse response = manager.createClaim(max, min, player.getUniqueId());

                                if (response.isStatus()) {
                                    ((Claim) response.getClaim()).addContribution(player.getUniqueId(), area); //Contribution tracking

                                    player.sendMessage(ChatColor.GREEN + "Claim has been successfully created.");

                                    VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
                                    group.removeAllVisuals();

                                    BaseVisual visual = visualizationManager.getProvider().spawnClaimVisual(VisualColor.GREEN, group, response.getClaim(), player.getLocation().getBlockY() - 1);
                                    visual.spawn();

                                    visualizationManager.despawnAfter(visual, 5);

                                    cleanup(player.getUniqueId(), false);
                                } else {
                                    player.sendMessage(ChatColor.RED + "Error creating claim");
                                    cleanup(player.getUniqueId(), true);
                                }
                            });
                        });
                    }
                    return "";
                },
                p -> {
                    cleanup(player.getUniqueId(), true);
                    return "";
                }).open();
    }

    @Override
    public void cleanup(UUID player, boolean visuals) {
        commandManager.forceCleanup(player, visuals);
    }
}
