package net.crashcraft.whipclaim.permissions;

import javafx.scene.paint.Material;
import net.crashcraft.whipclaim.claimobjects.PermissionSet;

import java.util.HashMap;

public class PermissionRouter {
    /*
        0 - Disable, 1 - Enable - 2 - Neutral

        Neutral fall back to previous layer

        Global Group  [ build: 0, interactions: 1, container{ chest }: 0 ]
        Main Group  [ build: 0, interactions: 2, container{ chest }: 1 ]
        Sub Group  [ build: 1, interactions: 2, container{ chest }: 2 ]

        Output  [ build: 1, interactions: 1, container{ chest }: 1 ]
     */

    private static int processPerm(int global, int main, int sub){
        return sub == 2 ? (main == 2 ? global : main) : sub;
    }

    public static int getLayeredBuild(PermissionSet global, PermissionSet main, PermissionSet sub) {
        return processPerm(global.getBuild(), main.getBuild(), sub.getBuild());
    }

    public static int getLayeredInteractions(PermissionSet global, PermissionSet main, PermissionSet sub) {
        return processPerm(global.getInteractions(), main.getInteractions(), sub.getInteractions());
    }

    public static int getLayeredEntities(PermissionSet global, PermissionSet main, PermissionSet sub) {
        return processPerm(global.getEntities(), main.getEntities(), sub.getEntities());
    }

    public static int getLayeredExplosions(PermissionSet global, PermissionSet main, PermissionSet sub) {
        return processPerm(global.getExplosions(), main.getExplosions(), sub.getExplosions());
    }

    public static int getLayeredTeleportation(PermissionSet global, PermissionSet main, PermissionSet sub) {
        return processPerm(global.getTeleportation(), main.getTeleportation(), sub.getTeleportation());
    }

    public static int getLayeredPistons(PermissionSet global, PermissionSet main, PermissionSet sub) {
        return processPerm(global.getPistons(), main.getPistons(), sub.getPistons());
    }

    public static int getLayeredFluids(PermissionSet global, PermissionSet main, PermissionSet sub) {
        return processPerm(global.getFluids(), main.getFluids(), sub.getFluids());
    }

    public static int getLayeredAllowSlimes(PermissionSet global, PermissionSet main, PermissionSet sub) {
        return processPerm(global.getAllowSlimes(), main.getAllowSlimes(), sub.getAllowSlimes());
    }

    public static int getLayeredAdmin(PermissionSet global, PermissionSet main, PermissionSet sub) {
        return processPerm(global.getAdmin(), main.getAdmin(), sub.getAdmin());
    }

    public static HashMap<Material, Integer> getLayeredContainers(Material container, PermissionSet global, PermissionSet main, PermissionSet sub) {
        return sub.getContainers().get(container) == 2 ?
                (main.getContainers().get(container) == 2 ?
                        global.getContainers() : main.getContainers()) : sub.getContainers();
    }
}
