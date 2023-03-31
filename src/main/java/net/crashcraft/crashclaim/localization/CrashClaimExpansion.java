package net.crashcraft.crashclaim.localization;

import com.google.common.base.Enums;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.PermState;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.claim.BlockClaimVisual;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class CrashClaimExpansion extends PlaceholderExpansion {

    private final CrashClaim crashClaim;
    private final List<String> placeholders;

    public CrashClaimExpansion(CrashClaim crashClaim){
        this.crashClaim = crashClaim;
        this.placeholders = Stream.of(
                    "total_owned_claims",
                    "total_owned_parent_claims",
                    "current_claim_owner",
                    "visual_status",
                    "has_permission_<PERMISSION>"
            ).map(placeholder -> '%' + getIdentifier() + '_' + placeholder + '%')
            .toList();
    }

    @Override
    public String onRequest(@Nullable OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer != null) {
            switch (params.toLowerCase()) {
                case "total_owned_claims" -> {
                    return Integer.toString(crashClaim.getDataManager().getNumberOwnedClaims(offlinePlayer.getUniqueId()));
                }
                case "total_owned_parent_claims" -> {
                    return Integer.toString(crashClaim.getDataManager().getNumberOwnedParentClaims(offlinePlayer.getUniqueId()));
                }
                case "current_claim_owner" -> {
                    if (!offlinePlayer.isOnline()){
                        return null;
                    }

                    Player player = offlinePlayer.getPlayer();

                    if (player == null){
                        return null;
                    }

                    Claim claim = crashClaim.getDataManager().getClaim(player.getLocation());

                    if (claim == null){
                        return "";
                    }

                    return Bukkit.getOfflinePlayer(claim.getOwner()).getName();
                }
                case "visual_status" -> {
                    if (!offlinePlayer.isOnline()){
                        return null;
                    }

                    Player onlinePlayer = offlinePlayer.getPlayer();

                    if (onlinePlayer == null){
                        return null;
                    }

                    VisualGroup group = CrashClaim.getPlugin().getVisualizationManager().fetchVisualGroup(onlinePlayer, false);

                    if (group == null){
                        return Localization.PLACEHOLDERAPI__VISUAL_STATUS_HIDDEN.getRawMessage();
                    }

                    for (BaseVisual visual : group.getActiveVisuals()){
                        if (visual.getClaim() != null){
                            return Localization.PLACEHOLDERAPI__VISUAL_STATUS_SHOWN.getRawMessage();
                        }
                    }

                    return Localization.PLACEHOLDERAPI__VISUAL_STATUS_HIDDEN.getRawMessage();
                }
            }

            if (params.startsWith("has_permission")) {
                if (!offlinePlayer.isOnline()) {
                    return null;
                }

                final var player = offlinePlayer.getPlayer();

                // To get rid of IDE warnings
                if (player == null) {
                    return null;
                }

                final var permission = params.replace("has_permission_", "");

                if (permission.isEmpty()) {
                    return null;
                }

                final var route = Enums.getIfPresent(PermissionRoute.class, permission).orNull();

                if (route == null) {
                    crashClaim.getSLF4JLogger().warn("[placeholder] Unknown permission route '" + permission + "' (" + params + ")");
                    return null;
                }

                final var claim = crashClaim.getDataManager().getClaim(player.getLocation());

                // There's no claim at player's location
                if (claim == null) {
                    return "UNKNOWN";
                }

                // Player is the owner of the claim
                if (claim.getOwner().equals(player.getUniqueId())) {
                    return "ENABLED";
                }

                // Player is in bypass mode
                if (PermissionHelper.getPermissionHelper().getBypassManager().isBypass(player.getUniqueId())) {
                    return "ENABLED";
                }

                return claim.hasPermission(player.getUniqueId(), player.getLocation(), route) ? "ENABLED" : "DISABLED";
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

    @NotNull
    @Override
    public List<String> getPlaceholders() {
        return placeholders;
    }

    @Override
    public boolean persist(){
        return true;
    }
}
