package net.crashcraft.whipclaim.data;

import org.bukkit.Location;

public class StaticClaimLogic {
    public static long getChunkHash(long chunkX, long chunkZ) {
        return (chunkZ ^ (chunkX << 32));
    }

    public static long getChunkHashFromLocation(int x, int z) {
        return getChunkHash(x >> 4, z >> 4);
    }

    public static Location calculateMaxCorner(Location loc1, Location loc2){
        return new Location(loc1.getWorld(), Math.max(loc1.getBlockX(), loc2.getBlockX()), loc1.getY(), Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
    }

    public static Location calculateMinCorner(Location loc1, Location loc2){
        return new Location(loc1.getWorld(), Math.min(loc1.getBlockX(), loc2.getBlockX()), loc1.getY(), Math.min(loc1.getBlockZ(), loc2.getBlockZ()));
    }
/*
    public static boolean isClaimBorder(int NWCorner_x, int SECorner_x, int NWCorner_z, int SECorner_z, int Start_x, int Start_z) {
        return Start_x == NWCorner_x || Start_x == SECorner_x || Start_z == NWCorner_z || Start_z == SECorner_z;
    }

 */
    public static boolean isClaimBorder(int min_x, int max_x, int min_z, int max_z, int point_x, int point_z){
        return (point_x == min_x || point_x == max_x) && (point_z == min_z || point_z == max_z);
    }
}
