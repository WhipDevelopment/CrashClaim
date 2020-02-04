package net.crashcraft.whipclaim.events;

import net.crashcraft.whipclaim.config.ValueConfig;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.permissions.PermissionHelper;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import net.crashcraft.whipclaim.permissions.PermissionSetup;
import net.crashcraft.whipclaim.visualize.VisualizationManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class WorldListener implements Listener {
     private PermissionHelper helper;
     private PermissionSetup perms;
     private VisualizationManager visuals;

    public WorldListener(ClaimDataManager manager, VisualizationManager visuals){
        this.perms = manager.getPermissionSetup();
        this.visuals = visuals;
        this.helper = PermissionHelper.getPermissionHelper();
    }

    @EventHandler
    public void onProjectileHitEvent(EntityInteractEvent e){
        if (ValueConfig.DISABLED_WORLDS.contains(e.getBlock().getWorld().getUID())){
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
                        visuals.sendAlert(player, "You do not have permission to interact in this claim.");
                    }
                }
            } else if (helper.hasPermission(location, PermissionRoute.INTERACTIONS)){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent e) {
        if (ValueConfig.DISABLED_WORLDS.contains(e.getBlock().getWorld().getUID())){
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
                visuals.sendAlert(player, "You do not have permission to interact in this claim.");
            }
        } else if (e.getEntity() instanceof WitherSkull
                && !helper.hasPermission(location, PermissionRoute.EXPLOSIONS)){
            e.setCancelled(true);
        } else {
            ArrayList<Player> players = new ArrayList<>();
            for (Entity entity : e.getEntity().getPassengers()) {
                if (entity instanceof Player){
                    if (helper.hasPermission(entity.getUniqueId(), location, PermissionRoute.INTERACTIONS)) {
                        return;
                    }
                    players.add((Player) entity);
                }
            }

            for (Player player : players){
                e.setCancelled(true);
                visuals.sendAlert(player, "You do not have permission to interact in this claim.");
            }
        }
    }

    @EventHandler
    public void onBlockIgniteEvent(BlockIgniteEvent e){
        if (ValueConfig.DISABLED_WORLDS.contains(e.getBlock().getWorld().getUID())){
            return;
        }

        Location location = e.getBlock().getLocation();
        if (e.getPlayer() != null){
            if (!helper.hasPermission(e.getPlayer().getUniqueId(), location, PermissionRoute.BUILD)) {
                e.setCancelled(true);
                visuals.sendAlert(e.getPlayer(), "You do not have permission to build in this claim.");
            }
        } else if (helper.hasPermission(location, PermissionRoute.BUILD)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplodeEvent(BlockExplodeEvent e){
        if (ValueConfig.DISABLED_WORLDS.contains(e.getBlock().getWorld().getUID())){
            return;
        }

        e.blockList().removeAll(processExplosion(e.blockList()));
    }

    @EventHandler
    public void onBlockExplodeEvent(EntityExplodeEvent e){
        if (ValueConfig.DISABLED_WORLDS.contains(e.getLocation().getWorld().getUID())){
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
