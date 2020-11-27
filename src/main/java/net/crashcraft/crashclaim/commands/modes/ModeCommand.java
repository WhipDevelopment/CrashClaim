package net.crashcraft.crashclaim.commands.modes;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import com.comphenix.protocol.ProtocolManager;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.listeners.ProtocalListener;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
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

    public ModeCommand(CrashClaim crashClaim, ProtocolManager protocolManager){
        manager = crashClaim.getDataManager();
        VisualizationManager visualizationManager = crashClaim.getVisualizationManager();

        subClaimCommand = new SubClaimCommand(manager, visualizationManager, this);
        claimModeCommand = new ClaimModeCommand(manager, visualizationManager, this);

        Bukkit.getPluginManager().registerEvents(subClaimCommand, crashClaim);
        Bukkit.getPluginManager().registerEvents(claimModeCommand, crashClaim);

        modeState = new HashMap<>();

        new ProtocalListener(protocolManager, crashClaim, claimModeCommand, subClaimCommand);
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
