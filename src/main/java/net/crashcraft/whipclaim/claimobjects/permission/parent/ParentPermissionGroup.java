package net.crashcraft.whipclaim.claimobjects.permission.parent;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.claimobjects.PermState;
import net.crashcraft.whipclaim.claimobjects.PermissionGroup;
import net.crashcraft.whipclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.whipclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.whipclaim.permissions.PermissionRoute;

import java.io.Serializable;
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
                new HashMap<>(), PermState.DISABLE, PermState.DISABLE);
    }

    @Override
    public GlobalPermissionSet createGlobalPermissionSet() {
        return new GlobalPermissionSet(PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,
                new HashMap<>(), PermState.DISABLE, PermState.DISABLE);
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
            case VIEW_SUB_CLAIMS:
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
