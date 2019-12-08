package net.crashcraft.whipclaim.commands.modes;

import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.data.*;
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
    private ModeCommand command;

    private HashMap<UUID, Claim> edditingMap;
    private HashMap<UUID, Location> clickMap;
    private HashMap<UUID, SubClaim> resizingMap;

    public SubClaimCommand(ClaimDataManager manager, VisualizationManager visualizationManager, ModeCommand command){
        this.manager = manager;
        this.visualizationManager = visualizationManager;
        this.command = command;

        edditingMap = new HashMap<>();
        clickMap = new HashMap<>();
        resizingMap = new HashMap<>();
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
                if (claim.isEditing()){
                    player.sendMessage(ChatColor.RED + "The claim your are attempting to resize is already being resized.");
                    return;
                }

                PermissionGroup group = claim.getPerms();
                PlayerPermissionSet main = group.getPlayerPermissionSet(uuid);

                if (main != null && PermissionRouter.getLayeredPermission(group.getPermissionSet(), main, PermissionRoute.MODIFY_CLAIM) == PermState.ENABLED){
                    edditingMap.put(uuid, claim);

                    claim.setEditing(true);

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

        if (resizingMap.containsKey(uuid)) {
            Location loc1 = clickMap.get(uuid);
            SubClaim subClaim = resizingMap.get(uuid);

            ErrorType error = manager.resizeSubClaim(subClaim, loc1.getBlockX(), loc1.getBlockZ(), location.getBlockX(), location.getBlockZ(),
                    (arr) -> {
                        //TODO  Do payments in here
                        return ErrorType.NONE;
                    });

            switch (error){
                case TOO_SMALL:
                    player.sendMessage(ChatColor.RED + "A claim has to be at least a 5x5");
                    cleanup(uuid, true);
                    break;
                case CANNOT_FLIP_ON_RESIZE:
                    player.sendMessage(ChatColor.RED + "Claims cannot be flipped, please retry and grab the other edge to expand in this direction");
                    cleanup(player.getUniqueId(), true);
                    break;
                case NONE:
                    player.sendMessage(ChatColor.GREEN + "Claim has been successfully resized");

                    VisualGroup group = visualizationManager.fetchVisualGroup(player, true);

                    visualizationManager.visualizeSuroudningSubClaims(claim, player);

                    for (Visual visual : group.getActiveVisuals()){
                        visualizationManager.despawnAfter(visual, 30);
                    }

                    cleanup(uuid, false);
                    break;
            }
        } else if (clickMap.containsKey(uuid)){
            formSubClaim(claim, player, location, clickMap.get(uuid));
        } else {
            VisualGroup group = visualizationManager.fetchVisualGroup(player, true);

            for (SubClaim subClaim : claim.getSubClaims()){
                if (StaticClaimLogic.isClaimBorder(subClaim.getUpperCornerX(), subClaim.getLowerCornerX(), subClaim.getUpperCornerZ(), subClaim.getLowerCornerZ(),
                        location.getBlockX(), location.getBlockZ())){
                    resizingMap.put(uuid, subClaim);
                    clickMap.put(uuid, location);

                    claim.setEditing(true);

                    player.sendMessage(ChatColor.GREEN + "Click another location to resize the claims. ");

                    for (Visual visual : group.getActiveVisuals()){
                        if (visual instanceof SubClaimVisual){
                            SubClaimVisual subClaimVisual = (SubClaimVisual) visual;
                            if (subClaimVisual.getClaim() instanceof SubClaim){
                                SubClaim visualSubClaim = (SubClaim) subClaimVisual.getClaim();

                                if (visualSubClaim.equals(subClaim)){
                                    subClaimVisual.color(TeamColor.YELLOW);
                                    return;
                                }
                            }
                        }
                    }
                    return;
                }
            }


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
        if (edditingMap.containsKey(uuid)){
            edditingMap.get(uuid).setEditing(false);
        }

        if (resizingMap.containsKey(uuid)){
            resizingMap.get(uuid).getParent().setEditing(false);
        }

        edditingMap.remove(uuid);
        clickMap.remove(uuid);
        resizingMap.remove(uuid);

        command.signalDisabled(uuid);

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
