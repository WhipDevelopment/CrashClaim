package net.crashcraft.whipclaim.events;

import dev.whip.crashclaim.ClaimManager;
import dev.whip.crashclaim.ClaimModeManager;
import dev.whip.crashclaim.StaticValueLookup;
import dev.whip.crashclaim.ClaimData;
import dev.whip.crashclaim.ClaimPermissionData;
import dev.whip.crashclaim.objects.ClaimObject;
import dev.whip.crashclaim.objects.ClaimPermsObject;
import dev.whip.crashclaim.user.User;
import dev.whip.crashclaim.user.UserCache;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;

public class PlayerListener implements Listener {
    final private ClaimData claimData = ClaimData.getInstance();

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerInteractEvent(PlayerInteractEvent e){
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getHand().equals(EquipmentSlot.HAND))
            ClaimModeManager.clickBlock(UserCache.getUser(e.getPlayer()), e.getClickedBlock().getLocation());

        if (e.getClickedBlock() == null || (!e.getClickedBlock().getType().isInteractable()
                && !StaticValueLookup.getInteractableMaterials().contains(e.getClickedBlock().getType())))
            return;

        Location location = e.getClickedBlock().getLocation();
        Material material = e.getClickedBlock().getType();
        ClaimPermsObject permsObject = ClaimPermissionData.lookup(location.getBlockX(), location.getBlockZ(), UserCache.getUser(e.getPlayer()));

        if (permsObject == null)
            return;

        if (!permsObject.getEnabledContainers().contains(material)) {
            switch (material) {
                case DROPPER:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case CHEST:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case TRAPPED_CHEST:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case FURNACE:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case HOPPER:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case DISPENSER:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case WHITE_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case ORANGE_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case MAGENTA_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case LIGHT_BLUE_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case YELLOW_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case LIME_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case PINK_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case LIGHT_GRAY_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case RED_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case PURPLE_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case GREEN_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case GRAY_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case CYAN_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case BROWN_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case BLUE_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case BLACK_SHULKER_BOX:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case ANVIL:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case CHIPPED_ANVIL:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case DAMAGED_ANVIL:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case BARREL:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case LECTERN:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
                case BLAST_FURNACE:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to open containers in this claim");
                    return;
            }
        } else {
            return;
        }

        if (!permsObject.isInteractions()) {
            switch (material) {
                case ENDER_CHEST:
                    return;
                case CRAFTING_TABLE:
                    return;
                case ENCHANTING_TABLE:
                    return;
                case LOOM:
                    return;
                case STONECUTTER:
                    return;
                case FLETCHING_TABLE:
                    return;
                case SMITHING_TABLE:
                    return;
                default:
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to interact in this claim");
                    break;
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerMoveEvent(PlayerMoveEvent e){
        final int fromX = e.getFrom().getBlockX();
        final int fromZ = e.getFrom().getBlockZ();
        final int toX = e.getTo().getBlockX();
        final int toZ = e.getTo().getBlockZ();
        final String world = e.getTo().getWorld().getName();

        if(!e.getPlayer().isGliding() &&
                !e.getPlayer().isFlying() &&
                (fromX != toX || fromZ != toZ)) {

            ClaimObject toClaim = claimData.getClaimAtLocation(toX, toZ, world);
            ClaimObject fromClaim = claimData.getClaimAtLocation(fromX, fromZ, world);

            if (toClaim != null) {
                if (toClaim != fromClaim && !toClaim.getEnterMessage().equals(""))
                    ClaimManager.sendMessageTitle(e.getPlayer(), 1, toClaim.getColor(UserCache.getUser(e.getPlayer()).getUserID()), toClaim.getEnterMessage());
            } else if (fromClaim != null && !fromClaim.getEnterMessage().equals("")){
                ClaimManager.sendMessageTitle(e.getPlayer(), 1, fromClaim.getColor(UserCache.getUser(e.getPlayer()).getUserID()), fromClaim.getExitMessage());
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e){
        if (e.getEntity() instanceof Player)
            return;

        if (e.getDamager() instanceof Projectile){
            Projectile arrow = (Projectile) e.getDamager();
            Location location = arrow.getLocation();
            if (arrow.getShooter() instanceof Player){
                Player player = (Player) arrow.getShooter();
                if (!ClaimPermissionData.lookup(location.getBlockX(), location.getBlockZ(), UserCache.getUser(player)).isEntities()) {
                    e.setCancelled(true);
                    ClaimManager.sendMessageTitle(player, 1, ChatColor.RED, "You do not have permission to interact with entities in this claim");
                }
            } else {
                if (!ClaimPermissionData.getGlobalPerms(location.getBlockX(), location.getBlockZ(), location.getWorld().getName()).isEntities()) {
                    e.setCancelled(true);
                }
            }
        } else if (e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            Location location = e.getEntity().getLocation();
            if (!ClaimPermissionData.lookup(location.getBlockX(), location.getBlockZ(), UserCache.getUser(player)).isEntities()) {
                e.setCancelled(true);
                ClaimManager.sendMessageTitle(player, 1, ChatColor.RED, "You do not have permission to interact with entities in this claim");
            }
        } else if (e.getDamager() instanceof TNTPrimed){
            Location location = e.getEntity().getLocation();
            if (!ClaimPermissionData.getGlobalPerms(location.getBlockX(), location.getBlockZ(), location.getWorld().getName()).isExplosions()) {
                e.setCancelled(true);
            }
        } else if (e.getDamager() instanceof Creeper){
            Location location = e.getEntity().getLocation();
            if (!ClaimPermissionData.getGlobalPerms(location.getBlockX(), location.getBlockZ(), location.getWorld().getName()).isExplosions()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHangingBreak(HangingBreakEvent event) {
        if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
            Location location = event.getEntity().getLocation();
            if (!ClaimPermissionData.getGlobalPerms(location.getBlockX(), location.getBlockZ(), location.getWorld().getName()).isExplosions()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onVehicleDestroyEvent(VehicleDamageEvent e){
        if (e.getAttacker() instanceof Player) {
            Player player = (Player) e.getAttacker();
            Location location = e.getVehicle().getLocation();
            if (!ClaimPermissionData.lookup(location.getBlockX(), location.getBlockZ(), UserCache.getUser(player)).isEntities()) {
                e.setCancelled(true);
                ClaimManager.sendMessageTitle(player, 1, ChatColor.RED, "You do not have permission to interact with entities in this claim");
            }
        } else if (e.getAttacker() instanceof TNTPrimed){
            Location location = e.getAttacker().getLocation();
            if (!ClaimPermissionData.getGlobalPerms(location.getBlockX(), location.getBlockZ(), location.getWorld().getName()).isExplosions()) {
                e.setCancelled(true);
            }
        } else if (e.getAttacker() instanceof Creeper){
            Location location = e.getAttacker().getLocation();
            if (!ClaimPermissionData.getGlobalPerms(location.getBlockX(), location.getBlockZ(), location.getWorld().getName()).isExplosions()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e){
        Location location = e.getRightClicked().getLocation();
        if (!ClaimPermissionData.lookup(location.getBlockX(), location.getBlockZ(), UserCache.getUser(e.getPlayer())).isEntities()) {
            e.setCancelled(true);
            ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to interact with entities in this claim");
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent e){
        Location location = e.getRightClicked().getLocation();
        if (!ClaimPermissionData.lookup(location.getBlockX(), location.getBlockZ(), UserCache.getUser(e.getPlayer())).isEntities()) {
            e.setCancelled(true);
            ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to interact with entities in this claim");
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerPickupArrowEvent(PlayerPickupArrowEvent e){
        Location location = e.getArrow().getLocation();
        if (!ClaimPermissionData.lookup(location.getBlockX(), location.getBlockZ(), UserCache.getUser(e.getPlayer())).isEntities()) {
            e.setCancelled(true);
            ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to interact with entities in this claim");
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onBlockBreakEvent(BlockBreakEvent e){
        Location location = e.getBlock().getLocation();
        if (!ClaimPermissionData.lookup(location.getBlockX(), location.getBlockZ(), UserCache.getUser(e.getPlayer())).isBuild()) {
            e.setCancelled(true);
            ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to build in this claim");
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onBlockPlaceEvent(BlockPlaceEvent e){
        Location location = e.getBlock().getLocation();
        if (!ClaimPermissionData.lookup(location.getBlockX(), location.getBlockZ(), UserCache.getUser(e.getPlayer())).isBuild()) {
            e.setCancelled(true);
            ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to build in this claim");
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent e){
        Location location = e.getBlockClicked().getLocation();
        if (!ClaimPermissionData.lookup(location.getBlockX(), location.getBlockZ(), UserCache.getUser(e.getPlayer())).isBuild()) {
            e.setCancelled(true);
            ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to build in this claim");
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerBucketFillEvent(PlayerBucketFillEvent e){
        Location location = e.getBlockClicked().getLocation();
        if (!ClaimPermissionData.lookup(location.getBlockX(), location.getBlockZ(), UserCache.getUser(e.getPlayer())).isBuild()) {
            e.setCancelled(true);
            ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to build in this claim");
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onBlockIgniteEvent(BlockIgniteEvent e){
        Location location = e.getBlock().getLocation();
        if (e.getIgnitingEntity() instanceof Player &&
                !ClaimPermissionData.lookup(location.getBlockX(), location.getBlockZ(), UserCache.getUser(e.getPlayer())).isBuild()) {
            e.setCancelled(true);
            ClaimManager.sendMessageTitle(e.getPlayer(), 1, ChatColor.RED, "You do not have permission to build in this claim");
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e){
        User user = UserCache.getUser(e.getPlayer());
        user.onJoin(e);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e){
        ClaimModeManager.playerLeaveCleanup(UserCache.getUser(e.getPlayer()));
    }
}
