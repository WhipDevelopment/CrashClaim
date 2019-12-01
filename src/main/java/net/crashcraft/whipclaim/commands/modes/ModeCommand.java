package net.crashcraft.whipclaim.commands.modes;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.data.StaticClaimLogic;
import net.crashcraft.whipclaim.visualize.VisualizationManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ModeCommand extends BaseCommand {
    private ClaimDataManager manager;

    private SubClaimCommand subClaimCommand;
    private ClaimModeCommand claimModeCommand;

    private HashMap<UUID, ClaimModeProvider> modeState;

    public ModeCommand(WhipClaim whipClaim){
        manager = whipClaim.getDataManager();
        VisualizationManager visualizationManager = whipClaim.getVisualizationManager();

        subClaimCommand = new SubClaimCommand(manager, visualizationManager);
        claimModeCommand = new ClaimModeCommand(manager, visualizationManager);

        Bukkit.getPluginManager().registerEvents(subClaimCommand, whipClaim);
        Bukkit.getPluginManager().registerEvents(claimModeCommand, whipClaim);

        modeState = new HashMap<>();
    }

    @CommandAlias("claim")
    public void onClaim(Player player){
        UUID uuid = player.getUniqueId();
        if (modeState.containsKey(uuid)) {
            modeState.get(uuid).cleanup(uuid);
        }
        claimModeCommand.onClaim(player);
    }

    @CommandAlias("subClaim")
    public void onSubClaim(Player player){
        UUID uuid = player.getUniqueId();
        if (modeState.containsKey(uuid)) {
            modeState.get(uuid).cleanup(uuid);
        }
        subClaimCommand.subclaim(player);
    }

    @CommandAlias("debug")
    public void debug(Player player){
        Location location = player.getLocation();
        ArrayList<Integer> claims = manager.temporaryTestGetChunkMap().get(player.getWorld().getUID()).get(StaticClaimLogic.getChunkHashFromLocation(location.getBlockX(), location.getBlockZ()));
        ArrayList<Claim> claimList = new ArrayList<>();
        for (Integer integer : claims){
            claimList.add(manager.temporaryTestGetClaimMap().get(integer));
        }

        for (Claim claim : claimList){
            System.out.println(claim.getId());
            System.out.println("|--Ux  " + claim.getUpperCornerX());
            System.out.println("|--Uz  " + claim.getUpperCornerZ());
            System.out.println("|--Lx  " + claim.getLowerCornerX());
            System.out.println("|--Lz  " + claim.getLowerCornerZ());
        }
    }
}
