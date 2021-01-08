package net.crashcraft.crashclaim.commands.modes;

import net.crashcraft.crashclaim.claimobjects.*;
import net.crashcraft.crashclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.crashclaim.data.*;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.permissions.PermissionRouter;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
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
    private final ClaimDataManager manager;
    private final VisualizationManager visualizationManager;
    private final ModeCommand command;

    private final HashMap<UUID, Claim> editingMap;
    private final HashMap<UUID, Location> clickMap;
    private final HashMap<UUID, SubClaim> resizingMap;

    public SubClaimCommand(ClaimDataManager manager, VisualizationManager visualizationManager, ModeCommand command){
        this.manager = manager;
        this.visualizationManager = visualizationManager;
        this.command = command;

        editingMap = new HashMap<>();
        clickMap = new HashMap<>();
        resizingMap = new HashMap<>();
    }

    public void subclaim(Player player){
        UUID uuid = player.getUniqueId();
        if (editingMap.containsKey(uuid)){
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

                if (main != null && PermissionRouter.getLayeredPermission(group.getGlobalPermissionSet(), main, PermissionRoute.MODIFY_CLAIM) == PermState.ENABLED){
                    editingMap.put(uuid, claim);

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
        if (editingMap.containsKey(e.getPlayer().getUniqueId()) && e.getHand() != null && e.getHand().equals(EquipmentSlot.HAND) && e.getClickedBlock() != null){
            click(e.getPlayer(), e.getClickedBlock().getLocation());
        }
    }

    public void clickFakeEntity(Player player, Location location){
        if (editingMap.containsKey(player.getUniqueId())){
            click(player, location);
        }
    }

    private void click(Player player, Location location){
        UUID uuid = player.getUniqueId();
        Claim claim = editingMap.get(player.getUniqueId());

        if (!MathUtils.iskPointCollide(claim.getMinX(), claim.getMinZ(),
                claim.getMaxX(), claim.getMaxZ(), location.getBlockX(), location.getBlockZ())){
            player.sendMessage(ChatColor.RED + "Sub claims can only be formed inside of a parent claim.");
            cleanup(player.getUniqueId(), true);
            return;
        }

        if (resizingMap.containsKey(uuid)) {
            Location loc1 = clickMap.get(uuid);
            SubClaim subClaim = resizingMap.get(uuid);

            ErrorType error = manager.resizeSubClaim(subClaim, loc1.getBlockX(), loc1.getBlockZ(), location.getBlockX(), location.getBlockZ()); //Resize no payment here

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

                    for (BaseVisual visual : group.getActiveVisuals()){
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
                if (StaticClaimLogic.isClaimBorder(subClaim.getMinX(), subClaim.getMaxX(), subClaim.getMinZ(), subClaim.getMaxZ(),
                        location.getBlockX(), location.getBlockZ())){
                    resizingMap.put(uuid, subClaim);
                    clickMap.put(uuid, location);

                    claim.setEditing(true);

                    player.sendMessage(ChatColor.GREEN + "Click another location to resize the claims. ");

                    for (BaseVisual visual : group.getActiveVisuals()){
                        BaseClaim tempClaim = visual.getClaim();
                        if (tempClaim == null)
                            continue;

                        if (tempClaim instanceof SubClaim){
                            SubClaim visualSubClaim = (SubClaim) tempClaim;
                            if (visualSubClaim.equals(subClaim)){
                                group .removeVisual(visual);

                                visualizationManager.getProvider().spawnClaimVisual(VisualColor.YELLOW, group, subClaim, visual.getY()).spawn();
                            }
                        }
                    }
                    return;
                }
            }

            visualizationManager.getProvider().spawnMarkerVisual(VisualColor.YELLOW, group, location.add(0, 1, 0)).spawn();

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

            for (BaseVisual visual : group.getActiveVisuals()){
                visualizationManager.despawnAfter(visual, 30);
            }

            player.sendMessage(ChatColor.GREEN + "Successfully created sub claim.");

            cleanup(player.getUniqueId(), false);
        } else {
            switch (response.getError()){
                case TOO_SMALL:
                    player.sendMessage(ChatColor.RED + "A sub claim needs to be at least a 5x5 area.");
                    break;
                case OUT_OF_BOUNDS:
                    player.sendMessage(ChatColor.RED + "You cannot form a sub claim outside of a parent claim.");
                    break;
                case OVERLAP_EXISTING_SUBCLAIM:
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
        if (editingMap.containsKey(uuid)){
            editingMap.get(uuid).setEditing(false);
        }

        if (resizingMap.containsKey(uuid)){
            resizingMap.get(uuid).getParent().setEditing(false);
        }

        editingMap.remove(uuid);
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
