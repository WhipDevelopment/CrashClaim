package net.crashcraft.whipclaim.permissions;

import net.crashcraft.whipclaim.claimobjects.GlobalPermissionSet;
import net.crashcraft.whipclaim.claimobjects.PermState;
import net.crashcraft.whipclaim.claimobjects.PermissionSet;
import net.crashcraft.whipclaim.claimobjects.PlayerPermissionSet;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;

public enum  PermissionRoute {
    BUILD{
        @Override
        public int getPerm(PlayerPermissionSet set) {
            if (set == null)
                return -1;
            return set.getBuild();
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            set.setBuild(value);
        }

        @Override
        public int getPerm(GlobalPermissionSet set) {
            if (set == null)
                return -1;
            return set.getBuild();
        }

        @Override
        public void setPerm(GlobalPermissionSet set, int value) {
            set.setBuild(value);
        }

    },
    INTERACTIONS{
        @Override
        public int getPerm(PlayerPermissionSet set) {
            if (set == null)
                return -1;
            return set.getInteractions();
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            set.setInteractions(value);
        }

        @Override
        public int getPerm(GlobalPermissionSet set) {
            if (set == null)
                return -1;
            return set.getInteractions();
        }

        @Override
        public void setPerm(GlobalPermissionSet set, int value) {
            set.setInteractions(value);
        }
    },
    ENTITIES{
        @Override
        public int getPerm(PlayerPermissionSet set) {
            if (set == null)
                return -1;
            return set.getEntities();
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            set.setEntities(value);
        }

        @Override
        public int getPerm(GlobalPermissionSet set) {
            if (set == null)
                return -1;
            return set.getEntities();
        }

        @Override
        public void setPerm(GlobalPermissionSet set, int value) {
            set.setEntities(value);
        }
    },
    EXPLOSIONS{
        @Override
        public int getPerm(PlayerPermissionSet set) {
            if (set == null)
                return -1;
            return set.getExplosions();
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            set.setExplosions(value);
        }

        @Override
        public int getPerm(GlobalPermissionSet set) {
            if (set == null)
                return -1;
            return set.getExplosions();
        }

        @Override
        public void setPerm(GlobalPermissionSet set, int value) {
            set.setExplosions(value);
        }
    },
    TELEPORTATION{
        @Override
        public int getPerm(PlayerPermissionSet set) {
            if (set == null)
                return -1;
            return set.getTeleportation();
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            set.setTeleportation(value);
        }

        @Override
        public int getPerm(GlobalPermissionSet set) {
            if (set == null)
                return -1;
            return set.getTeleportation();
        }

        @Override
        public void setPerm(GlobalPermissionSet set, int value) {
            set.setTeleportation(value);
        }
    },
    PISTONS{
        @Override
        public int getPerm(PlayerPermissionSet set) {
            return PermState.NEUTRAL;
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            throw new RuntimeException("Unsupported operation in permission class");
        }

        @Override
        public int getPerm(GlobalPermissionSet set) {
            if (set == null)
                return -1;
            return set.getPistons();
        }

        @Override
        public void setPerm(GlobalPermissionSet set, int value) {
            set.setPistons(value);
        }
    },
    FLUIDS{
        @Override
        public int getPerm(PlayerPermissionSet set) {
            return PermState.NEUTRAL;
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            throw new RuntimeException("Unsupported operation in permission class");
        }

        @Override
        public int getPerm(GlobalPermissionSet set) {
            if (set == null)
                return PermState.DISABLE;
            return set.getFluids();
        }

        @Override
        public void setPerm(GlobalPermissionSet set, int value) {
            set.setFluids(value);
        }
    },
    MODIFY_PERMISSIONS{
        @Override
        public int getPerm(PlayerPermissionSet set) {
            if (set == null)
                return PermState.NEUTRAL;
            return set.getModifyPermissions();
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            set.setModifyPermissions(value);
        }

        @Override
        public int getPerm(GlobalPermissionSet set) {
            return PermState.DISABLE;
        }

        @Override
        public void setPerm(GlobalPermissionSet set, int value) {
            throw new RuntimeException("Unsupported operation in permission class");
        }
    },
    MODIFY_CLAIM{
        @Override
        public int getPerm(PlayerPermissionSet set) {
            if (set == null)
                return PermState.NEUTRAL;
            return set.getModifyClaim();
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            set.setModifyClaim(value);
        }

        @Override
        public int getPerm(GlobalPermissionSet set) {
            return PermState.DISABLE;
        }

        @Override
        public void setPerm(GlobalPermissionSet set, int value) {
            throw new RuntimeException("Unsupported operation in permission class");
        }
    },
    VIEW_SUB_CLAIMS{
        @Override
        public int getPerm(PlayerPermissionSet set) {
            if (set == null)
                return PermState.NEUTRAL;
            return set.getViewSubClaims();
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            set.setViewSubClaims(value);
        }

        @Override
        public int getPerm(GlobalPermissionSet set) {
            if (set == null)
                return PermState.DISABLE;
            return set.getViewSubClaims();
        }

        @Override
        public void setPerm(GlobalPermissionSet set, int value) {
            set.setViewSubClaims(value);
        }
    },
    CONTAINERS{
        @Override
        public int getPerm(PlayerPermissionSet set) {
            throw new RuntimeException("PermissionRoute was called with an invalid perm. (CONTAINERS called getPerm");
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            throw new RuntimeException("PermissionRoute was called with an invalid perm. (CONTAINERS called setPerm");
        }

        @Override
        public int getPerm(GlobalPermissionSet set) {
            throw new RuntimeException("PermissionRoute was called with an invalid perm. (CONTAINERS called getPerm");
        }

        @Override
        public void setPerm(GlobalPermissionSet set, int value) {
            throw new RuntimeException("PermissionRoute was called with an invalid perm. (CONTAINERS called setPerm");
        }

        public void setPerm(PlayerPermissionSet set, int value, Material material) {
            set.getContainers().put(material, value);
        }

        public int getPerm(PlayerPermissionSet set, Material material){
            if (set == null)
                return PermState.NEUTRAL;
            Integer integer = set.getContainers().get(material);

            return integer == null ? PermState.NEUTRAL : integer;
        }

        public void setPerm(GlobalPermissionSet set, int value, Material material) {
            set.getContainers().put(material, value == PermState.NEUTRAL ? PermState.DISABLE : value);
        }

        public int getPerm(GlobalPermissionSet set, Material material){
            if (set == null)
                return PermState.DISABLE;

            Integer integer = set.getContainers().get(material);

            return integer == null ? PermState.DISABLE : integer;
        }
    };

    PermissionRoute(){

    }

    public abstract int getPerm(GlobalPermissionSet set);

    public abstract void setPerm(GlobalPermissionSet set, int value);

    public abstract int getPerm(PlayerPermissionSet set);

    public abstract void setPerm(PlayerPermissionSet set, int value);

    public void setPerm(GlobalPermissionSet set, int value, Material material) {
        throw new RuntimeException("PermissionRoute was called with an invalid perm. (CONTAINERS called setPerm");
    }

    public int getPerm(GlobalPermissionSet set, Material material){
        throw new RuntimeException("PermissionRoute was called with an invalid perm. (CONTAINERS called getPerm");
    }

    public void setPerm(PlayerPermissionSet set, int value, Material material) {
        throw new RuntimeException("PermissionRoute was called with an invalid perm. (CONTAINERS called setPerm");
    }

    public int getPerm(PlayerPermissionSet set, Material material){
        throw new RuntimeException("PermissionRoute was called with an invalid perm. (CONTAINERS called getPerm");
    }
}
