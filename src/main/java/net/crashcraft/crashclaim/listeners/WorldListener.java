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
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.*;

public class WorldListener implements Listener {
     private final PermissionHelper helper;
     private final PermissionSetup perms;
     private final VisualizationManager visuals;
     private final ClaimDataManager manager;

     private final Set<Sheep> trackSheepRegrow; // Tracker to block sheep from regrowing after block cancelled

    public WorldListener(ClaimDataManager manager, VisualizationManager visuals){
        this.manager = manager;
        this.perms = manager.getPermissionSetup();
        this.visuals = visuals;
        this.helper = PermissionHelper.getPermissionHelper();

        this.trackSheepRegrow = new HashSet<>();
    }

    @EventHandler
    public void onSheepRegrowWoolEvent(SheepRegrowWoolEvent e){
        if (trackSheepRegrow.remove(e.getEntity())){ // API workaround
            e.setCancelled(true);
        }
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

        if (e.getEntity() instanceof Projectile && ((Projectile) e.getEntity()).getShooter() instanceof Player player){
            Location location = e.getBlock().getLocation();

            if (!helper.hasPermission(player.getUniqueId(), location, PermissionRoute.INTERACTIONS)) {
                Material material = e.getBlock().getType();
                if (material.isInteractable() || perms.getExtraInteractables().contains(material)) {
                    e.setCancelled(true);
                    visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__INTERACTION.getMessage(player));
                }
            }
        } else {
            // Entities other than projectiles where shooter is a player are handled by entity grief
            if (!helper.hasPermission(e.getBlock().getLocation(), PermissionRoute.ENTITY_GRIEF)){
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

        if (e.getEntity() instanceof Player) {
            if (!helper.hasPermission(e.getEntity().getUniqueId(), location, PermissionRoute.INTERACTIONS)) {
                e.setCancelled(true);
                Player player = (Player) e.getEntity();
                visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__INTERACTION.getMessage(player));
            }
        } else {
            if (e.getEntity() instanceof Arrow && ((Arrow) e.getEntity()).getShooter() instanceof Player player) {
                if (e.getBlock().getType().equals(Material.TNT)
                        && !helper.hasPermission(player.getUniqueId(), location, PermissionRoute.INTERACTIONS)){
                    e.setCancelled(true);
                    visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__INTERACTION.getMessage(player));
                }
            } else if (e.getEntity() instanceof Sheep sheep && !helper.hasPermission(location, PermissionRoute.ENTITY_GRIEF)) {
                e.setCancelled(true);

                if (e.getBlock().getBlockData().getMaterial() == Material.GRASS_BLOCK && e.getTo() == Material.DIRT) { // Stupid api workaround for sheep eat
                    trackSheepRegrow.add(sheep);
                }
            } else if (
                    (e.getEntity() instanceof Enderman
                            || (e.getEntity() instanceof WitherSkull) // Wither skulls should be the one exploding but some versions the api is wrong, TODO check if explosions are handled correctly
                            || (e.getEntity() instanceof Wither)) // Handles wither block breaks other than explosions
                    && !helper.hasPermission(location, PermissionRoute.ENTITY_GRIEF)
            ) {
                e.setCancelled(true);
            } else if (e.getEntity() instanceof FallingBlock){
                // Don't handle this for now, maybe need to handle
            } else {
                for (Entity entity : e.getEntity().getPassengers()) { // Used for boats and horses with player as passenger
                    if (entity instanceof Player player){
                        if (helper.hasPermission(entity.getUniqueId(), location, PermissionRoute.INTERACTIONS)) {
                            return;
                        }

                        e.setCancelled(true);
                        visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__INTERACTION.getMessage(player));
                        return;
                    }
                }

                //No player with permissions was a passenger or no passengers
                if (!helper.hasPermission(location, PermissionRoute.ENTITY_GRIEF)) {
                    e.setCancelled(true);
                }
            }
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

    private List<Block> processExplosion(List<Block> blocks ){
        ArrayList<Block> removeAlBlocks = new ArrayList<>();
        for (Block block : blocks){
            if (!helper.hasPermission(block.getLocation(), PermissionRoute.EXPLOSIONS)){
                removeAlBlocks.add(block);
            }
        }

        return removeAlBlocks;
    }
}
