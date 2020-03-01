package net.crashcraft.whipclaim.permissions;

import java.util.ArrayList;
import java.util.UUID;

public class BypassManager {
    private ArrayList<UUID> bypassMode;

    public BypassManager(){
        bypassMode = new ArrayList<>();
    }

    public boolean isBypass(UUID uuid){
        return bypassMode.contains(uuid);
    }

    public boolean toggleBypass(UUID uuid){
        if (bypassMode.contains(uuid)){
            bypassMode.remove(uuid);
            return false;
        } else {
            bypassMode.add(uuid);
            return true;
        }
    }
}
