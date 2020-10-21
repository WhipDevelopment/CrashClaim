package net.crashcraft.whipclaim.commands.modes;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import com.comphenix.protocol.ProtocolManager;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.listeners.ProtocalListener;
import net.crashcraft.whipclaim.visualize.VisualizationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class ModeCommand extends BaseCommand {
    private ClaimDataManager manager;

    private SubClaimCommand subClaimCommand;
    private ClaimModeCommand claimModeCommand;

    private HashMap<UUID, ClaimModeProvider> modeState;

    public ModeCommand(WhipClaim whipClaim, ProtocolManager protocolManager){
        manager = whipClaim.getDataManager();
        VisualizationManager visualizationManager = whipClaim.getVisualizationManager();

        subClaimCommand = new SubClaimCommand(manager, visualizationManager, this);
        claimModeCommand = new ClaimModeCommand(manager, visualizationManager, this);

        Bukkit.getPluginManager().registerEvents(subClaimCommand, whipClaim);
        Bukkit.getPluginManager().registerEvents(claimModeCommand, whipClaim);

        modeState = new HashMap<>();

        new ProtocalListener(protocolManager, whipClaim, claimModeCommand, subClaimCommand);
    }

    @CommandAlias("claim")
    @CommandPermission("crashclaim.user.claim")
    public void onClaim(Player player){
        UUID uuid = player.getUniqueId();

        if (modeState.containsKey(uuid)) {
            ClaimModeProvider provider = modeState.get(uuid);
            provider.cleanup(uuid);

            if (provider instanceof ClaimModeCommand){
                modeState.remove(uuid);
                player.sendMessage(ChatColor.RED + "Claim mode disabled");
                return;
            }
        }
        modeState.put(uuid, claimModeCommand);
        claimModeCommand.onClaim(player);
    }

    @CommandAlias("subClaim")
    @CommandPermission("crashclaim.user.subclaim")
    public void onSubClaim(Player player){
        UUID uuid = player.getUniqueId();
        if (modeState.containsKey(uuid)) {
            ClaimModeProvider provider = modeState.get(uuid);
            provider.cleanup(uuid);

            if (provider instanceof SubClaimCommand){
                modeState.remove(uuid);
                player.sendMessage(ChatColor.RED + "Sub Claim mode disabled");
                return;
            }
        }
        modeState.put(uuid, subClaimCommand);
        subClaimCommand.subclaim(player);
    }

    public void signalDisabled(UUID uuid){
        modeState.remove(uuid);
    }
}
