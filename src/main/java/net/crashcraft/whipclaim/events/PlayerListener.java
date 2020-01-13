package net.crashcraft.whipclaim.events;

import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.permissions.PermissionHelper;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import net.crashcraft.whipclaim.permissions.PermissionSetup;
import net.crashcraft.whipclaim.visualize.VisualizationManager;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

import java.util.UUID;

public class PlayerListener implements Listener {
    private PermissionHelper helper;
    private PermissionSetup perms;
    private VisualizationManager visuals;
    private ClaimDataManager manager;

    public PlayerListener(ClaimDataManager manager, VisualizationManager visuals){
        this.manager = manager;
        this.perms = manager.getPermissionSetup();
        this.visuals = visuals;
        this.helper = PermissionHelper.getPermissionHelper();
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent e){
        Player player = e.getPlayer();

        if (e.getClickedBlock() == null)
            return;

        Location location = e.getClickedBlock().getLocation();

        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && perms.getHeldItemInteraction().contains(e.getItem().getType())) {
            if (!helper.hasPermission(player.getUniqueId(), location, PermissionRoute.ENTITIES)){
                e.setCancelled(true);
                visuals.sendAlert(player, "You do not have permission to interact with entities in this claim");
            }
            return;
        }

        if (!e.getClickedBlock().getType().isInteractable()
                        && !perms.getExtraInteractables().contains(e.getClickedBlock().getType())
                        || perms.getUntrackedBlocks().contains(e.getClickedBlock().getType()))
            return;



        if (e.getClickedBlock().getState() instanceof Container){
            if (helper.hasPermission(player.getUniqueId(), location, e.getClickedBlock().getType())){
                return;
            }

            e.setCancelled(true);
            visuals.sendAlert(player, "You do not have permission to open containers in this claim.");
        } else if (!helper.hasPermission(player.getUniqueId(), location, PermissionRoute.INTERACTIONS)){
            e.setCancelled(true);
            visuals.sendAlert(player, "You do not have permission to interact in this claim.");
        }
    }


    @SuppressWarnings("Duplicates")
    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent e){
        final int fromX = e.getFrom().getBlockX();
        final int fromZ = e.getFrom().getBlockZ();

        if (e.getTo() == null)
            return;

        final int toX = e.getTo().getBlockX();
        final int toZ = e.getTo().getBlockZ();
        final UUID world = e.getTo().getWorld().getUID();
        final Player player = e.getPlayer();

        if(!player.isGliding() &&
                !player.isFlying() &&
                (fromX != toX || fromZ != toZ)) {

            /*
                If the sub claim has no entry or exit set, it is treated as if it was not there
                If the sub claim has anything set then its treated like its own claim

                Play entry before exit

                Parent claim needs to have entry null to play sub claim exit on enter to parent claim
             */


            Claim toClaim = manager.getClaim(toX, toZ, world);
            Claim fromClaim = manager.getClaim(fromX, fromZ, world);

            BaseClaim to = null;
            BaseClaim from = null;

            if (toClaim != null) {
                SubClaim toSubClaim = toClaim.getSubClaim(toX, toZ);
                if (toSubClaim != null) {
                    if (toSubClaim.getEntryMessage() != null || toSubClaim.getExitMessage() != null) {
                        to = toSubClaim;
                    } else {
                        to = toClaim;
                    }
                } else {
                    to = toClaim;
                }
            }

            if (fromClaim != null) {
                SubClaim fromSubClaim = fromClaim.getSubClaim(fromX, fromZ);
                if (fromSubClaim != null) {
                    if (fromSubClaim.getEntryMessage() != null || fromSubClaim.getExitMessage() != null) {
                        from = fromSubClaim;
                    } else {
                        from = fromClaim;
                    }
                } else {
                    from = fromClaim;
                }
            }

            if (to != null){
                if (from != null){
                    if (to.equals(from)){
                        return;
                    }

                    if (to.getEntryMessage() != null){
                        visuals.sendAlert(player, to.getEntryMessage());
                    } else if (from.getEntryMessage() != null){
                        visuals.sendAlert(player, from.getExitMessage());
                    }
                } else {
                    if (to.getEntryMessage() != null){
                        visuals.sendAlert(player, to.getEntryMessage());
                    }
                }
            } else {
                if (from != null){
                    if (from.getExitMessage() != null){
                        visuals.sendAlert(player, from.getExitMessage());
                    }
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e){
        if (e.getEntity() instanceof Player)
            return;

        if (e.getDamager() instanceof Projectile){
            Projectile arrow = (Projectile) e.getDamager();
            Location location = arrow.getLocation();
            if (arrow.getShooter() instanceof Player){
                Player player = (Player) arrow.getShooter();

                if (!helper.hasPermission(player.getUniqueId(), location, PermissionRoute.ENTITIES)){
                    e.setCancelled(true);
                    visuals.sendAlert(player, "You do not have permission to interact with entities in this claim");
                }
            } else {
                if (!helper.hasPermission(location, PermissionRoute.ENTITIES)){
                    e.setCancelled(true);
                }
            }
        } else if (e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            if (!helper.hasPermission(player.getUniqueId(), e.getEntity().getLocation(), PermissionRoute.ENTITIES)){
                e.setCancelled(true);
                visuals.sendAlert(player, "You do not have permission to interact with entities in this claim");
            }
        } else if (e.getDamager() instanceof TNTPrimed){
            if (!helper.hasPermission(e.getEntity().getLocation(), PermissionRoute.EXPLOSIONS)){
                e.setCancelled(true);
            }
        } else if (e.getDamager() instanceof Creeper){
            if (!helper.hasPermission(e.getEntity().getLocation(), PermissionRoute.EXPLOSIONS)){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHangingBreak(HangingBreakEvent event) {
        if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
            if (!helper.hasPermission(event.getEntity().getLocation(), PermissionRoute.EXPLOSIONS)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onVehicleDestroyEvent(VehicleDamageEvent e){
        if (e.getAttacker() instanceof Player) {
            Player player = (Player) e.getAttacker();
            if (!helper.hasPermission(player.getUniqueId(), e.getVehicle().getLocation(), PermissionRoute.ENTITIES)){
                e.setCancelled(true);
                visuals.sendAlert(player, "You do not have permission to interact with entities in this claim");
            }
        } else if (e.getAttacker() instanceof TNTPrimed){
            if (!helper.hasPermission(e.getAttacker().getLocation(), PermissionRoute.EXPLOSIONS)){
                e.setCancelled(true);
            }
        } else if (e.getAttacker() instanceof Creeper){
            if (!helper.hasPermission(e.getAttacker().getLocation(), PermissionRoute.EXPLOSIONS)){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e){
        Player player = e.getPlayer();
        if (!helper.hasPermission(player.getUniqueId(), e.getRightClicked().getLocation(), PermissionRoute.ENTITIES)){
            e.setCancelled(true);
            visuals.sendAlert(player, "You do not have permission to interact with entities in this claim");
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent e){
        Player player = e.getPlayer();
        if (!helper.hasPermission(player.getUniqueId(), e.getRightClicked().getLocation(), PermissionRoute.ENTITIES)){
            e.setCancelled(true);
            visuals.sendAlert(player, "You do not have permission to interact with entities in this claim");
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerPickupArrowEvent(PlayerPickupArrowEvent e){
        Player player = e.getPlayer();
        if (!helper.hasPermission(player.getUniqueId(), e.getArrow().getLocation(), PermissionRoute.ENTITIES)){
            e.setCancelled(true);
            visuals.sendAlert(player, "You do not have permission to interact with entities in this claim");
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onBlockBreakEvent(BlockBreakEvent e){
        Player player = e.getPlayer();
        if (!helper.hasPermission(player.getUniqueId(), e.getBlock().getLocation(), PermissionRoute.BUILD)){
            e.setCancelled(true);
            visuals.sendAlert(player, "You do not have permission to build in this claim");
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onBlockPlaceEvent(BlockPlaceEvent e){
        Player player = e.getPlayer();
        if (!helper.hasPermission(player.getUniqueId(), e.getBlock().getLocation(), PermissionRoute.BUILD)){
            e.setCancelled(true);
            visuals.sendAlert(player, "You do not have permission to build in this claim");
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent e){
        Player player = e.getPlayer();
        if (!helper.hasPermission(player.getUniqueId(), e.getBlockClicked().getLocation(), PermissionRoute.BUILD)){
            e.setCancelled(true);
            visuals.sendAlert(player, "You do not have permission to build in this claim");
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerBucketFillEvent(PlayerBucketFillEvent e){
        Player player = e.getPlayer();
        if (!helper.hasPermission(player.getUniqueId(), e.getBlockClicked().getLocation(), PermissionRoute.BUILD)){
            e.setCancelled(true);
            visuals.sendAlert(player, "You do not have permission to build in this claim");
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onBlockIgniteEvent(BlockIgniteEvent e){
        if (e.getIgnitingEntity() instanceof Player &&
                !helper.hasPermission(e.getPlayer().getUniqueId(), e.getBlock().getLocation(), PermissionRoute.BUILD)) {
            e.setCancelled(true);
            visuals.sendAlert(e.getPlayer(), "You do not have permission to build in this claim");
        }
    }
}
