package net.crashcraft.crashclaim.localization;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class CrashClaimExpansion extends PlaceholderExpansion {
    private final CrashClaim crashClaim;

    public CrashClaimExpansion(CrashClaim crashClaim){
        this.crashClaim = crashClaim;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player != null){
            switch (params.toLowerCase()) {
                case "total_owned_claims" -> {
                    return Integer.toString(crashClaim.getDataManager().getNumberOwnedClaims(player.getUniqueId()));
                }
                case "total_owned_parent_claims" -> {
                    return Integer.toString(crashClaim.getDataManager().getNumberOwnedParentClaims(player.getUniqueId()));
                }
                case "current_claim_owner" -> {
                    if (!player.isOnline()){
                        return "";
                    }

                    Claim claim = crashClaim.getDataManager().getClaim(player.getPlayer().getLocation());

                    if (claim == null){
                        return "";
                    }

                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(claim.getOwner());
                    return offlinePlayer.getName();
                }
            }
        }
        return null;
    }

    @Override
    public @NotNull String getIdentifier() {
        return crashClaim.getName();
    }

    @Override
    public @NotNull String getAuthor() {
        return crashClaim.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return crashClaim.getDescription().getVersion();
    }

    @Override
    public boolean persist(){
        return true;
    }
}
