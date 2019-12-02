package net.crashcraft.whipclaim.commands.modes;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.data.ClaimResponse;
import net.crashcraft.whipclaim.data.MathUtils;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import net.crashcraft.whipclaim.permissions.PermissionRouter;
import net.crashcraft.whipclaim.visualize.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.UUID;

public class SubClaimCommand implements Listener, ClaimModeProvider {
    private ClaimDataManager manager;
    private VisualizationManager visualizationManager;

    private HashMap<UUID, Claim> edditingMap;
    private HashMap<UUID, Location> clickMap;

    public SubClaimCommand(ClaimDataManager manager, VisualizationManager visualizationManager){
        this.manager = manager;
        this.visualizationManager = visualizationManager;

        edditingMap = new HashMap<>();
        clickMap = new HashMap<>();
    }

    public void subclaim(Player player){
        UUID uuid = player.getUniqueId();
        if (edditingMap.containsKey(uuid)){
            player.sendMessage(ChatColor.RED + "Sub Claiming mode disabled.");
            cleanup(uuid, true);
        } else {
            Location location = player.getLocation();
            Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), player.getWorld().getUID());
            if (claim != null) {
                PermissionGroup group = claim.getPerms();
                PermissionSet main = group.getPlayerPermissionSet(uuid);

                if (main != null && PermissionRouter.getLayeredPermission(group.getPermissionSet(), main, PermissionRoute.MODIFY_CLAIM) == PermState.ENABLED){
                    edditingMap.put(uuid, claim);

                    visualizationManager.visualizeSuroudningSubClaims(claim, player);

                    player.sendMessage(ChatColor.GREEN + "Sub Claiming mode enabled.");
                } else {
                    player.sendMessage(ChatColor.RED + "You need MODIFY_CLAIM to create sub claims.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You need to be standing in a claim to enable sub claiming mode.");
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e){
        if (edditingMap.containsKey(e.getPlayer().getUniqueId()) && e.getHand() != null && e.getHand().equals(EquipmentSlot.HAND) && e.getClickedBlock() != null){
            click(e.getPlayer(), e.getClickedBlock().getLocation());
        }
    }

    public void clickFakeEntity(Player player, Location location){
        if (edditingMap.containsKey(player.getUniqueId())){
            click(player, location);
        }
    }

    private void click(Player player, Location location){
        UUID uuid = player.getUniqueId();
        Claim claim = edditingMap.get(player.getUniqueId());

        if (!MathUtils.checkPointCollide(claim.getUpperCornerX(), claim.getUpperCornerZ(),
                claim.getLowerCornerX(), claim.getLowerCornerZ(), location.getBlockX(), location.getBlockZ())){
            player.sendMessage(ChatColor.RED + "Sub claims can only be formed inside of a parent claim.");
            cleanup(player.getUniqueId(), true);
            return;
        }

        if (clickMap.containsKey(uuid)){
            formSubClaim(claim, player, location, clickMap.get(uuid));
        } else {
            VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
            MarkerVisual markerVisual = new MarkerVisual(location.add(0, 1, 0));

            group.addVisual(markerVisual);

            markerVisual.spawn();
            markerVisual.color(TeamColor.YELLOW);

            clickMap.put(uuid, location);
            player.sendMessage(ChatColor.GREEN + "Click an opposite corner to form a sub claim");
        }
    }

    private void formSubClaim(Claim claim, Player player, Location loc1, Location loc2){
        ClaimResponse response = manager.createSubClaim(player, claim, loc1, loc2);

        if (response.isStatus()){
            VisualGroup group = visualizationManager.fetchVisualGroup(player, true);

            group.removeAllVisuals();

            visualizationManager.visualizeSuroudningSubClaims(claim, player);

            for (Visual visual : group.getActiveVisuals()){
                visualizationManager.despawnAfter(visual, 30);
            }

            player.sendMessage(ChatColor.GREEN + "Successfully created sub claim.");

            cleanup(player.getUniqueId(), false);
        } else {
            switch (response.getError()){
                case OUT_OF_BOUNDS:
                    player.sendMessage(ChatColor.RED + "You cannot form a sub claim outside of a parent claim.");
                    break;
                case OVERLAP_EXISITNG:
                    player.sendMessage(ChatColor.RED + "You cannot overlap an existing sub claim.");
                    break;
                case GENERIC:
                    player.sendMessage(ChatColor.RED + "There was an error creating the sub claim.");
                    break;
            }
            cleanup(player.getUniqueId(), true);
        }
    }

    private void cleanup(UUID uuid, boolean visuals){
        edditingMap.remove(uuid);
        clickMap.remove(uuid);

        if (visuals) {
            VisualGroup group = visualizationManager.fetchExistingGroup(uuid);
            if (group != null) {
                group.removeAllVisuals();
            }
        }
    }

    @Override
    public void cleanup(UUID uuid) {
        cleanup(uuid, true);
    }
}
