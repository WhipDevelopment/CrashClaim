package net.crashcraft.whipclaim.data;

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

            /*
        maxX = -4
maxZ = -1
minX = 24
minZ = 26
x = -1
z = 23
         */
    /*
            (y >= ly && y <= ry) && (x >= lx && x <= rx)
(y <= ly && y >= ry) && (x <= lx && x >= rx)
            lx = 10065
            ly = 28
            rx = 10049
            ry = 10
            x = 10053
            y = 25
     */

    /*

     */
/*
    public static boolean doOverlap(int l1x, int l1y, int r1x, int r1y, int l2x, int l2y, int r2x, int r2y) {
        return  (((l2x >= l1x && l2x <= r1x) ||
                (r2x >= l1x && r2x <= r1x)) &&
                ((l2y >= l1y && l2y <= r1y) ||
                (r2y >= l1y && r2y <= r1y)));
    }
 */
}
