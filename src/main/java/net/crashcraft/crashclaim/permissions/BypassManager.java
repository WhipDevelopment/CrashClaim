package net.crashcraft.crashclaim.permissions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.UUID;

public class BypassManager implements Listener {
    private final ArrayList<UUID> bypassMode;

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

    public void removeBypass(UUID uuid){
        bypassMode.remove(uuid);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        bypassMode.remove(e.getPlayer().getUniqueId());
    }
}
