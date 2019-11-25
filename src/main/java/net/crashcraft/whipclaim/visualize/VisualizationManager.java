package net.crashcraft.whipclaim.visualize;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.crashcraft.whipclaim.WhipClaim;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class VisualizationManager {
    private static WrappedDataWatcher.Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);
    private static WrappedDataWatcher.Serializer integerSerializer = WrappedDataWatcher.Registry.get(Integer.class);

    private ProtocolManager protocolManager;
    private HashMap<UUID, VisualGroup> visualHashMap;

    private HashMap<Visual, Long> timeMap;

    public VisualizationManager(WhipClaim whipClaim){
        protocolManager = ProtocolLibrary.getProtocolManager();

        visualHashMap = new HashMap<>();
        timeMap = new HashMap<>();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(whipClaim, () -> {
            if (timeMap.size() == 0)
                return;

            long time = System.currentTimeMillis();

            for(Iterator<Map.Entry<Visual, Long>> it = timeMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Visual, Long> entry = it.next();
                if (entry.getValue() <= time){
                    entry.getKey().remove();
                    it.remove();
                }
            }
        },0,20);

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
                                if (location != null){//TODO add this
                                    //ClaimModeManager.clickBlock(UserCache.getUser(event.getPlayer()), location);
                                    return;
                                }
                            }
                        }
                    }
                });
    }

    public VisualGroup fetchVisualGroup(Player player, boolean create){
        if (visualHashMap.containsKey(player.getUniqueId())){
            return visualHashMap.get(player.getUniqueId());
        } else if (create){
            VisualGroup group = new VisualGroup(player, this);
            visualHashMap.put(player.getUniqueId(), group);
            return group;
        }
        return null;
    }

    public void despawnAfter(Visual visual, int seconds){
        timeMap.put(visual, System.currentTimeMillis() + (seconds * 1000));
    }

    public void colorEntities(Player player, TeamColor color, ArrayList<String> uuids){
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);

        packet.getStrings()
                .write(0, color.toString());
        packet.getIntegers().write(0, 3);

        packet.getSpecificModifier(Collection.class)
                .write(0, uuids);

        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void spawnEntity(Player player, int x, int z, int y, int id, UUID uuid, Visual visual){
        double dx;
        double dz;

        dx = x + 0.5;
        dz = z + 0.5;

        WrappedDataWatcher watcher = new WrappedDataWatcher();

        watcher.setObject(0, byteSerializer, (byte) (0x20 | 0x40)); // Glowing Invisible
        watcher.setObject(14, integerSerializer, 2); //Slime size : 12

        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

        packet.getIntegers()
                .write(0, id)
                .write(1, 40);//38  //Entity id
        packet.getUUIDs()
                .write(0, uuid);
        packet.getDoubles() //Cords
                .write(0, dx)
                .write(1, (double) y)
                .write(2, dz);

        packet.getDataWatcherModifier().write(0, watcher);

        try {
            protocolManager.sendServerPacket(player, packet);
            visual.addSpawnData(id, uuid.toString(), new Location(player.getWorld(), dx, y, dz));
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void despawnEntities(Player player, ArrayList<Integer> entities){
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);

        packet.getIntegerArrays()
                .write(0, toPrimitiveIntegerArrays(entities));

        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private int[] toPrimitiveIntegerArrays(ArrayList<Integer> array){
        return ArrayUtils.toPrimitive(array.toArray(new Integer[0]));
    }
}
