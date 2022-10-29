package net.crashcraft.crashclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.PermState;
import net.crashcraft.crashclaim.claimobjects.permission.GlobalPermissionSet;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.localization.LocalizationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;

@CommandAlias("claiminfo")
@CommandPermission("crashclaim.admin.claiminfo")
public class ClaimInfoCommand extends BaseCommand {
    private final ClaimDataManager manager;

    public ClaimInfoCommand(ClaimDataManager manager){
        this.manager = manager;
    }

    @Default
    public void onClaimInfo(Player player){
        Location location = player.getLocation();
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim != null){
            GlobalPermissionSet set = claim.getPerms().getGlobalPermissionSet();

            final String enabled = Localization.CLAIM_INFO__STATUS_ENABLED.getRawMessage();
            final String disabled = Localization.CLAIM_INFO__STATUS_DISABLED.getRawMessage();

            LocalizationUtils.sendMessageList(player, Localization.CLAIM_INFO__MESSAGE.getMessageList(
                    player,
                    "min_x", Integer.toString(claim.getMinX()),
                    "min_z", Integer.toString(claim.getMinZ()),
                    "max_x", Integer.toString(claim.getMaxX()),
                    "max_z", Integer.toString(claim.getMaxZ()),
                    "id", Integer.toString(claim.getId()),
                    "name", claim.getName(),
                    "owner", Bukkit.getOfflinePlayer(claim.getOwner()).getName(),
                    "build_status", set.getBuild() == PermState.ENABLED ? enabled : disabled,
                    "entities_status", set.getEntities() == PermState.ENABLED ? enabled : disabled,
                    "interactions_status", set.getInteractions() == PermState.ENABLED ? enabled : disabled,
                    "view_sub_claims_status", set.getViewSubClaims() == PermState.ENABLED ? enabled : disabled,
                    "teleportation_status", set.getTeleportation() == PermState.ENABLED ? enabled : disabled,
                    "explosions_status", set.getExplosions() == PermState.ENABLED ? enabled : disabled,
                    "fluids_status", set.getFluids() == PermState.ENABLED ? enabled : disabled,
                    "pistons_status", set.getPistons() == PermState.ENABLED ? enabled : disabled
            ));

            for (Map.Entry<Material, Integer> entry :set.getContainers().entrySet()) {
                player.spigot().sendMessage(Localization.CLAIM_INFO__CONTAINER_MESSAGE.getMessage(
                        player,
                        "name", CrashClaim.getPlugin().getMaterialName().getMaterialName(entry.getKey()),
                        "status", entry.getValue() == PermState.ENABLED ? enabled : disabled
                ));
            }
        } else {
            player.spigot().sendMessage(Localization.CLAIM_INFO__NO_CLAIM.getMessage(player));
        }
    }
}
