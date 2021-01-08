package net.crashcraft.crashclaim.commands.claiming.modes;

import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.commands.claiming.ClaimCommand;
import net.crashcraft.crashclaim.commands.claiming.ClaimMode;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.data.ClaimResponse;
import net.crashcraft.crashclaim.data.ErrorType;
import net.crashcraft.crashclaim.data.MathUtils;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.VisualType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NewSubClaimMode implements ClaimMode {
    private final ClaimCommand commandManager;
    private final Location firstLocation;
    private final VisualizationManager visualizationManager;
    private final ClaimDataManager manager;
    private final Claim claim;

    public NewSubClaimMode(ClaimCommand commandManager, Player player, Claim claim, Location firstLocation) {
        this.commandManager = commandManager;
        this.claim = claim;
        this.firstLocation = firstLocation;
        this.visualizationManager = commandManager.getVisualizationManager();
        this.manager = commandManager.getDataManager();

        VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
        group.removeAllVisualsOfType(VisualType.MARKER);

        visualizationManager.getProvider().spawnMarkerVisual(VisualColor.YELLOW, group, firstLocation.add(0, 1, 0)).spawn();

        player.sendMessage(ChatColor.GREEN + "Click an opposite corner to form a sub claim");
    }

    @Override
    public void click(Player player, Location click) {
        if (!MathUtils.iskPointCollide(claim.getMinX(), claim.getMinZ(),
                claim.getMaxX(), claim.getMaxZ(), click.getBlockX(), click.getBlockZ())){
            player.sendMessage(ChatColor.RED + "Sub claims can only be formed inside of a parent claim.");
            cleanup(player.getUniqueId(), true);
            return;
        }

        ClaimResponse response = manager.createSubClaim(player, claim, firstLocation, click);

        if (response.isStatus()){
            VisualGroup group = visualizationManager.fetchVisualGroup(player, true);

            group.removeAllVisuals();

            visualizationManager.visualizeSuroudningSubClaims(claim, player);

            for (BaseVisual visual : group.getActiveVisuals()){
                visualizationManager.despawnAfter(visual, 5);
            }

            player.sendMessage(ChatColor.GREEN + "Successfully created sub claim.");

            cleanup(player.getUniqueId(), false);
        } else {
            switch (response.getError()) {
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

    @Override
    public void cleanup(UUID player, boolean visuals) {
        claim.setEditing(false);

        commandManager.forceCleanup(player, visuals);
    }
}
