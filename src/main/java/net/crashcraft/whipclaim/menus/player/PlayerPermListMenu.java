package net.crashcraft.whipclaim.menus.player;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.menu.defaultmenus.PlayerListMenu;
import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerPermListMenu {
    public PlayerPermListMenu(BaseClaim claim, Player viewer, GUI previous){
        ArrayList<UUID> uuids = new ArrayList<>(claim.getPerms().getPlayerPermissions().keySet());

        for (Player player : Bukkit.getOnlinePlayers()){
            if (!uuids.contains(player.getUniqueId()))
                uuids.add(player.getUniqueId());
        }

        uuids.remove(viewer.getUniqueId());    //Cant modify perms of yourself

        if (claim instanceof SubClaim){ //Owners permissions are off limits.
            SubClaim temp = (SubClaim) claim;
            uuids.remove(temp.getParent().getOwner());
        } else if (claim instanceof Claim){
            Claim temp = (Claim) claim;
            uuids.remove(temp.getOwner());
        } else {
            throw new RuntimeException("Claim was not of known type.");
        }

        new PlayerListMenu(viewer, previous, uuids, (player, uuid) -> {
            new PlayerPermissionMenu(player, claim.getPerms(), uuid).open();
            return "";
        }).open();
    }
}
