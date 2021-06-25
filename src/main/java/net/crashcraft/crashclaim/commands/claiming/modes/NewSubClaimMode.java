package net.crashcraft.crashclaim.commands.claiming.modes;

import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.commands.claiming.ClaimCommand;
import net.crashcraft.crashclaim.commands.claiming.ClaimMode;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.data.ClaimResponse;
import net.crashcraft.crashclaim.data.MathUtils;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.VisualType;
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

        player.sendMessage(Localization.NEW_SUBCLAIM__CLICK_CORNER.getMessage());
    }

    @Override
    public void click(Player player, Location click) {
        if (!MathUtils.iskPointCollide(claim.getMinX(), claim.getMinZ(),
                claim.getMaxX(), claim.getMaxZ(), click.getBlockX(), click.getBlockZ())){
            player.sendMessage(Localization.NEW_SUBCLAIM__NOT_INSIDE_PARENT.getMessage());
            cleanup(player.getUniqueId(), true);
            return;
        }

        ClaimResponse response = manager.createSubClaim(player, claim, firstLocation, click);

        if (response.isStatus()){
            VisualGroup group = visualizationManager.fetchVisualGroup(player, true);

            group.removeAllVisuals();

            visualizationManager.visualizeSuroudningSubClaims(claim, player);

            for (BaseVisual visual : group.getActiveVisuals()){
                visualizationManager.deSpawnAfter(visual, 5);
            }

            player.sendMessage(Localization.NEW_SUBCLAIM__SUCCESS.getMessage());

            cleanup(player.getUniqueId(), false);
        } else {
            switch (response.getError()) {
                case TOO_SMALL:
                    player.sendMessage(Localization.NEW_SUBCLAIM__MIN_AREA.getMessage());
                    break;
                case OUT_OF_BOUNDS:
                    player.sendMessage(Localization.NEW_SUBCLAIM__NEED_PARENT.getMessage());
                    break;
                case OVERLAP_EXISTING_SUBCLAIM:
                    player.sendMessage(Localization.NEW_SUBCLAIM__NO_OVERLAP.getMessage());
                    break;
                case GENERIC:
                    player.sendMessage(Localization.NEW_SUBCLAIM__ERROR.getMessage());
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
