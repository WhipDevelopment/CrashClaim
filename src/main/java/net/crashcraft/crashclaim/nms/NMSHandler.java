package net.crashcraft.crashclaim.nms;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NMSHandler {

    public void sendActionBar(Player player, BaseComponent[] message) {
        player.sendActionBar(message);
    }


    public void setEntityTeam(Player player, String team, List<String> uuids){
        WrapperPlayServerTeams.ScoreBoardTeamInfo teamInfo = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                null, null, null, null, null, null, null
        );

        WrapperPlayServerTeams packet = new WrapperPlayServerTeams(team, WrapperPlayServerTeams.TeamMode.ADD_ENTITIES, teamInfo, uuids);

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);

    }

    public int getMinWorldHeight(World world) {
        return world.getMinHeight();
    }

    public void removeEntity(Player player, Set<Integer> entity_ids){
        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(entity_ids.stream().mapToInt(Integer::intValue).toArray());

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public void spawnGlowingInvisibleMagmaSlime(Player player, double x, double z, double y, int id, UUID uuid,
                                                HashMap<Integer, String> fakeEntities, HashMap<Integer, Location> entityLocations) {
        WrapperPlayServerSpawnEntity spawnEntityPacket = new WrapperPlayServerSpawnEntity(id, uuid, EntityTypes.MAGMA_CUBE,
                new com.github.retrooper.packetevents.protocol.world.Location(x, y, z, 0f, 0f), (float) 0, 0, new Vector3d(0, 0, 0));

        List<EntityData> metadata = new ArrayList<>();
        byte bytevalues = 0x20 | 0x40;
        metadata.add(new EntityData(0, EntityDataTypes.BYTE, bytevalues));
        metadata.add(new EntityData(16, EntityDataTypes.INT, 2));
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(id, metadata);

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnEntityPacket);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, metadataPacket);

        fakeEntities.put(id, uuid.toString());
        entityLocations.put(id, new Location(player.getWorld(), x, y, z));
    }

    private AtomicInteger ENTITY_ID;

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
