package net.crashcraft.whipclaim.permissions;

import net.crashcraft.whipclaim.claimobjects.*;
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
     */

    private static int processPerm(int primary, int secondary){
        return secondary == 2 ? primary : secondary;
    }

    public static int getLayeredPermission(PermissionSet global, PermissionSet main, PermissionRoute route){
        return processPerm(route.getPerm(global), route.getPerm(main));
    }

    public static int getLayeredPermission(Claim parent, SubClaim subClaim, UUID uuid, PermissionRoute route){
        PermissionGroup parentPerms = parent.getPerms();
        if (subClaim == null){
            return getLayeredPermission(parentPerms.getPermissionSet(), parentPerms.getPlayerPermissionSet(uuid), route);
        } else {
            PermissionGroup subPerms = parent.getPerms();
            return processPerm(
                    processPerm(route.getPerm(parentPerms.getPermissionSet()), route.getPerm(parentPerms.getPlayerPermissionSet(uuid))),
                    processPerm(route.getPerm(subPerms.getPermissionSet()), route.getPerm(subPerms.getPlayerPermissionSet(uuid)))
            );
        }
    }

    public static int getLayeredContainer(Claim parent, SubClaim subClaim, UUID uuid, PermissionRoute route, Material material){
        PermissionGroup parentPerms = parent.getPerms();
        if (subClaim == null){
            return getLayeredContainer(parentPerms.getPermissionSet(), parentPerms.getPlayerPermissionSet(uuid), route, material);
        } else {
            PermissionGroup subPerms = parent.getPerms();
            return processPerm(
                    processPerm(route.getPerm(parentPerms.getPermissionSet()), route.getListPerms(parentPerms.getPlayerPermissionSet(uuid)).get(material)),
                    processPerm(route.getPerm(subPerms.getPermissionSet()), route.getListPerms(subPerms.getPlayerPermissionSet(uuid)).get(material))
            );
        }
    }

    public static int getLayeredContainer(PermissionSet parent, PermissionSet secondary, PermissionRoute route, Material material){
        int mainPerm = route.getListPerms(secondary).get(material);
        int globalPerm = route.getListPerms(parent).get(material);

        return mainPerm == 2 ?
                        globalPerm : mainPerm;
    }

}
