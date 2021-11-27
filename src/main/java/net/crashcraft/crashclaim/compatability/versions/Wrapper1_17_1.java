package net.crashcraft.crashclaim.compatability.versions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.crashcraft.crashclaim.compatability.CompatabilityManager;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;

public class Wrapper1_17_1 extends Wrapper1_17_0 {
    @Override
    public void removeEntity(Player player, Set<Integer> entity_ids){
        PacketContainer packet = CompatabilityManager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);

        packet.getIntLists()
                .write(0, new ArrayList<>(entity_ids));

        try {
            CompatabilityManager.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
