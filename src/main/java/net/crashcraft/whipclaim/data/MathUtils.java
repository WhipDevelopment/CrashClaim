package net.crashcraft.whipclaim.data;

public class MathUtils {
    public static boolean checkPointCollide(int x1, int y1, int x2, int y2, int x, int y) {
        return (x > x1 && x < x2 && y > y1 && y < y2);
    }

    public static boolean doOverlap(int l1x, int l1y, int r1x, int r1y, int l2x, int l2y, int r2x, int r2y) {
        return  (((l2x >= l1x && l2x <= r1x) ||
                (r2x >= l1x && r2x <= r1x)) &&
                ((l2y >= l1y && l2y <= r1y) ||
                (r2y >= l1y && r2y <= r1y)));
    }
}
