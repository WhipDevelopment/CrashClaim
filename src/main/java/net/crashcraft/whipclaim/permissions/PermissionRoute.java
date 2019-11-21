package net.crashcraft.whipclaim.permissions;

import javafx.scene.paint.Material;
import net.crashcraft.whipclaim.claimobjects.PermissionSet;

import java.util.ArrayList;
import java.util.HashMap;

public enum  PermissionRoute {
    BUILD{
        @Override
        public int getPerm(PermissionSet set) {
            return set.getBuild();
        }
    },
    INTERACTIONS{
        @Override
        public int getPerm(PermissionSet set) {
            return set.getInteractions();
        }
    },
    ENTITIES{
        @Override
        public int getPerm(PermissionSet set) {
            return set.getEntities();
        }
    },
    EXPLOSIONS{
        @Override
        public int getPerm(PermissionSet set) {
            return set.getExplosions();
        }
    },
    TELEPORTATION{
        @Override
        public int getPerm(PermissionSet set) {
            return set.getTeleportation();
        }
    },
    PISTONS{
        @Override
        public int getPerm(PermissionSet set) {
            return set.getPistons();
        }
    },
    FLUIDS{
        @Override
        public int getPerm(PermissionSet set) {
            return set.getFluids();
        }
    },
    ALLOW_SLIMES{
        @Override
        public int getPerm(PermissionSet set) {
            return set.getAllowSlimes();
        }
    },
    MODIFY_PERMISSIONS{
        @Override
        public int getPerm(PermissionSet set) {
            return set.getModifyPermissions();
        }
    },
    MODIFY_CLAIM{
        @Override
        public int getPerm(PermissionSet set) {
            return set.getModifyClaim();
        }
    },
    CONTAINERS{
        @Override
        public int getPerm(PermissionSet set) {
            throw new RuntimeException("PermissionRoute was called with an invalid perm. (CONTAINERS called getPerm");
        }

        @Override
        public HashMap<Material, Integer> getListPerms(PermissionSet set) {
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
