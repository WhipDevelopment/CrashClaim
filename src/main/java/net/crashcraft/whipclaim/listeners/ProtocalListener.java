package net.crashcraft.whipclaim.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.commands.ClaimModeCommand;
import net.crashcraft.whipclaim.visualize.Visual;
import net.crashcraft.whipclaim.visualize.VisualGroup;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ProtocalListener {
    public ProtocalListener(ProtocolManager protocolManager, WhipClaim whipClaim, ClaimModeCommand command){
        protocolManager.addPacketListener(
                new PacketAdapter(whipClaim, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
                    @Override
                    public void onPacketReceiving(PacketEvent event){
                        PacketContainer packet = event.getPacket();
                        if (packet.getEntityUseActions().read(0).equals(EnumWrappers.EntityUseAction.INTERACT_AT) &&
                                packet.getHands().read(0).equals(EnumWrappers.Hand.MAIN_HAND)){
                            Player player = event.getPlayer();

                            if (player == null)
                                return;

                            VisualGroup group = whipClaim.getVisualizationManager().fetchVisualGroup(player, false);

                            if (group == null)
                                return;

                            int id = packet.getIntegers().read(0);

                            for (Visual visual : group.getActiveVisuals()){
                                Location location = visual.getEntityLocation(id);
                                if (location != null){
                                    command.click(player, location);
                                    return;
                                }
                            }
                        }
                    }
                });
    }
}
