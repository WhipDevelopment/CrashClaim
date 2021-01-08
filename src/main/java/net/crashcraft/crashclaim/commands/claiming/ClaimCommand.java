package net.crashcraft.crashclaim.commands.claiming;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.*;
import net.crashcraft.crashclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.crashclaim.commands.claiming.modes.NewClaimMode;
import net.crashcraft.crashclaim.commands.claiming.modes.NewSubClaimMode;
import net.crashcraft.crashclaim.commands.claiming.modes.ResizeClaimMode;
import net.crashcraft.crashclaim.commands.claiming.modes.ResizeSubClaimMode;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.data.StaticClaimLogic;
import net.crashcraft.crashclaim.listeners.ProtocalListener;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.permissions.PermissionRouter;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.UUID;

public class ClaimCommand extends BaseCommand implements Listener {
    private final ClaimDataManager dataManager;
    private final VisualizationManager visualizationManager;
    private final HashMap<UUID, ClickState> modeMap;
    private final HashMap<UUID, ClaimMode> stateMap;
    private final HashMap<UUID, Claim> claimMap;

    public ClaimCommand(ClaimDataManager dataManager, VisualizationManager visualizationManager){
        this.dataManager = dataManager;
        this.visualizationManager = visualizationManager;
        this.modeMap = new HashMap<>();
        this.stateMap = new HashMap<>();
        this.claimMap = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, CrashClaim.getPlugin());
        new ProtocalListener(visualizationManager.getProtocolManager(), CrashClaim.getPlugin(), this);
    }

    @CommandAlias("claim")
    public void claim(Player player){
        UUID uuid = player.getUniqueId();

        if (modeMap.containsKey(uuid)) {
            forceCleanup(uuid, true);

            visualizationManager.sendAlert(player, ChatColor.RED + "Claim mode " + ChatColor.BOLD + "disabled!");
        } else {
            forceCleanup(uuid, true);

            modeMap.put(uuid, ClickState.CLAIM);
            visualizationManager.visualizeSuroudningClaims(player, dataManager);
            visualizationManager.sendAlert(player, ChatColor.GREEN + "Claim mode " + ChatColor.BOLD + "enabled!");
        }
    }

    @CommandAlias("subclaim")
    public void subClaim(Player player){
        UUID uuid = player.getUniqueId();

        if (stateMap.containsKey(uuid)) {
            forceCleanup(uuid, true);

            visualizationManager.sendAlert(player, ChatColor.RED + "Sub Claiming mode " + ChatColor.BOLD + "disabled!");
        } else {
            forceCleanup(uuid, true);

            Location location = player.getLocation();

            Claim claim = dataManager.getClaim(location.getBlockX(), location.getBlockZ(), player.getWorld().getUID());
            if (claim == null) {
                player.sendMessage(ChatColor.RED + "You need to be standing in a claim to enable sub claiming mode.");
                return;
            }

            if (!PermissionHelper.getPermissionHelper().hasPermission(claim, uuid, PermissionRoute.MODIFY_CLAIM)) {
                player.sendMessage(ChatColor.RED + "You need MODIFY_CLAIM to create sub claims.");
                return;
            }

            if (claim.isEditing()){
                player.sendMessage(ChatColor.RED + "The claim your are attempting to resize is already being resized.");
                return;
            }

            claimMap.put(uuid, claim);
            modeMap.put(uuid, ClickState.SUB_CLAIM);

            claim.setEditing(true);
            visualizationManager.visualizeSuroudningSubClaims(claim, player);

            visualizationManager.sendAlert(player, ChatColor.GREEN + "Sub Claiming mode " + ChatColor.BOLD + "enabled!");
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e){
        if (e.getHand() == null
                || !e.getHand().equals(EquipmentSlot.HAND)
                || e.getClickedBlock() == null){
            return;
        }

        click(e.getPlayer(), e.getClickedBlock().getLocation());
    }

    public void click(Player player, Location location) {
        UUID uuid = player.getUniqueId();

        if (stateMap.containsKey(uuid)){
            stateMap.get(uuid).click(player, location);
            return;
        }

        if (modeMap.containsKey(uuid)){
            ClickState state = modeMap.get(uuid);

            switch (state){
                case CLAIM:
                    Claim claim = dataManager.getClaim(location);
                    if (claim == null){
                        stateMap.put(uuid, new NewClaimMode(this, player, location));
                    } else {
                        stateMap.put(uuid, new ResizeClaimMode(this, player, claim, location));
                    }
                    return;
                case SUB_CLAIM:
                    Claim parent = claimMap.get(uuid);

                    if (parent == null){
                        return;
                    }

                    parent.setEditing(true);

                    SubClaim subClaim = parent.getSubClaim(location.getBlockX(), location.getBlockZ());

                    if (subClaim != null){
                        stateMap.put(uuid, new ResizeSubClaimMode(this, player, parent, subClaim, location));
                        return;
                    }

                    stateMap.put(uuid, new NewSubClaimMode(this, player, parent, location));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        forceCleanup(e.getPlayer().getUniqueId(), true);
    }

    public void forceCleanup(UUID uuid, boolean visuals){
        Claim claim = claimMap.get(uuid);
        if (claim != null){
            claim.setEditing(false);
        }

        modeMap.remove(uuid);
        stateMap.remove(uuid);

        if (visuals){
            VisualGroup group = visualizationManager.fetchExistingGroup(uuid);
            if (group != null){
                group.removeAllVisuals();
            }
        }
    }

    public ClaimDataManager getDataManager() {
        return dataManager;
    }

    public VisualizationManager getVisualizationManager() {
        return visualizationManager;
    }
}
