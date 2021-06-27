package net.crashcraft.crashclaim.permissions;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.PermState;
import net.crashcraft.crashclaim.claimobjects.PermissionGroup;
import net.crashcraft.crashclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.crashclaim.claimobjects.permission.PlayerPermissionSet;
import org.bukkit.Material;

import java.util.UUID;

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
            return PermState.ENABLED; //Enabled so we cant set it as players
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            throw new RuntimeException("Unsupported operation in permission class");
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
            return PermState.ENABLED; //Enabled so we cant set it as players
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
            return PermState.ENABLED; //Enabled so we can set it as players
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
            int old = 4;

            for (Material material : CrashClaim.getPlugin().getDataManager().getPermissionSetup().getTrackedContainers()){
                if (old == 4){
                    old = CONTAINERS.getPerm(set, material);
                    continue;
                }

                if (old != (CONTAINERS.getPerm(set, material))){
                    return 4;
                }
            }

            return old;
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            throw new RuntimeException("PermissionRoute was called with an invalid perm. (CONTAINERS called setPerm");
        }

        @Override
        public int getPerm(GlobalPermissionSet set) {
            int old = 4;

            for (Material material : CrashClaim.getPlugin().getDataManager().getPermissionSetup().getTrackedContainers()){
                if (old == 4){
                    old = CONTAINERS.getPerm(set, material);
                    continue;
                }

                if (old != (CONTAINERS.getPerm(set, material))){
                    return 4;
                }
            }

            return old;
        }

        @Override
        public void setPerm(GlobalPermissionSet set, int value) {
            throw new RuntimeException("PermissionRoute was called with an invalid perm. (CONTAINERS called setPerm");
        }

        public void setPerm(PlayerPermissionSet set, int value, Material material) {
            set.setContainer(material, value);
        }

        public int getPerm(PlayerPermissionSet set, Material material){
            if (set == null)
                return PermState.NEUTRAL;

            Integer integer = set.getContainers().get(material);

            return integer == null ? set.getDefaultConatinerValue() : integer;
        }

        public void setPerm(GlobalPermissionSet set, int value, Material material) {
            set.setContainer(material, value);
        }

        public int getPerm(GlobalPermissionSet set, Material material){
            if (set == null)
                return PermState.DISABLE;

            Integer integer = set.getContainers().get(material);

            return integer == null ? set.getDefaultConatinerValue() : integer;
        }
    },
    /**
     * Allow Pistons
     * Allow Fluids
     *
     * Anything past this point can return a 4 to signify that getPermission could be completed but could not find a common value
     */
    MISC {
        @Override
        public int getPerm(PlayerPermissionSet set) {
            return PermState.ENABLED;
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            throw new RuntimeException("PermissionRoute was called with an invalid perm. (MISC called setPerm");
        }

        @Override
        public int getPerm(GlobalPermissionSet set) {
            return set.getPistons() == set.getPistons() ? set.getPistons() : 4;
        }

        @Override
        public void setPerm(GlobalPermissionSet set, int value) {
            if (value == PermState.NEUTRAL){
                throw new RuntimeException("PermissionRoute was called with an invalid perm. (MISC called setPerm with NUETRAL which is unsupported");
            }

            PISTONS.setPerm(set, value);
            FLUIDS.setPerm(set, value);
        }
    },
    ADMIN {
        @Override
        public int getPerm(PlayerPermissionSet set) {
            return set.getModifyClaim() == set.getModifyPermissions() ? set.getModifyClaim() : 4;
        }

        @Override
        public void setPerm(PlayerPermissionSet set, int value) {
            MODIFY_CLAIM.setPerm(set, value);
            MODIFY_PERMISSIONS.setPerm(set, value);
        }

        @Override
        public int getPerm(GlobalPermissionSet set) {
            throw new RuntimeException("PermissionRoute was called with an invalid perm. (ADMIN called setPerm");
        }

        @Override
        public void setPerm(GlobalPermissionSet set, int value) {
            throw new RuntimeException("PermissionRoute was called with an invalid perm. (ADMIN called setPerm");
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

    public void postSetPayload(PermissionGroup group, int value, UUID player){
        // noop
    }
}
