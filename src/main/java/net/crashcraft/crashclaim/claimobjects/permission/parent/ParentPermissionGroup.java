package net.crashcraft.crashclaim.claimobjects.permission.parent;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import net.crashcraft.crashclaim.claimobjects.PermState;
import net.crashcraft.crashclaim.claimobjects.PermissionGroup;
import net.crashcraft.crashclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.crashclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.crashclaim.permissions.PermissionRoute;

import java.util.HashMap;
import java.util.UUID;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class,
        property = "@object_id")
@JsonTypeName("ParentPermissionGroup")
public class ParentPermissionGroup extends PermissionGroup {
    public ParentPermissionGroup() {

    }

    public ParentPermissionGroup(BaseClaim owner, GlobalPermissionSet globalPermissionSet, HashMap<UUID, PlayerPermissionSet> playerPermissions) {
        super(owner, globalPermissionSet, playerPermissions);
    }

    @Override
    public PlayerPermissionSet createPlayerPermissionSet() {
        return new PlayerPermissionSet(PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL,
                new HashMap<>(), PermState.NEUTRAL, PermState.DISABLE, PermState.DISABLE);
    }

    @Override
    public GlobalPermissionSet createGlobalPermissionSet() {
        return new GlobalPermissionSet(PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, PermState.ENABLED, PermState.ENABLED,
                new HashMap<>(), PermState.DISABLE, PermState.DISABLE, PermState.DISABLE);
    }

    @Override
    public int checkGlobalValue(int value, PermissionRoute route) {
        if (value == PermState.NEUTRAL) {
            return PermState.DISABLE;
        } else {
            return value;
        }
    }

    @Override
    public int checkPlayerValue(int value, PermissionRoute route) {
        switch (route){
            case MODIFY_CLAIM:
            case MODIFY_PERMISSIONS:
                if (value == PermState.NEUTRAL) {
                    return PermState.DISABLE;
                } else {
                    return value;
                }
            default:
                return value;
        }
    }

    //JSON needs this


}
