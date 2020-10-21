package net.crashcraft.whipclaim.visualize.api.visuals;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.visualize.api.BaseVisual;
import net.crashcraft.whipclaim.visualize.api.VisualColor;
import net.crashcraft.whipclaim.visualize.api.VisualGroup;
import net.crashcraft.whipclaim.visualize.api.VisualType;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class BaseGlowVisual extends BaseVisual {
    private static WrappedDataWatcher.Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);
    private static WrappedDataWatcher.Serializer integerSerializer = WrappedDataWatcher.Registry.get(Integer.class);

    private static ProtocolManager protocolManager;

    private HashMap<Integer, String> fakeEntities; // id - uuid
    private HashMap<Integer, Location> entityLocations; // check for hit collision on custom interact event

    public BaseGlowVisual(VisualType type, VisualColor color, VisualGroup parent, Player player, int y) {
        super(type, color, parent, player, y);
        this.fakeEntities = new HashMap<>();
        this.entityLocations = new HashMap<>();
    }

    public BaseGlowVisual(VisualType type, VisualColor color, VisualGroup parent, Player player, int y, BaseClaim claim) {
        super(type, color, parent, player, y, claim);

        this.fakeEntities = new HashMap<>();
        this.entityLocations = new HashMap<>();
    }

    public void spawnEntity(int x, int z, int y){
        spawnEntity(getPlayer(),
                x,
                z,
                y,
                getParent().generateUiniqueID(),
                getParent().generateUiniqueUUID());
    }

    public void removeAll(){
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);

        packet.getIntegerArrays()
                .write(0, toPrimitiveIntegerArrays(fakeEntities.keySet()));

        try {
            protocolManager.sendServerPacket(getPlayer(), packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void colorEntities(Player player, VisualColor color, ArrayList<String> uuids){
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);

        packet.getStrings()
                .write(0, color.toString());   //Team name
        packet.getIntegers().write(0, 3);   //Packet option - 3: update team

        packet.getSpecificModifier(Collection.class)
                .write(0, uuids);

        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void spawnEntity(Player player, int x, int z, int y, int id, UUID uuid){
        double dx;
        double dz;

        dx = x + 0.5;
        dz = z + 0.5;

        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

        packet.getIntegers()
                .write(0, id)
                .write(1, 44);//38  //Entity id //1.14: 40 //1.15: 41
        packet.getUUIDs()
                .write(0, uuid);
        packet.getDoubles() //Cords
                .write(0, dx)
                .write(1, (double) y)
                .write(2, dz);

        PacketContainer metaDataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA, true);

        WrappedDataWatcher watcher = new WrappedDataWatcher();

        watcher.setObject(0, byteSerializer, (byte) (0x20 | 0x40)); // Glowing Invisible
        watcher.setObject(15, integerSerializer, 2); //Slime size : 12

        metaDataPacket.getIntegers()
                .write(0, id);
        metaDataPacket.getWatchableCollectionModifier()
                .write(0, watcher.getWatchableObjects());

        try {
            protocolManager.sendServerPacket(player, packet);
            protocolManager.sendServerPacket(player, metaDataPacket);

            fakeEntities.put(id, uuid.toString());
            entityLocations.put(id, new Location(player.getWorld(), dx, y, dz));
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Integer> getEntityIDs(){
        return new ArrayList<>(fakeEntities.keySet());
    }

    public ArrayList<String> getEntityUUIDs(){
        return new ArrayList<>(fakeEntities.values());
    }

    public Location getEntityLocation(int id){
        return entityLocations.get(id);
    }

    public boolean containsID(int id){
        return fakeEntities.containsKey(id);
    }

    public boolean containsUUID(String uuid){
        return fakeEntities.containsValue(uuid);
    }

    public static void setProtocolManager(ProtocolManager manager){
        protocolManager = manager;
    }

    private static int[] toPrimitiveIntegerArrays(Set<Integer> array){
        return ArrayUtils.toPrimitive(array.toArray(new Integer[0]));
    }
}
