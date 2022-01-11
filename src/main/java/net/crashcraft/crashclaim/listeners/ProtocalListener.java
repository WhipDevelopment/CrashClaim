package net.crashcraft.crashclaim.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.commands.claiming.ClaimCommand;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.providers.glow.BaseGlowVisual;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ProtocalListener {
    public ProtocalListener(ProtocolManager protocolManager, CrashClaim crashClaim, ClaimCommand command){
        protocolManager.addPacketListener(
                new PacketAdapter(crashClaim, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
                    @Override
                    public void onPacketReceiving(PacketEvent event){
                        PacketContainer packet = event.getPacket();

                        if (crashClaim.getWrapper().isInteractAndMainHand(packet)){
                            Player player = event.getPlayer();

                            if (player == null)
                                return;

                            VisualGroup group = crashClaim.getVisualizationManager().fetchVisualGroup(player, false);

                            if (group == null)
                                return;

                            int id = packet.getIntegers().read(0);

                            for (BaseVisual visual : group.getActiveVisuals()){
                                if (visual instanceof BaseGlowVisual glowVisual) {
                                    Location location = glowVisual.getEntityLocation(id);
                                    if (location != null) {
                                        Bukkit.getScheduler().runTask(plugin, () -> command.click(player, location));
                                        return;
                                    }
                                }
                            }
                        }
                    }
                });
    }
}
