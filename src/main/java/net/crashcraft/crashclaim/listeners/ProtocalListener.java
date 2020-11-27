package net.crashcraft.crashclaim.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.commands.modes.ClaimModeCommand;
import net.crashcraft.crashclaim.commands.modes.SubClaimCommand;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.visuals.BaseGlowVisual;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ProtocalListener {
    public ProtocalListener(ProtocolManager protocolManager, CrashClaim crashClaim, ClaimModeCommand command, SubClaimCommand subClaimCommand){
        protocolManager.addPacketListener(
                new PacketAdapter(crashClaim, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
                    @Override
                    public void onPacketReceiving(PacketEvent event){
                        PacketContainer packet = event.getPacket();
                        if (packet.getEntityUseActions().read(0).equals(EnumWrappers.EntityUseAction.INTERACT_AT) &&
                                packet.getHands().read(0).equals(EnumWrappers.Hand.MAIN_HAND)){
                            Player player = event.getPlayer();

                            if (player == null)
                                return;

                            VisualGroup group = crashClaim.getVisualizationManager().fetchVisualGroup(player, false);

                            if (group == null)
                                return;

                            int id = packet.getIntegers().read(0);

                            for (BaseVisual visual : group.getActiveVisuals()){
                                if (visual instanceof BaseGlowVisual) {
                                    BaseGlowVisual glowVisual = (BaseGlowVisual) visual;

                                    Location location = glowVisual.getEntityLocation(id);
                                    if (location != null) {
                                        command.customEntityClick(player, location);
                                        subClaimCommand.clickFakeEntity(player, location);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                });
    }
}
