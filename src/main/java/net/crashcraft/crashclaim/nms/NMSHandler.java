package net.crashcraft.crashclaim.nms;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NMSHandler {
    
    public void sendActionBarTitle(Player player, BaseComponent[] message, int fade_in, int duration, int fade_out) {
        PacketContainer packet = NMSManager.getProtocolManager().createPacket(PacketType.Play.Server.SET_ACTION_BAR_TEXT);

        packet.getChatComponents().write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(message)));

        PacketContainer packetDelay = NMSManager.getProtocolManager().createPacket(PacketType.Play.Server.SET_TITLES_ANIMATION);

        packetDelay.getIntegers().write(0, fade_in);
        packetDelay.getIntegers().write(1, duration);
        packetDelay.getIntegers().write(2, fade_out);

        NMSManager.getProtocolManager().sendServerPacket(player, packet);
        NMSManager.getProtocolManager().sendServerPacket(player, packetDelay);
    }

    
    public void setEntityTeam(Player player, String team, List<String> uuids){
        PacketContainer packet = NMSManager.getProtocolManager().createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);

        packet.getStrings()
                .write(0, team);   //Team name
        packet.getIntegers().write(0, 3);   //Packet option - 3: update team

        packet.getSpecificModifier(Collection.class)
                .write(0, uuids);

        NMSManager.getProtocolManager().sendServerPacket(player, packet);
    }

    private static boolean tempFix = true;

    public boolean isInteractAndMainHand(PacketContainer packet) {
        if (!packet.getEnumEntityUseActions().read(0).getAction().equals(EnumWrappers.EntityUseAction.INTERACT_AT)){
            return false;
        }

        //return packet.getHands().read(0).equals(EnumWrappers.Hand.MAIN_HAND); TODO fix this when protocol lib updates

        //below is nasty fix
        if (tempFix){
            tempFix = false;
            return true;
        } else {
            tempFix = true;
        }

        return false;
    }

    public int getMinWorldHeight(World world) {
        return world.getMinHeight();
    }

    public void removeEntity(Player player, Set<Integer> entity_ids){
        PacketContainer packet = NMSManager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);

        packet.getIntLists()
                .write(0, new ArrayList<>(entity_ids));

        NMSManager.getProtocolManager().sendServerPacket(player, packet);
    }

    public void spawnGlowingInvisibleMagmaSlime(Player player, double x, double z, double y, int id, UUID uuid,
                                                HashMap<Integer, String> fakeEntities, HashMap<Integer, Location> entityLocations) {
        PacketContainer packet = NMSManager.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);

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

        PacketContainer metaDataPacket = NMSManager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA, true);

        metaDataPacket.getIntegers()
                .write(0, id);
        metaDataPacket.getDataValueCollectionModifier().write(0, Lists.newArrayList(
                new WrappedDataValue(0, NMSManager.getByteSerializer(), (byte) (0x20 | 0x40)), // Glowing Invisible
                new WrappedDataValue(16, NMSManager.getIntegerSerializer(), 2) //Slime size : 12
        ));

        NMSManager.getProtocolManager().sendServerPacket(player, packet);
        NMSManager.getProtocolManager().sendServerPacket(player, metaDataPacket);

        fakeEntities.put(id, uuid.toString());
        entityLocations.put(id, new Location(player.getWorld(), x, y, z));
    }

    private AtomicInteger ENTITY_ID;

    private String NMS;

    public Class<?> getNMSClass(final String className) throws ClassNotFoundException {
        return Class.forName(NMS + className);
    }

    public int getUniqueEntityID() {
        if (ENTITY_ID == null) {
            try {
                final Field entityCount = Class.forName("net.minecraft.world.entity.Entity").getDeclaredField("d");
                entityCount.setAccessible(true);
                ENTITY_ID = (AtomicInteger) entityCount.get(null);
            } catch (final ReflectiveOperationException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return ENTITY_ID.incrementAndGet();
    }
}
