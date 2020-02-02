package net.crashcraft.whipclaim.permissions;

import net.crashcraft.whipclaim.claimobjects.*;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import org.bukkit.Material;

import java.util.UUID;

public class PermissionRouter {
    /*
        0 - Disable, 1 - Enable - 2 - Neutral

        Neutral fall back to previous layer

        Global Group  [ build: 0, interactions: 1, container{ chest }: 0 ]
        Main Group  [ build: 0, interactions: 2, container{ chest }: 1 ]
        Sub Group  [ build: 1, interactions: 2, container{ chest }: 2 ]

        Output  [ build: 1, interactions: 1, container{ chest }: 1 ]


        TODO should  have sub claims global as a per player with a nuetral to fall back on i guess
     */

    private static int processPerm(int primary, int secondary){
        return secondary == 2 ? primary : secondary;
    }

    public static int getLayeredPermission(Claim parent, SubClaim subClaim, PermissionRoute route){
        if (subClaim != null){
            return processPerm(route.getPerm(parent.getPerms().getPermissionSet()), route.getPerm(subClaim.getPerms().getPermissionSet()));
        } else {
            return route.getPerm(parent.getPerms().getPermissionSet());
        }
    }

    public static int getLayeredPermission(Claim parent, SubClaim subClaim, Material material){
        if (subClaim != null){
            return processPerm(PermissionRoute.CONTAINERS.getPerm(parent.getPerms().getPermissionSet(), material),
                    PermissionRoute.CONTAINERS.getPerm(subClaim.getPerms().getPermissionSet(), material));
        } else {
            return PermissionRoute.CONTAINERS.getPerm(parent.getPerms().getPermissionSet(), material);
        }
    }

    public static int getLayeredPermission(GlobalPermissionSet global, GlobalPermissionSet main, PermissionRoute route){
        return processPerm(route.getPerm(global), route.getPerm(main));
    }

    public static int getLayeredPermission(GlobalPermissionSet global, GlobalPermissionSet main, Material material){
        return processPerm(PermissionRoute.CONTAINERS.getPerm(global, material), PermissionRoute.CONTAINERS.getPerm(main, material));
    }

    public static int getLayeredPermission(GlobalPermissionSet global, PlayerPermissionSet main, PermissionRoute route){
        return main == null ? route.getPerm(global) : processPerm(route.getPerm(global), route.getPerm(main));

       // return processPerm(route.getPerm(global), main == null ? PermState.NEUTRAL : route.getPerm(main));
    }

    public static int getLayeredPermission(Claim parent, SubClaim subClaim, UUID uuid, PermissionRoute route){
        PermissionGroup parentPerms = parent.getPerms();
        if (subClaim == null){
            PlayerPermissionSet main = parentPerms.getPlayerPermissionSet(uuid);
            if (main == null){
                return route.getPerm(parentPerms.getPermissionSet());
            } else return getLayeredPermission(parentPerms.getPermissionSet(), main, route);
        } else {
            PermissionGroup subPerms = subClaim.getPerms();

            PlayerPermissionSet parentMainSet = parentPerms.getPlayerPermissionSet(uuid);
            PlayerPermissionSet subMainSet = subPerms.getPlayerPermissionSet(uuid);

            return processPerm(
                    getLayeredPermission(parentPerms.getPermissionSet(), parentMainSet, route),
                    getLayeredPermission(subPerms.getPermissionSet(), subMainSet, route)
            );
        }
    }

    /*
         if null or -1
         treat as neutral
     */

    public static int getLayeredContainer(Claim parent, SubClaim subClaim, UUID uuid, Material material){
        PermissionGroup parentPerms = parent.getPerms();
        if (subClaim == null){
            return getLayeredContainer(parentPerms.getPermissionSet(), parentPerms.getPlayerPermissionSet(uuid), material);
        } else {
            PermissionGroup subPerms = parent.getPerms();
            return processPerm(
                    getLayeredContainer(parentPerms.getPermissionSet(), parentPerms.getPlayerPermissionSet(uuid), material),
                    getLayeredContainer(subPerms.getPermissionSet(), subPerms.getPlayerPermissionSet(uuid), material)
            );
        }
    }

    public static int getLayeredContainer(GlobalPermissionSet parent, PlayerPermissionSet secondary, Material material){
        int globalPerm = PermissionRoute.CONTAINERS.getPerm(parent, material);

        if (secondary == null){
            return globalPerm;
        }

        int mainPerm = PermissionRoute.CONTAINERS.getPerm(secondary, material);

        return mainPerm == PermState.NEUTRAL ?
                        globalPerm : mainPerm;
    }

}
