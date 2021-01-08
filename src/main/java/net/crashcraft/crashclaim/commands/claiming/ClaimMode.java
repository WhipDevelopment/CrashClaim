package net.crashcraft.crashclaim.commands.claiming;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface ClaimMode {
    void click(Player player, Location click);

    void cleanup(UUID player, boolean visuals);
}


    /*
    CLAIM_NONE,
    CLAIM,
    CLAIM_RESIZE,
    SUB_CLAIM_NONE,
    SUB_CLAIM,
    SUB_CLAIM_RESIZE

     */