package net.crashcraft.crashclaim.commands.modes;

import dev.whip.crashutils.menusystem.defaultmenus.ConfirmationMenu;
import net.crashcraft.crashpayment.payment.TransactionType;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.PermState;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.data.*;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.permissions.PermissionRouter;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.VisualType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class ClaimModeCommand implements Listener, ClaimModeProvider {
    private final ClaimDataManager manager;
    private final VisualizationManager visualizationManager;
    private final ModeCommand command;

    private final ArrayList<UUID> enabledMode;
    private final HashMap<UUID, Location> clickMap;
    private final HashMap<UUID, Claim> resizingMap;

    public ClaimModeCommand(ClaimDataManager manager,  VisualizationManager visualizationManage, ModeCommand command){
        this.manager = manager;
        this.visualizationManager = visualizationManage;
        this.command = command;

        enabledMode = new ArrayList<>();
        clickMap = new HashMap<>();
        resizingMap = new HashMap<>();
    }

    public void onClaim(Player player) {
        UUID uuid = player.getUniqueId();

        enabledMode.add(uuid);

        visualizationManager.visualizeSuroudningClaims(player, manager);

        player.sendMessage(ChatColor.GREEN + "Claim mode enabled, click 2 corners to claim.");
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e){
        if (enabledMode.contains(e.getPlayer().getUniqueId()) && e.getHand() != null && e.getHand().equals(EquipmentSlot.HAND) && e.getClickedBlock() != null){
            click(e.getPlayer(), e.getClickedBlock().getLocation());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        cleanup(e.getPlayer().getUniqueId(), true);
    }

    public void customEntityClick(Player player, Location location){
        if (enabledMode.contains(player.getUniqueId())){
            click(player, location);
        }
    }

    public void click(Player player, Location loc1){
        World world = loc1.getWorld();

        if (world == null) {
            cleanup(player.getUniqueId(), true);
            throw new RuntimeException("World was null on claim mode manager click");
        }

        if (GlobalConfig.disabled_worlds.contains(world.getUID())){
            player.sendMessage(ChatColor.RED + "You are unable to claim in this world");
            return;
        }

        Claim claim = manager.getClaim(loc1.getBlockX(), loc1.getBlockZ(), loc1.getWorld().getUID());

        if (claim != null || resizingMap.containsKey(player.getUniqueId())){
            clickedExistingClaim(player, loc1, claim);
            return;
        }

        clickClaim(player, loc1);
    }

    public void clickClaim(Player player, Location location){
        UUID uuid = player.getUniqueId();
        UUID world = player.getWorld().getUID();

        if (!clickMap.containsKey(uuid)){
            clickMap.put(uuid, location);
            player.sendMessage(ChatColor.GREEN + "Click the an opposite corner to form a new claim.");

            VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
            group.removeAllVisualsOfType(VisualType.MARKER);

            visualizationManager.getProvider().spawnMarkerVisual(VisualColor.YELLOW, group, location).spawn();
            return;
        }

        Location loc2 = clickMap.get(uuid);

        Location max = StaticClaimLogic.calculateMaxCorner(loc2, location);
        Location min = StaticClaimLogic.calculateMinCorner(loc2, location);

        if ((max.getBlockX() - min.getBlockX()) < 4 || (max.getBlockZ() - min.getBlockZ()) < 4) {
            player.sendMessage(ChatColor.RED + "A claim has to be at least a 5x5");
            cleanup(player.getUniqueId(),true);
            return;
        }

        if (manager.checkOverLapSurroudningClaims(-1, max.getBlockX(), max.getBlockZ(), min.getBlockX(), min.getBlockZ(), world)){
            player.sendMessage(ChatColor.RED + "You cannot claim over an existing claim.");
            cleanup(player.getUniqueId(), true);
            return;
        }

        int area = ContributionManager.getArea(min.getBlockX(), min.getBlockZ(), max.getBlockX(), max.getBlockZ());

        int price = (int) Math.ceil(area * GlobalConfig.money_per_block);
        CrashClaim.getPlugin().getPayment().makeTransaction(uuid, TransactionType.WITHDRAW, "Claim Purchase", price, (res) -> {
            if (!res.transactionSuccess()){
                player.sendMessage(ChatColor.RED + "You need " + price + " coins to claim that area.");
                cleanup(player.getUniqueId(), true);
                return;
            }

            Bukkit.getScheduler().runTask(CrashClaim.getPlugin(), () -> {
                new ConfirmationMenu(player,
                        "Confirm Claim Creation",
                        ChatColor.GREEN + "The claim creation will cost: " + ChatColor.YELLOW + price,
                        new ArrayList<>(Arrays.asList("Confirm or deny the creation.")),
                        Material.EMERALD,
                        (p, aBoolean) -> {
                            if (aBoolean){
                                ClaimResponse response = manager.createClaim(max, min, uuid);

                                if (response.isStatus()) {
                                    ((Claim) response.getClaim()).addContribution(player.getUniqueId(), area); //Contribution tracking

                                    player.sendMessage(ChatColor.GREEN + "Claim has been successfully created.");

                                    VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
                                    group.removeAllVisuals();

                                    BaseVisual visual = visualizationManager.getProvider().spawnClaimVisual(VisualColor.GREEN, group, response.getClaim(), player.getLocation().getBlockY() - 1);
                                    visual.spawn();

                                    visualizationManager.despawnAfter(visual, 30);

                                    cleanup(player.getUniqueId(), false);
                                } else {
                                    player.sendMessage(ChatColor.RED + "Error creating claim");
                                    cleanup(player.getUniqueId(), true);
                                }
                            }
                            return "";
                        },
                        p -> {
                            cleanup(player.getUniqueId(), true);
                            return "";
                        }).open();
            });
        });
    }

    public void clickedExistingClaim(Player player, Location location, Claim claim){
        UUID uuid = player.getUniqueId();

        if (claim == null || resizingMap.containsKey(uuid)) {    //Already have it in the clickmap
            claim = resizingMap.get(uuid);
            Location loc1 = clickMap.get(uuid);

            if (claim == null)
                return;

            VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
            BaseVisual visual = visualizationManager.getProvider().spawnClaimVisual(VisualColor.GREEN, group, claim, player.getLocation().getBlockY() - 1);

            ErrorType error = manager.resizeClaim(claim, loc1.getBlockX(), loc1.getBlockZ(), location.getBlockX(), location.getBlockZ(), player,
                    aBoolean -> {
                        group.removeAllVisuals();
                        visual.spawn();

                        visualizationManager.despawnAfter(visual, 30);

                        if (aBoolean)
                            player.sendMessage(ChatColor.GREEN + "Claim successfully resized");
                    });
            switch (error){
                case OVERLAP_EXISTING:
                    player.sendMessage(ChatColor.RED + "You cannot overlap other claims.");
                    cleanup(uuid, true);
                    return;
                case TOO_SMALL:
                    player.sendMessage(ChatColor.RED + "A claim has to be at least a 5x5");
                    cleanup(uuid, true);
                    return;
                case CANNOT_FLIP_ON_RESIZE:
                    player.sendMessage(ChatColor.RED + "Claims cannot be flipped, please retry and grab the other edge to expand in this direction");
                    cleanup(uuid, true);
                    return;
                case OVERLAP_EXISTING_SUBCLAIM:
                    player.sendMessage(ChatColor.RED + "Sub claims need to stay inside of the claim when resizing,\n Delete or resize sub claims and try again.");
                    cleanup(uuid, true);
                    return;
                case NONE:
                    cleanup(uuid, false);
                    return;
            }
        }

        if (StaticClaimLogic.isClaimBorder(claim.getMinX(), claim.getMaxX(), claim.getMinZ(),
                claim.getMaxZ(), location.getBlockX(), location.getBlockZ())){
            if (PermissionRouter.getLayeredPermission(claim, null, player.getUniqueId(), PermissionRoute.MODIFY_CLAIM) == PermState.ENABLED
                    || claim.getOwner().equals(uuid)){

                clickMap.put(uuid, location);
                resizingMap.put(uuid, claim);

                claim.setEditing(true);

                VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
                group.removeAllVisuals();

                visualizationManager.getProvider().spawnClaimVisual(VisualColor.GOLD, group, claim, player.getLocation().getBlockY() - 1).spawn();

                player.sendMessage(ChatColor.GREEN + "Click another location to resize the claim.");
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission to modify this claim.");
                cleanup(uuid, true);
            }
        } else {
           player.sendMessage(ChatColor.RED + "You need to click the border of the claim to resize it. Grabbing an edge will move it in that direction, grabbing a corner will move it in both directions relative to the corner.");
        }
    }

    private void cleanup(UUID uuid, boolean visuals){
        clickMap.remove(uuid);
        enabledMode.remove(uuid);

        if (resizingMap.containsKey(uuid)){
            resizingMap.get(uuid).setEditing(false);
            resizingMap.remove(uuid);
        }

        command.signalDisabled(uuid);
    }

    @Override
    public void cleanup(UUID uuid) {
        cleanup(uuid, true);
    }
}
