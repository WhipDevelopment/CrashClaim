package net.crashcraft.whipclaim.menus;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AllClaimListMenu {
    public AllClaimListMenu(Player player, GUI previous){
        ClaimDataManager manager = WhipClaim.getPlugin().getDataManager();
        Set<Claim> claims = new HashSet<>();

        Set<Integer> cla = manager.getOwnedClaims(player.getUniqueId());
        if (cla != null) {
            for (Integer id : cla) {
                claims.add(manager.getClaim(id));
            }
        }

        Set<Integer> subClaims = manager.getOwnedSubClaims(player.getUniqueId());
        if (subClaims != null) {
            for (Integer id : subClaims) {
                Claim claim = manager.getParentClaim(id);
                claims.add(claim);
            }
        }

        new RealClaimListMenu(player, previous, "Claim List", null, new ArrayList<>(claims), (p, claim) -> {
            new ClaimMenu(player, (Claim) claim).open();
            return null;
        }).open();
    }
}
