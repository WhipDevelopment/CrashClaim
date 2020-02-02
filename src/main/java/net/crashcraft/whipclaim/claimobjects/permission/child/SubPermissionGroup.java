package net.crashcraft.whipclaim.claimobjects.permission.child;

import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.claimobjects.PermState;
import net.crashcraft.whipclaim.claimobjects.PermissionGroup;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.whipclaim.permissions.PermissionRoute;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class SubPermissionGroup extends PermissionGroup implements Serializable {
    public SubPermissionGroup() {
    }

    public SubPermissionGroup(BaseClaim owner, GlobalPermissionSet globalPermissionSet, HashMap<UUID, PlayerPermissionSet> playerPermissions) {
        super(owner, globalPermissionSet, playerPermissions);
    }

    @Override
    public PlayerPermissionSet createPlayerPermissionSet() {
        return new PlayerPermissionSet(PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL,
                PermState.NEUTRAL, new HashMap<>(), PermState.NEUTRAL, PermState.NEUTRAL);
    }

    @Override
    public GlobalPermissionSet createGlobalPermissionSet() {
        return new GlobalPermissionSet(PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL,
                PermState.NEUTRAL, new HashMap<>(), PermState.NEUTRAL, PermState.NEUTRAL);
    }

    @Override
    public int checkGlobalValue(int value, PermissionRoute route) {
        return value;
    }

    @Override
    public int checkPlayerValue(int value, PermissionRoute route) {
        return value;
    }
}
