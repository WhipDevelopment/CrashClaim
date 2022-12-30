package net.crashcraft.crashclaim.compatability.versions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.google.common.collect.Lists;
import net.crashcraft.crashclaim.compatability.CompatabilityManager;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class Wrapper1_19_3 extends Wrapper1_19_2 {
    @Override
    public void spawnGlowingInvisibleMagmaSlime(Player player, double x, double z, double y, int id, UUID uuid,
                                                HashMap<Integer, String> fakeEntities, HashMap<Integer, Location> entityLocations) {
        PacketContainer packet = CompatabilityManager.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);

        packet.getIntegers()
                .write(0, id);
        packet.getEntityTypeModifier()
                .write(0, EntityType.MAGMA_CUBE);

        packet.getUUIDs()
                .write(0, uuid);
        packet.getDoubles() //Cords
                .write(0, x)
                .write(1, y)
                .write(2, z);

        PacketContainer metaDataPacket = CompatabilityManager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA, true);

        metaDataPacket.getIntegers()
                .write(0, id);
        metaDataPacket.getDataValueCollectionModifier().write(0, Lists.newArrayList(
                new WrappedDataValue(0, CompatabilityManager.getByteSerializer(), (byte) (0x20 | 0x40)), // Glowing Invisible
                new WrappedDataValue(16, CompatabilityManager.getIntegerSerializer(), 2) //Slime size : 12
        ));

        CompatabilityManager.getProtocolManager().sendServerPacket(player, packet);
        CompatabilityManager.getProtocolManager().sendServerPacket(player, metaDataPacket);

        fakeEntities.put(id, uuid.toString());
        entityLocations.put(id, new Location(player.getWorld(), x, y, z));
    }
}
