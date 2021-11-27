package net.crashcraft.crashclaim.compatability.versions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.primitives.Ints;
import net.crashcraft.crashclaim.compatability.CompatabilityManager;
import net.crashcraft.crashclaim.compatability.CompatabilityWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("Duplicates")
public class Wrapper1_16 implements CompatabilityWrapper {
    @Override
    public void sendActionBarTitle(Player player, BaseComponent[] message, int fade_in, int duration, int fade_out) {
        PacketContainer packet = CompatabilityManager.getProtocolManager().createPacket(PacketType.Play.Server.TITLE);

        packet.getTitleActions().write(0, EnumWrappers.TitleAction.ACTIONBAR);
        packet.getChatComponents().write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(message)));
        packet.getIntegers().write(0, fade_in);
        packet.getIntegers().write(1, duration);
        packet.getIntegers().write(2, fade_out);

        try {
            CompatabilityManager.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void spawnGlowingInvisibleMagmaSlime(Player player, double x, double z, double y, int id, UUID uuid,
                                                HashMap<Integer, String> fakeEntities, HashMap<Integer, Location> entityLocations) {
        PacketContainer packet = CompatabilityManager.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

        packet.getIntegers()
                .write(0, id)
                .write(1, 44);//38  //Entity id //1.14: 40 //1.15: 41
        packet.getUUIDs()
                .write(0, uuid);
        packet.getDoubles() //Cords
                .write(0, x)
                .write(1, y)
                .write(2, z);

        PacketContainer metaDataPacket = CompatabilityManager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA, true);

        WrappedDataWatcher watcher = new WrappedDataWatcher();

        watcher.setObject(0, CompatabilityManager.getByteSerializer(), (byte) (0x20 | 0x40)); // Glowing Invisible
        watcher.setObject(15, CompatabilityManager.getIntegerSerializer(), 2); //Slime size : 12

        metaDataPacket.getIntegers()
                .write(0, id);
        metaDataPacket.getWatchableCollectionModifier()
                .write(0, watcher.getWatchableObjects());

        try {
            CompatabilityManager.getProtocolManager().sendServerPacket(player, packet);
            CompatabilityManager.getProtocolManager().sendServerPacket(player, metaDataPacket);

            fakeEntities.put(id, uuid.toString());
            entityLocations.put(id, new Location(player.getWorld(), x, y, z));
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeEntity(Player player, Set<Integer> entity_ids){
        PacketContainer packet = CompatabilityManager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);

        packet.getIntegerArrays()
                .write(0, Ints.toArray(entity_ids));

        try {
            CompatabilityManager.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setEntityTeam(Player player, String team, List<String> uuids){
        PacketContainer packet = CompatabilityManager.getProtocolManager().createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);

        packet.getStrings()
                .write(0, team);   //Team name
        packet.getIntegers().write(0, 3);   //Packet option - 3: update team

        packet.getSpecificModifier(Collection.class)
                .write(0, uuids);

        try {
            CompatabilityManager.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isInteractAndMainHand(PacketContainer packet) {
        return packet.getEntityUseActions().read(0).equals(EnumWrappers.EntityUseAction.INTERACT_AT) &&
                packet.getHands().read(0).equals(EnumWrappers.Hand.MAIN_HAND);
    }

    @Override
    public int getMinWorldHeight(World world) {
        return 0;
    }

    private AtomicInteger ENTITY_ID;
    private String NMS;

    public Class<?> getNMSClass(final String className) throws ClassNotFoundException {
        return Class.forName(NMS + className);
    }

    @Override
    public int getUniqueEntityID() {
        if (ENTITY_ID == null){
            final String packageName = Bukkit.getServer().getClass().getPackage().getName();
            final String SERVER_VERSION = packageName.substring(packageName.lastIndexOf('.') + 1);
            NMS = "net.minecraft.server." + SERVER_VERSION + ".";

            try {
                final Field entityCount = getNMSClass("Entity").getDeclaredField("entityCount");
                entityCount.setAccessible(true);
                ENTITY_ID = (AtomicInteger) entityCount.get(null);
            } catch (final ReflectiveOperationException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return ENTITY_ID.incrementAndGet();
    }
}
