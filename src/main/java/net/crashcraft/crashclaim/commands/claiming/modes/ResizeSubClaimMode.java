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
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
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
            player.spigot().sendMessage(Localization.RESIZE_SUBCLAIM__INSTRUCTIONS.getMessage(player));
            return;
        }

        player.spigot().sendMessage(Localization.RESIZE_SUBCLAIM__CLICK_ANOTHER_LOCATION.getMessage(player));

        VisualGroup group = visualizationManager.fetchVisualGroup(player, true);

        for (BaseVisual visual : group.getActiveVisuals()){
            BaseClaim tempClaim = visual.getClaim();
            if (tempClaim == null)
                continue;

            if (tempClaim instanceof SubClaim){
                SubClaim visualSubClaim = (SubClaim) tempClaim;
                if (visualSubClaim.equals(subClaim)){
                    group.removeVisual(visual);

                    visualizationManager.getProvider(player.getUniqueId()).spawnClaimVisual(VisualColor.YELLOW, group, subClaim, visual.getY()).spawn();
                    return;
                }
            }
        }
    }

    @Override
    public void click(Player player, Location click) {
        if (!PermissionHelper.getPermissionHelper().hasPermission(claim, player.getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
            player.spigot().sendMessage(Localization.SUBCLAIM__NO_PERMISSION.getMessage(player));
            cleanup(player.getUniqueId(), true);
            return;
        }

        if (firstLocation == null){
            firstLocation = click;
            firstClick();
            return;
        }

        if (!MathUtils.iskPointCollide(claim.getMinX(), claim.getMinZ(),
                claim.getMaxX(), claim.getMaxZ(), click.getBlockX(), click.getBlockZ())){
            player.spigot().sendMessage(Localization.RESIZE_SUBCLAIM__INSIDE_PARENT.getMessage(player));
            cleanup(player.getUniqueId(), true);
            return;
        }

        ErrorType error = manager.resizeSubClaim(subClaim, firstLocation.getBlockX(), firstLocation.getBlockZ(), click.getBlockX(), click.getBlockZ()); //Resize no payment here

        switch (error){
            case OVERLAP_EXISTING_SUBCLAIM:
                player.spigot().sendMessage(Localization.RESIZE_SUBCLAIM__NO_OVERLAP.getMessage(player));
                cleanup(player.getUniqueId(), true);
                return;
            case TOO_SMALL:
                player.spigot().sendMessage(Localization.RESIZE_SUBCLAIM__MIN_SIZE.getMessage(player));
                cleanup(player.getUniqueId(), true);
                return;
            case CANNOT_FLIP_ON_RESIZE:
                player.spigot().sendMessage(Localization.RESIZE_SUBCLAIM__CANNOT_FLIP.getMessage(player));
                cleanup(player.getUniqueId(), true);
                return;
            case NONE:
                player.spigot().sendMessage(Localization.RESIZE_SUBCLAIM__SUCCESS.getMessage(player));

                VisualGroup group = visualizationManager.fetchVisualGroup(player, true);

                visualizationManager.visualizeSurroundingSubClaims(claim, player);

                for (BaseVisual visual : group.getActiveVisuals()){
                    visualizationManager.deSpawnAfter(visual, 5);
                }

                cleanup(player.getUniqueId(), false);
        }
    }

    @Override
    public void cleanup(UUID player, boolean visuals) {
        firstLocation = null;
        claim.setEditing(false);

        commandManager.forceCleanup(player, visuals);
    }
}
