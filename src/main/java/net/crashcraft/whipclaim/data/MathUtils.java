package net.crashcraft.whipclaim.data;

public class MathUtils {












    public static boolean checkPointCollide(int maxX, int maxZ, int minX, int minZ, int x, int z) {
        return (z <= minZ && z >= maxZ) && (x <= minX && x >= maxX);
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

    public static boolean doOverlap(int l1x, int l1y, int r1x, int r1y, int l2x, int l2y, int r2x, int r2y) {
        return  (((l2x >= l1x && l2x <= r1x) ||
                (r2x >= l1x && r2x <= r1x)) &&
                ((l2y >= l1y && l2y <= r1y) ||
                (r2y >= l1y && r2y <= r1y)));
    }

    public static boolean containedInside(int l1x, int l1y, int r1x, int r1y, int l2x, int l2y, int r2x, int r2y) {
        return checkPointCollide(l1x, l1y, r1x, r1y, l2x, l2y) && checkPointCollide(l1x, l1y, r1x, r1y, r2x, r2y);
    }
}
