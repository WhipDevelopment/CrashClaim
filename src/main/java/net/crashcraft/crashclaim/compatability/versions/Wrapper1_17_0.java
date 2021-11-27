package net.crashcraft.crashclaim.compatability.versions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
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
public class Wrapper1_17_0 implements CompatabilityWrapper {
    @Override
    public void sendActionBarTitle(Player player, BaseComponent[] message, int fade_in, int duration, int fade_out) {
        PacketContainer packet = CompatabilityManager.getProtocolManager().createPacket(
                new PacketType(PacketType.Protocol.PLAY, PacketType.Sender.SERVER, 0x41, MinecraftVersion.getCurrentVersion(), "ActionBar"));

        packet.getChatComponents().write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(message)));

        PacketContainer packetDelay = CompatabilityManager.getProtocolManager().createPacket(
                new PacketType(PacketType.Protocol.PLAY, PacketType.Sender.SERVER, 0x5A, MinecraftVersion.getCurrentVersion(), "SetTitleTime"));

        packetDelay.getIntegers().write(0, fade_in);
        packetDelay.getIntegers().write(1, duration);
        packetDelay.getIntegers().write(2, fade_out);

        try {
            CompatabilityManager.getProtocolManager().sendServerPacket(player, packet);
            CompatabilityManager.getProtocolManager().sendServerPacket(player, packetDelay);
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
                .write(1, 48);//38  //Entity id //1.14: 40 //1.15: 41
        packet.getUUIDs()
                .write(0, uuid);
        packet.getDoubles() //Cords
                .write(0, x)
                .write(1, y)
                .write(2, z);

        PacketContainer metaDataPacket = CompatabilityManager.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA, true);

        WrappedDataWatcher watcher = new WrappedDataWatcher();

        watcher.setObject(0, CompatabilityManager.getByteSerializer(), (byte) (0x20 | 0x40)); // Glowing Invisible
        watcher.setObject(16, CompatabilityManager.getIntegerSerializer(), 2); //Slime size : 12

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
        for (int id : entity_ids) {
            PacketContainer packet = CompatabilityManager.getProtocolManager().createPacket(
                    new PacketType(PacketType.Protocol.PLAY, PacketType.Sender.SERVER, 0x3A, MinecraftVersion.getCurrentVersion(), "DestroyEntity"));

            packet.getIntegers()
                    .write(0, id);

            try {
                CompatabilityManager.getProtocolManager().sendServerPacket(player, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
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

    private static boolean tempFix = true;

    @Override
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

    @Override
    public int getMinWorldHeight(World world) {
        return world.getMinHeight();
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
                final Field entityCount = Class.forName("net.minecraft.world.entity.Entity").getDeclaredField("b");
                entityCount.setAccessible(true);
                ENTITY_ID = (AtomicInteger) entityCount.get(null);
            } catch (final ReflectiveOperationException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return ENTITY_ID.incrementAndGet();
    }
}