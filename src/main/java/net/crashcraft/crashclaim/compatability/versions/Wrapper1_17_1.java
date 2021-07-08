package net.crashcraft.crashclaim.compatability.versions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.crashcraft.crashclaim.compatability.CompatabilityManager;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class Wrapper1_17_1 extends Wrapper1_17 {
    @Override
    public void removeEntity(Player player, int[] entity_ids){
        PacketContainer packet = CompatabilityManager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);

        packet.getIntegerArrays()
                .write(0, entity_ids);

        try {
            CompatabilityManager.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
