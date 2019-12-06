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

        @Override
        public void setPerm(PermissionSet set, int value) {
            set.setBuild(value);
        }
    },
    INTERACTIONS{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return -1;
            return set.getInteractions();
        }

        @Override
        public void setPerm(PermissionSet set, int value) {
            set.setInteractions(value);
        }
    },
    ENTITIES{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return -1;
            return set.getEntities();
        }

        @Override
        public void setPerm(PermissionSet set, int value) {
            set.setEntities(value);
        }
    },
    EXPLOSIONS{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return -1;
            return set.getExplosions();
        }

        @Override
        public void setPerm(PermissionSet set, int value) {
            set.setExplosions(value);
        }
    },
    TELEPORTATION{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return -1;
            return set.getTeleportation();
        }

        @Override
        public void setPerm(PermissionSet set, int value) {
            set.setTeleportation(value);
        }
    },
    PISTONS{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return -1;
            return set.getPistons();
        }

        @Override
        public void setPerm(PermissionSet set, int value) {
            set.setPistons(value);
        }
    },
    FLUIDS{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return -1;
            return set.getFluids();
        }

        @Override
        public void setPerm(PermissionSet set, int value) {
            set.setFluids(value);
        }
    },
    MODIFY_PERMISSIONS{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return PermState.NEUTRAL;
            return set.getModifyPermissions();
        }

        @Override
        public void setPerm(PermissionSet set, int value) {
            set.setModifyPermissions(value);
        }
    },
    MODIFY_CLAIM{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return PermState.NEUTRAL;
            return set.getModifyClaim();
        }

        @Override
        public void setPerm(PermissionSet set, int value) {
            set.setModifyClaim(value);
        }
    },
    VIEW_SUB_CLAIMS{
        @Override
        public int getPerm(PermissionSet set) {
            if (set == null)
                return PermState.NEUTRAL;
            return set.getViewSubClaims();
        }

        @Override
        public void setPerm(PermissionSet set, int value) {
            set.setViewSubClaims(value);
        }
    },
    CONTAINERS{
        @Override
        public int getPerm(PermissionSet set) {
            throw new RuntimeException("PermissionRoute was called with an invalid perm. (CONTAINERS called getPerm");
        }

        @Override
        public void setPerm(PermissionSet set, int value) {
            throw new RuntimeException("PermissionRoute was called with an invalid perm. (CONTAINERS called setPerm");
        }

        @Override
        public HashMap<Material, Integer> getListPerms(PermissionSet set) {
            if (set == null)
                return null;
            return set.getContainers();
        }

        @Override
        public void setListPerms(PermissionSet set, Material material, int value){
            set.getContainers().replace(material, value);
        }
    };

    PermissionRoute(){

    }

    public abstract int getPerm(PermissionSet set);

    public abstract void setPerm(PermissionSet set, int value);

    public HashMap<Material, Integer> getListPerms(PermissionSet set){
        throw new RuntimeException("PermissionRoute was called with an invalid perm");
    }

    public void setListPerms(PermissionSet set, Material material, int value){
        throw new RuntimeException("PermissionRoute was called with an invalid perm");
    }
}
