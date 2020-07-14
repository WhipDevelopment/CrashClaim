package net.crashcraft.whipclaim.api;

import net.crashcraft.whipclaim.menus.list.ClaimListMenu;
import org.bukkit.entity.Player;

public class WhipClaimAPI {
    public WhipClaimAPI(){

    }

    public void openClaimListMenu(Player player){
        new ClaimListMenu(player, null).open();
    }
}
