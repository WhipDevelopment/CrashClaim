package net.crashcraft.crashclaim.listeners;

import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.permissions.PermissionSetup;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorldListener implements Listener {
     private final PermissionHelper helper;
     private final PermissionSetup perms;
     private final VisualizationManager visuals;
     private final ClaimDataManager manager;

    public WorldListener(ClaimDataManager manager, VisualizationManager visuals){
        this.manager = manager;
        this.perms = manager.getPermissionSetup();
        this.visuals = visuals;
        this.helper = PermissionHelper.getPermissionHelper();
    }

    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onStructureGrowEvent(StructureGrowEvent e){
        if (GlobalConfig.disabled_worlds.contains(e.getWorld().getUID())){
            return;
        }

        ArrayList<BlockState> removeAlBlocks = new ArrayList<>();
        if (e.getPlayer() != null){
            UUID uuid = e.getPlayer().getUniqueId();

            if (!helper.hasPermission(uuid, e.getLocation(), PermissionRoute.BUILD)) { // Fixes Mushrooms and trees growing into claims.
                visuals.sendAlert(e.getPlayer(), Localization.ALERT__NO_PERMISSIONS__BUILD.getMessage(e.getPlayer()));
                e.setCancelled(true);
                return; // Unable to grow initial block so whole event needs to be canceled.
            }

            for (BlockState state : e.getBlocks()){
                if (!helper.hasPermission(uuid, state.getLocation(), PermissionRoute.BUILD)){ // Fixes Mushrooms and trees growing into claims.
                    removeAlBlocks.add(state);
                }
            }

            if (removeAlBlocks.size() > 0){
                visuals.sendAlert(e.getPlayer(), Localization.ALERT__NO_PERMISSIONS__BUILD.getMessage(e.getPlayer()));
            }
        } else {
            Location baseBlock = e.getLocation();
            Claim baseClaim = manager.getClaim(baseBlock); // Claim of base block, should be grow source.

            if (baseClaim == null) {
                // If there is no claim at the base of the, for example tree (sapling) then we check every block
                for (BlockState state : e.getBlocks()) {
                    if (!helper.hasPermission(state.getLocation(), PermissionRoute.BUILD)) { // Fixes Mushrooms and trees growing into claims.
                        removeAlBlocks.add(state);
                    }
                }
            } else {
                for (BlockState state : e.getBlocks()) {
                    Location blockLocation = state.getLocation();
                    Claim blockClaim = manager.getClaim(blockLocation);

                    if (blockClaim == null || blockClaim == baseClaim){
                        continue; // Skip as this should only be hit under a natural grow event.
                    }

                    if (!helper.hasPermission(state.getLocation(), PermissionRoute.BUILD)) { // Final check if claims do not match, check permission as we want to prevent growing into another claim
                        removeAlBlocks.add(state);
                    }
                }
            }
        }

        e.getBlocks().removeAll(removeAlBlocks);

        if (e.getBlocks().isEmpty()){
            e.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProjectileHitEvent(EntityInteractEvent e){
        if (GlobalConfig.disabled_worlds.contains(e.getBlock().getWorld().getUID())){
            return;
        }

        if (e.getEntity() instanceof Projectile){
            Location location = e.getBlock().getLocation();
            if (((Projectile) e.getEntity()).getShooter() instanceof Player) {
                Player player = (Player) ((Projectile) e.getEntity()).getShooter();

                if (player == null)
                    return;

                if (!helper.hasPermission(player.getUniqueId(), location, PermissionRoute.INTERACTIONS)) {
                    Material material = e.getBlock().getType();
                    if (material.isInteractable() || perms.getExtraInteractables().contains(material)) {
                        e.setCancelled(true);
                        visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__INTERACTION.getMessage(player));
                    }
                }
            } else if (!helper.hasPermission(location, PermissionRoute.INTERACTIONS)){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent e) {
        if (GlobalConfig.disabled_worlds.contains(e.getBlock().getWorld().getUID())){
            return;
        }

        Location location = e.getBlock().getLocation();
        if (e.getEntity() instanceof Arrow && ((Arrow) e.getEntity()).getShooter() instanceof Player) {
            Player player = (Player) ((Arrow) e.getEntity()).getShooter();

            if (player == null)
                return;

            if (e.getBlock().getType().equals(Material.TNT)
                    && !helper.hasPermission(player.getUniqueId(), location, PermissionRoute.INTERACTIONS)){
                e.setCancelled(true);
                visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__INTERACTION.getMessage(player));
            }
        } else if ((e.getEntity() instanceof WitherSkull || e.getEntity() instanceof Wither)
                && !helper.hasPermission(location, PermissionRoute.EXPLOSIONS)) {
            e.setCancelled(true);
        } else if (e.getEntity() instanceof Player
                && !helper.hasPermission(e.getEntity().getUniqueId(), location, PermissionRoute.INTERACTIONS)) {
            e.setCancelled(true);
            Player player = (Player) e.getEntity();
            visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__INTERACTION.getMessage(player));
        } else if ((e.getEntity() instanceof Sheep || e.getEntity() instanceof Enderman)
                && !helper.hasPermission(location, PermissionRoute.BUILD)) {
            e.setCancelled(true);
        } else {
            for (Entity entity : e.getEntity().getPassengers()) {
                if (entity instanceof Player){
                    if (helper.hasPermission(entity.getUniqueId(), location, PermissionRoute.INTERACTIONS)) {
                        return;
                    }

                    e.setCancelled(true);
                    Player player = (Player) entity;
                    visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__INTERACTION.getMessage(player));
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgniteEvent(BlockIgniteEvent e){
        if (GlobalConfig.disabled_worlds.contains(e.getBlock().getWorld().getUID())){
            return;
        }

        Location location = e.getBlock().getLocation();
        if (e.getPlayer() != null){
            if (!helper.hasPermission(e.getPlayer().getUniqueId(), location, PermissionRoute.BUILD)) {
                e.setCancelled(true);
                visuals.sendAlert(e.getPlayer(), Localization.ALERT__NO_PERMISSIONS__BUILD.getMessage(e.getPlayer()));
            }
        } else if (!helper.hasPermission(location, PermissionRoute.BUILD)){
            e.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockExplodeEvent(BlockExplodeEvent e){
        if (GlobalConfig.disabled_worlds.contains(e.getBlock().getWorld().getUID())){
            return;
        }

        e.blockList().removeAll(processExplosion(e.blockList()));
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockExplodeEvent(EntityExplodeEvent e){
        if (GlobalConfig.disabled_worlds.contains(e.getLocation().getWorld().getUID())){
            return;
        }

        e.blockList().removeAll(processExplosion(e.blockList()));
    }

    private List<Block> processExplosion(List<Block> blocks){
        ArrayList<Block> removeAlBlocks = new ArrayList<>();
        for (Block block : blocks){
            if (!helper.hasPermission(block.getLocation(), PermissionRoute.EXPLOSIONS)){
                removeAlBlocks.add(block);
            }
        }

        return removeAlBlocks;
    }
}
