package net.crashcraft.crashclaim.data;

public class MathUtils {
    public static boolean iskPointCollide(int minX, int minZ, int maxX, int maxZ, int x, int z) {
        return (z >= minZ && z <= maxZ) && (x >= minX && x <= maxX);
    }

    public static boolean doOverlap(int minX_1, int minZ_1, int maxX_1, int maxZ_1, int minX_2, int minZ_2, int maxX_2, int maxZ_2) {
        return !(minX_2 > maxX_1 ||
                minZ_2 > maxZ_1 ||
                minX_1 > maxX_2 ||
                minZ_1 > maxZ_2);
    }

    public static boolean containedInside(int minX_1, int minZ_1, int maxX_1, int maxZ_1, int minX_2, int minZ_2, int maxX_2, int maxZ_2) {
        return iskPointCollide(minX_1, minZ_1, maxX_1, maxZ_1, minX_2, minZ_2)
                && iskPointCollide(minX_1, minZ_1, maxX_1, maxZ_1, maxX_2, maxZ_2);
    }
}
