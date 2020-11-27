package net.crashcraft.crashclaim.api;

import net.crashcraft.crashclaim.menus.list.ClaimListMenu;
import org.bukkit.entity.Player;

public class CrashClaimAPI {
    public CrashClaimAPI(){

    }

    public void openClaimListMenu(Player player){
        new ClaimListMenu(player, null).open();
    }
}
