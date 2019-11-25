package net.crashcraft.whipclaim.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

public class StaticClaimLogic {
    public static long getChunkHash(long chunkX, long chunkZ) {
        return (chunkZ ^ (chunkX << 32));
    }

    public static long getChunkHashFromLocation(int x, int z) {
        return getChunkHash(x >> 4, z >> 4);
    }

    public static Location calculateUpperCorner(Location loc1, Location loc2){
        return new Location(loc1.getWorld(), Math.min(loc1.getBlockX(), loc2.getBlockX()), loc1.getY(), Math.min(loc1.getBlockZ(), loc2.getBlockZ()));
    }

    public static Location calculateLowerCorner(Location loc1, Location loc2){
        return new Location(loc1.getWorld(), Math.max(loc1.getBlockX(), loc2.getBlockX()), loc1.getY(), Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
    }

    public static Location calculateUpperCorner(int x, int z, int x2, int z2, UUID world){
        return new Location(Bukkit.getWorld(world), Math.min(x, x2), 0, Math.min(z, z2));
    }

    public static Location calculateLowerCorner(int x, int z, int x2, int z2, UUID world){
        return new Location(Bukkit.getWorld(world), Math.max(x, x2), 0, Math.max(z, z2));
    }
}
