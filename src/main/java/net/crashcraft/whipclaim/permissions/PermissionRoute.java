package net.crashcraft.whipclaim.permissions;

import net.crashcraft.whipclaim.claimobjects.PermState;
import net.crashcraft.whipclaim.claimobjects.PermissionSet;
import org.bukkit.Material;

import java.util.HashMap;

public enum  PermissionRoute {
    BUILD{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return -1;
            return set.getBuild();
        }
    },
    INTERACTIONS{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return -1;
            return set.getInteractions();
        }
    },
    ENTITIES{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return -1;
            return set.getEntities();
        }
    },
    EXPLOSIONS{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return -1;
            return set.getExplosions();
        }
    },
    TELEPORTATION{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return -1;
            return set.getTeleportation();
        }
    },
    PISTONS{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return -1;
            return set.getPistons();
        }
    },
    FLUIDS{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return -1;
            return set.getFluids();
        }
    },
    ALLOW_SLIMES{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return -1;
            return set.getAllowSlimes();
        }
    },
    MODIFY_PERMISSIONS{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return PermState.NEUTRAL;
            return set.getModifyPermissions();
        }
    },
    MODIFY_CLAIM{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return PermState.NEUTRAL;
            return set.getModifyClaim();
        }
    },
    VIEW_SUB_CLAIMS{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return PermState.NEUTRAL;
            return set.getViewSubClaims();
        }
    },
    CONTAINERS{
        @Override
        public int getPerm(PermissionSet set) {
            throw new RuntimeException("PermissionRoute was called with an invalid perm. (CONTAINERS called getPerm");
        }

        @Override
        public HashMap<Material, Integer> getListPerms(PermissionSet set) {
            if (set == null)
                return null;
            return set.getContainers();
        }
    };

    PermissionRoute(){

    }

    public abstract int getPerm(PermissionSet set);

    public HashMap<Material, Integer> getListPerms(PermissionSet set){
        throw new RuntimeException("PermissionRoute was called with an invalid perm");
    }
}
