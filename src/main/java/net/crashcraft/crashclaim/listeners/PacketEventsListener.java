package net.crashcraft.crashclaim.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.commands.claiming.ClaimCommand;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.visuals.BaseGlowVisual;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;


public class PacketEventsListener implements PacketListener {
    private final CrashClaim crashClaim;
    private final ClaimCommand claimCommand;

    public PacketEventsListener(CrashClaim crashClaim, ClaimCommand claimCommand) {
        this.crashClaim = crashClaim;

        ClaimDataManager dataManager = crashClaim.getDataManager();
        VisualizationManager visualizationManager = crashClaim.getVisualizationManager();
        this.claimCommand = new ClaimCommand(dataManager, visualizationManager);

    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
            if (interact.getHand() == InteractionHand.MAIN_HAND) {
                Player player = (Player) event.getPlayer();

                if (player == null)
                    return;

                VisualGroup group = crashClaim.getVisualizationManager().fetchVisualGroup(player, false);

                if (group == null)
                    return;
                int id = interact.getEntityId();

                for (BaseVisual visual : group.getActiveVisuals()) {
                    if (visual instanceof BaseGlowVisual glowVisual) {
                        Location location = glowVisual.getEntityLocation(id);
                        if (location != null) {
                            Bukkit.getScheduler().runTask(crashClaim, () -> claimCommand.click(player, location));
                            return;
                        }
                    }
                }
            }
        }
    }
}