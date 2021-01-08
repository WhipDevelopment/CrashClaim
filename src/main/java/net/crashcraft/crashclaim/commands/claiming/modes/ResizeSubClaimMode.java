package net.crashcraft.crashclaim.commands.claiming.modes;

import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.commands.claiming.ClaimCommand;
import net.crashcraft.crashclaim.commands.claiming.ClaimMode;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.data.ErrorType;
import net.crashcraft.crashclaim.data.MathUtils;
import net.crashcraft.crashclaim.data.StaticClaimLogic;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ResizeSubClaimMode implements ClaimMode {
    private final ClaimCommand commandManager;
    private Location firstLocation;
    private final Player player;
    private final VisualizationManager visualizationManager;
    private final ClaimDataManager manager;
    private final Claim claim;
    private final SubClaim subClaim;

    public ResizeSubClaimMode(ClaimCommand commandManager, Player player, Claim claim, SubClaim subClaim, Location firstLocation) {
        this.commandManager = commandManager;
        this.claim = claim;
        this.player = player;
        this.subClaim = subClaim;
        this.firstLocation = firstLocation;
        this.visualizationManager = commandManager.getVisualizationManager();
        this.manager = commandManager.getDataManager();

        firstClick();
    }

    private void firstClick(){
        if (!StaticClaimLogic.isClaimBorder(subClaim.getMinX(), subClaim.getMaxX(), subClaim.getMinZ(), subClaim.getMaxZ(),
                firstLocation.getBlockX(), firstLocation.getBlockZ())){
            firstLocation = null;
            player.sendMessage(ChatColor.RED + "You need to click the border of the sub claim to resize it. Grabbing an edge will move it in that direction, grabbing a corner will move it in both directions relative to the corner.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Click another location to resize the claims. ");

        VisualGroup group = visualizationManager.fetchVisualGroup(player, true);

        for (BaseVisual visual : group.getActiveVisuals()){
            BaseClaim tempClaim = visual.getClaim();
            if (tempClaim == null)
                continue;

            if (tempClaim instanceof SubClaim){
                SubClaim visualSubClaim = (SubClaim) tempClaim;
                if (visualSubClaim.equals(subClaim)){
                    group.removeVisual(visual);

                    visualizationManager.getProvider().spawnClaimVisual(VisualColor.YELLOW, group, subClaim, visual.getY()).spawn();
                    return;
                }
            }
        }
    }

    @Override
    public void click(Player player, Location click) {
        if (firstLocation == null){
            firstLocation = click;
            firstClick();
            return;
        }

        if (!MathUtils.iskPointCollide(claim.getMinX(), claim.getMinZ(),
                claim.getMaxX(), claim.getMaxZ(), click.getBlockX(), click.getBlockZ())){
            player.sendMessage(ChatColor.RED + "Sub claims can only be formed inside of a parent claim.");
            cleanup(player.getUniqueId(), true);
            return;
        }

        ErrorType error = manager.resizeSubClaim(subClaim, firstLocation.getBlockX(), firstLocation.getBlockZ(), click.getBlockX(), click.getBlockZ()); //Resize no payment here

        switch (error){
            case OVERLAP_EXISTING_SUBCLAIM:
                player.sendMessage(ChatColor.RED + "You cannot overlap other sub claims.");
                cleanup(player.getUniqueId(), true);
                return;
            case TOO_SMALL:
                player.sendMessage(ChatColor.RED + "A claim has to be at least a 5x5");
                cleanup(player.getUniqueId(), true);
                return;
            case CANNOT_FLIP_ON_RESIZE:
                player.sendMessage(ChatColor.RED + "Claims cannot be flipped, please retry and grab the other edge to expand in this direction");
                cleanup(player.getUniqueId(), true);
                return;
            case NONE:
                player.sendMessage(ChatColor.GREEN + "Claim has been successfully resized");

                VisualGroup group = visualizationManager.fetchVisualGroup(player, true);

                visualizationManager.visualizeSuroudningSubClaims(claim, player);

                for (BaseVisual visual : group.getActiveVisuals()){
                    visualizationManager.despawnAfter(visual, 5);
                }

                cleanup(player.getUniqueId(), false);
        }
    }

    @Override
    public void cleanup(UUID player, boolean visuals) {
        claim.setEditing(false);

        commandManager.forceCleanup(player, visuals);
    }
}
