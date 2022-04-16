package net.crashcraft.crashclaim.listeners;

import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.permissions.PermissionSetup;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

import java.util.List;
import java.util.UUID;

public class PlayerListener implements Listener {
    private final PermissionHelper helper;
    private final PermissionSetup perms;
    private final VisualizationManager visuals;
    private final ClaimDataManager manager;

    public PlayerListener(ClaimDataManager manager, VisualizationManager visuals){
        this.manager = manager;
        this.perms = manager.getPermissionSetup();
        this.visuals = visuals;
        this.helper = PermissionHelper.getPermissionHelper();
    }

    @EventHandler
    public void onPotionSplashEvent(PotionSplashEvent e){
        if (e.getPotion().getShooter() instanceof Player shooter){
            for (LivingEntity livingEntity : e.getAffectedEntities()){
                if (livingEntity instanceof Player player){
                    if (shooter == player || !GlobalConfig.blockPvPInsideClaims || manager.getClaim(player.getLocation()) == null){
                        continue;
                    }

                    e.setCancelled(true);
                    visuals.sendAlert(player, Localization.PVP_DISABLED_INSIDE_CLAIM.getMessage(player));
                } else if (!helper.hasPermission(shooter.getUniqueId(), livingEntity.getLocation(), PermissionRoute.ENTITIES)){
                    e.setCancelled(true);
                    visuals.sendAlert(shooter, Localization.ALERT__NO_PERMISSIONS__ENTITIES.getMessage(shooter));
                }
            }
        } else {
            for (LivingEntity livingEntity : e.getAffectedEntities()){
                if (!(livingEntity instanceof Player) && !helper.hasPermission(livingEntity.getLocation(), PermissionRoute.ENTITIES)){ // If not player
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileThrow(ProjectileLaunchEvent e){
        if (GlobalConfig.disabled_worlds.contains(e.getEntity().getWorld().getUID())){
            return;
        }

        if (e.getEntity().getShooter() instanceof Player player && !helper.hasPermission(player.getUniqueId(), player.getLocation(), PermissionRoute.ENTITIES)){
            e.setCancelled(true);
            visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__ENTITIES.getMessage(player));
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoinEvent(PlayerJoinEvent e){
        if (e.getPlayer().hasPermission("crashclaim.admin.bypassonjoin")){
            helper.getBypassManager().addBypass(e.getPlayer().getUniqueId()); // Enable on join
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingPlaceEvent(HangingPlaceEvent e){
        if (GlobalConfig.disabled_worlds.contains(e.getEntity().getWorld().getUID())){
            return;
        }

        Player player = e.getPlayer();

        if (player == null){
            return;
        }

        if (!helper.hasPermission(player.getUniqueId(), e.getEntity().getLocation(), PermissionRoute.BUILD)) {
            e.setCancelled(true);
            visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__BUILD.getMessage(player));
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent e){
        if (e.getClickedBlock() == null)
            return;

        Player player = e.getPlayer();
        Location location = e.getClickedBlock().getLocation();

        if (GlobalConfig.disabled_worlds.contains(location.getWorld().getUID())){
            return;
        }

        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && perms.getHeldItemInteraction().contains(e.getItem().getType())) {
            if (!helper.hasPermission(player.getUniqueId(), location, PermissionRoute.ENTITIES)){
                e.setCancelled(true);
                visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__ENTITIES.getMessage(player));
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
            visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__CONTAINERS.getMessage(player));
        } else if (!helper.hasPermission(player.getUniqueId(), location, PermissionRoute.INTERACTIONS)){
            e.setCancelled(true);
            visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__INTERACTION.getMessage(player));
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (GlobalConfig.disabled_worlds.contains(event.getBlock().getWorld().getUID())){
            return;
        }

        if (isFullOfLiquid(event.getBlock()) && checkToCancelFluid(event.getBlock(), event.getToBlock())){
            event.setCancelled(true);
        }
    }

    private boolean isFullOfLiquid(Block block){
        return block.isLiquid()
                || (block.getBlockData() instanceof Waterlogged && ((Waterlogged) block.getBlockData()).isWaterlogged());
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonEvent(BlockPistonExtendEvent event){
        if (GlobalConfig.disabled_worlds.contains(event.getBlock().getWorld().getUID())){
            return;
        }

        if (processPistonEvent(event.getDirection(), event.getBlocks(), event.getBlock())){
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonEvent(BlockPistonRetractEvent event){
        if (GlobalConfig.disabled_worlds.contains(event.getBlock().getWorld().getUID())){
            return;
        }

        if (processPistonEvent(event.getDirection(), event.getBlocks(), event.getBlock())){
            event.setCancelled(true);
        }
    }

    private boolean checkToCancelFluid(Block block, Block pushingBlock){
        Claim pistonClaim = manager.getClaim(block.getLocation());
        Claim pushedClaim = manager.getClaim(pushingBlock.getLocation());

        if (pistonClaim == pushedClaim)
            return false;

        //Check both claims if one has it disabled then the event is canceled
        if (pistonClaim != null && !pistonClaim.hasGlobalPermission(PermissionRoute.FLUIDS)) {
            return true;
        }

        // Could combine but this is easier to read
        return pushedClaim != null && !pushedClaim.hasGlobalPermission(PermissionRoute.FLUIDS);
    }

    private boolean checkToCancel(Block block, Block pushingBlock){
        Claim pistonClaim = manager.getClaim(block.getLocation());
        Claim pushedClaim = manager.getClaim(pushingBlock.getLocation());

        if (pistonClaim == pushedClaim)
            return false;

        //Check both claims if one has it disabled then the event is canceled
        if (pistonClaim != null && !pistonClaim.hasGlobalPermission(PermissionRoute.PISTONS)) {
            return true;
        }

        // Could combine but this is easier to read
        return pushedClaim != null && !pushedClaim.hasGlobalPermission(PermissionRoute.PISTONS);
    }

    private boolean processPistonEvent(BlockFace direction, List<Block> blocks, Block pistonBlock){
        Block pushed = pistonBlock.getRelative(direction);
        if (blocks.isEmpty()) {
            return checkToCancel(pistonBlock, pushed);
        }

        if (checkToCancel(pistonBlock, pushed)){
            return true;
        }

        for (Block block : blocks){
            if (checkToCancel(block, block.getRelative(direction))){
                return true;
            }
        }
        return false;
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleportEvent(PlayerTeleportEvent event){
        if (GlobalConfig.disabled_worlds.contains(event.getTo().getWorld().getUID())){
            return;
        }

        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.UNKNOWN)){
            return;
        }

        Location location = event.getTo();
        switch (GlobalConfig.teleportCause.get(event.getCause())){
            case 0: //Disable
                return;
            case 1: //Block
                if (!helper.hasPermission(event.getPlayer().getUniqueId(), location, PermissionRoute.TELEPORTATION)){
                    visuals.sendAlert(event.getPlayer(), Localization.ALERT__NO_PERMISSIONS__TELEPORT.getMessage(event.getPlayer()));
                    event.setCancelled(true);
                }
                return;
            case 2: //Relocate
                if (!helper.hasPermission(event.getPlayer().getUniqueId(), location, PermissionRoute.TELEPORTATION)){
                    visuals.sendAlert(event.getPlayer(), Localization.ALERT__NO_PERMISSIONS__TELEPORT_RELOCATE.getMessage(event.getPlayer()));

                    Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
                    if (claim != null) {
                        int distMax = Math.abs(location.getBlockX() - claim.getMaxX());
                        int distMin = Math.abs(location.getBlockX() - claim.getMinX());

                        World world = location.getWorld();
                        if (distMax > distMin) {    //Find closest side
                            event.setTo(new Location(world, claim.getMinX() - 1,
                                    world.getHighestBlockYAt(claim.getMinX() - 1,
                                            location.getBlockZ()), location.getBlockZ()));
                        } else {
                            event.setTo(new Location(world, claim.getMaxX() + 1,
                                    world.getHighestBlockYAt(claim.getMaxX() + 1,
                                            location.getBlockZ()), location.getBlockZ()));
                        }
                    }
                }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent e){
        final int fromX = e.getFrom().getBlockX();
        final int fromZ = e.getFrom().getBlockZ();
        final int toX = e.getTo().getBlockX();
        final int toZ = e.getTo().getBlockZ();
        final UUID world = e.getTo().getWorld().getUID();
        final Player player = e.getPlayer();

        if((GlobalConfig.checkEntryExitWhileFlying || (!player.isGliding() && !player.isFlying())) && (fromX != toX || fromZ != toZ)) {
            if (GlobalConfig.disabled_worlds.contains(world)){
                return;
            }

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
                to = check(toClaim, toSubClaim);
            }

            if (fromClaim != null) {
                SubClaim fromSubClaim = fromClaim.getSubClaim(fromX, fromZ);
                from = check(fromClaim, fromSubClaim);
            }

            if (to != null){
                if (from != null){
                    if (to.equals(from)){
                        return;
                    }

                    if (to.getEntryMessage() != null){
                        visuals.sendAlert(player,to.getParsedEntryMessage());
                    } else if (from.getEntryMessage() != null){
                        visuals.sendAlert(player, from.getParsedExitMessage());
                    }
                } else {
                    if (to.getEntryMessage() != null){
                        visuals.sendAlert(player, to.getParsedEntryMessage());
                    }
                }
            } else {
                if (from != null){
                    if (from.getExitMessage() != null){
                        visuals.sendAlert(player, from.getParsedExitMessage());
                    }
                }
            }
        }
    }

    private BaseClaim check(Claim claim, SubClaim subClaim){
        if (subClaim != null) {
            if (subClaim.getEntryMessage() != null || subClaim.getExitMessage() != null) {
                return subClaim;
            } else {
                return claim;
            }
        } else {
            return claim;
        }
    }

    private boolean inClaim(Player player){
        return manager.getClaim(player.getLocation()) != null;
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e){
        if (GlobalConfig.disabled_worlds.contains(e.getEntity().getWorld().getUID())){
            return;
        }

        // Handle pvp inside claims
        if (e.getEntity() instanceof Player player){
            if (!GlobalConfig.blockPvPInsideClaims){
                return;
            }

            Entity damager = e.getDamager();
            if (damager instanceof Player attacker) {
                if (inClaim(player) || inClaim(attacker)) {
                    e.setCancelled(true);
                    attacker.sendActionBar(Localization.PVP_DISABLED_INSIDE_CLAIM.getMessage(attacker));
                }
            } else if (damager instanceof Projectile) {
                Projectile proj = ((Projectile) e.getDamager());

                if (proj instanceof EnderPearl){
                    return;
                }

                if (proj.getShooter() instanceof Player shooter) {
                    if (inClaim(player) || inClaim(shooter)) {
                        e.setCancelled(true);

                        if (proj instanceof Arrow arrow) { // Remove fire damage
                            if (arrow.getFireTicks() >= 1 && player.getFireTicks() >= 1) {
                                player.setFireTicks(0);
                            }
                        }

                        shooter.sendActionBar(Localization.PVP_DISABLED_INSIDE_CLAIM.getMessage(shooter));
                    }
                }
            }
            return;
        }

        if (e.getDamager() instanceof Projectile arrow){
            Location location = arrow.getLocation();
            if (arrow.getShooter() instanceof Player player){
                if (!helper.hasPermission(player.getUniqueId(), location, PermissionRoute.ENTITIES)){
                    e.setCancelled(true);
                    visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__ENTITIES.getMessage(player));
                }
            } else {
                if (!helper.hasPermission(location, PermissionRoute.ENTITY_GRIEF)){ // Skeletons and other shooters
                    e.setCancelled(true);
                }
            }
        } else if (e.getDamager() instanceof Player player) {
            if (e.getEntity().getType().equals(EntityType.ITEM_FRAME)){ // Special case move item frames into build category, less confusing
                if (!helper.hasPermission(player.getUniqueId(), e.getEntity().getLocation(), PermissionRoute.BUILD)){
                    e.setCancelled(true);
                    visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__BUILD.getMessage(player));
                }
            } else if (!helper.hasPermission(player.getUniqueId(), e.getEntity().getLocation(), PermissionRoute.ENTITIES)){
                e.setCancelled(true);
                visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__ENTITIES.getMessage(player));
            }
        } else if (e.getDamager() instanceof TNTPrimed){
            if (!helper.hasPermission(e.getEntity().getLocation(), PermissionRoute.EXPLOSIONS)){
                e.setCancelled(true);
            }
        } else if (e.getDamager() instanceof Creeper){
            if (!helper.hasPermission(e.getEntity().getLocation(), PermissionRoute.ENTITY_GRIEF)){ // Creeper damage is now moved to this flag
                e.setCancelled(true);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreakByEntityEvent(HangingBreakByEntityEvent event) {
        if (GlobalConfig.disabled_worlds.contains(event.getEntity().getWorld().getUID())){
            return;
        }

        if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
            if (!helper.hasPermission(event.getEntity().getLocation(), PermissionRoute.EXPLOSIONS)){
                event.setCancelled(true);
            }
        } else if (event.getCause() == HangingBreakEvent.RemoveCause.ENTITY){
            if (event.getRemover() instanceof Player player){
                if (!helper.hasPermission(player.getUniqueId(), event.getEntity().getLocation(), PermissionRoute.ENTITIES)){
                    event.setCancelled(true);
                }

                return; // Stop its a player!
            } else if (event.getRemover() instanceof Arrow arrow){
                if (arrow.getShooter() instanceof Player player && !helper.hasPermission(player.getUniqueId(), event.getEntity().getLocation(), PermissionRoute.ENTITIES)){
                    event.setCancelled(true);
                    return;
                }
            }

            if (!helper.hasPermission(event.getEntity().getLocation(), PermissionRoute.ENTITY_GRIEF)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreakEvent(HangingBreakEvent event) {
        if (GlobalConfig.disabled_worlds.contains(event.getEntity().getWorld().getUID())){
            return;
        }

        if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
            if (!helper.hasPermission(event.getEntity().getLocation(), PermissionRoute.EXPLOSIONS)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFertilizeEvent(BlockFertilizeEvent event){
        if (GlobalConfig.disabled_worlds.contains(event.getBlock().getWorld().getUID())){
            return;
        }

        //TODO look into capturing bee fetalization and put into entity_grief

        if (event.getPlayer() == null){ // It was a dispenser
            Location baseLocation = event.getBlock().getLocation();
            Claim baseClaim = manager.getClaim(baseLocation);

            if (baseClaim == null){
                return;
            }

            if (blockFertilizeDispenserCHeck(baseClaim, baseLocation, BlockFace.NORTH)
                || blockFertilizeDispenserCHeck(baseClaim, baseLocation, BlockFace.EAST)
                    || blockFertilizeDispenserCHeck(baseClaim, baseLocation, BlockFace.SOUTH)
                    || blockFertilizeDispenserCHeck(baseClaim, baseLocation, BlockFace.WEST)
                    || blockFertilizeDispenserCHeck(baseClaim, baseLocation, BlockFace.UP)) { // Exclude down because the block has to be placed on something
                return;
            }

            if (!helper.hasPermission(event.getBlock().getLocation(), PermissionRoute.INTERACTIONS)){
                event.setCancelled(true);
            }
        } else if (!helper.hasPermission(event.getPlayer().getUniqueId(), event.getBlock().getLocation(), PermissionRoute.INTERACTIONS)){
            event.setCancelled(true);
            visuals.sendAlert(event.getPlayer(), Localization.ALERT__NO_PERMISSIONS__INTERACTION.getMessage(event.getPlayer()));
        }
    }

    private boolean blockFertilizeDispenserCHeck(Claim baseClaim, Location baseLocation, BlockFace blockFace){
        Block block = baseLocation.add(blockFace.getDirection()).getBlock();

        if (block.getType() == Material.DISPENSER){
            BaseClaim blockClaim = manager.getClaim(block.getLocation());

            if (blockClaim == baseClaim){
                Dispenser dispenser = (Dispenser) block.getState().getBlockData();
                return dispenser.isTriggered();
            }
        }

        return false;
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleDestroyEvent(VehicleDamageEvent e){
        if (GlobalConfig.disabled_worlds.contains(e.getVehicle().getWorld().getUID())){
            return;
        }

        if (e.getAttacker() instanceof Player) {
            Player player = (Player) e.getAttacker();
            if (!helper.hasPermission(player.getUniqueId(), e.getVehicle().getLocation(), PermissionRoute.ENTITIES)){
                e.setCancelled(true);
                visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__ENTITIES.getMessage(player));
            }
        } else if (e.getAttacker() instanceof TNTPrimed){
            if (!helper.hasPermission(e.getAttacker().getLocation(), PermissionRoute.EXPLOSIONS)){
                e.setCancelled(true);
            }
        } else if (e.getAttacker() != null) {
            // Entity other than player or tnt did the damage
            if (!helper.hasPermission(e.getAttacker().getLocation(), PermissionRoute.ENTITY_GRIEF)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e){
        Player player = e.getPlayer();

        if (GlobalConfig.disabled_worlds.contains(player.getWorld().getUID())){
            return;
        }

        if (!helper.hasPermission(player.getUniqueId(), e.getRightClicked().getLocation(), PermissionRoute.ENTITIES)){
            e.setCancelled(true);
            visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__ENTITIES.getMessage(player));
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent e){
        Player player = e.getPlayer();

        if (GlobalConfig.disabled_worlds.contains(player.getWorld().getUID())){
            return;
        }

        if (!helper.hasPermission(player.getUniqueId(), e.getRightClicked().getLocation(), PermissionRoute.ENTITIES)){
            e.setCancelled(true);
            visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__ENTITIES.getMessage(player));
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPickupArrowEvent(PlayerPickupArrowEvent e){
        Player player = e.getPlayer();

        if (GlobalConfig.disabled_worlds.contains(player.getWorld().getUID())){
            return;
        }

        if (!helper.hasPermission(player.getUniqueId(), e.getArrow().getLocation(), PermissionRoute.ENTITIES)){
            e.setCancelled(true);
            visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__ENTITIES.getMessage(player));
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent e){
        Player player = e.getPlayer();

        if (GlobalConfig.disabled_worlds.contains(player.getWorld().getUID())){
            return;
        }

        if (!helper.hasPermission(player.getUniqueId(), e.getBlock().getLocation(), PermissionRoute.BUILD)){
            e.setCancelled(true);
            visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__BUILD.getMessage(player));
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent e){
        Player player = e.getPlayer();

        if (GlobalConfig.disabled_worlds.contains(player.getWorld().getUID())){
            return;
        }

        if (!helper.hasPermission(player.getUniqueId(), e.getBlock().getLocation(), PermissionRoute.BUILD)){
            e.setCancelled(true);
            visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__BUILD.getMessage(player));
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent e){
        Player player = e.getPlayer();

        if (GlobalConfig.disabled_worlds.contains(player.getWorld().getUID())){
            return;
        }

        if (!helper.hasPermission(player.getUniqueId(), e.getBlock().getLocation(), PermissionRoute.BUILD)){
            e.setCancelled(true);
            visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__BUILD.getMessage(player));
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketFillEvent(PlayerBucketFillEvent e){
        Player player = e.getPlayer();

        if (GlobalConfig.disabled_worlds.contains(player.getWorld().getUID())){
            return;
        }

        if (!helper.hasPermission(player.getUniqueId(), e.getBlock().getLocation(), PermissionRoute.BUILD)){
            e.setCancelled(true);
            visuals.sendAlert(player, Localization.ALERT__NO_PERMISSIONS__BUILD.getMessage(player));
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgniteEvent(BlockIgniteEvent e){
        if (GlobalConfig.disabled_worlds.contains(e.getBlock().getWorld().getUID())){
            return;
        }

        if (e.getPlayer() == null){
            // Not a player so it is an entity
            if (!helper.hasPermission(e.getBlock().getLocation(), PermissionRoute.ENTITY_GRIEF)){
                e.setCancelled(true);
            }
        } else {
            if (!helper.hasPermission(e.getPlayer().getUniqueId(), e.getBlock().getLocation(), PermissionRoute.BUILD)) {
                e.setCancelled(true);
                visuals.sendAlert(e.getPlayer(), Localization.ALERT__NO_PERMISSIONS__BUILD.getMessage(e.getPlayer()));
            }
        }
    }
}
