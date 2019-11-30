package net.crashcraft.whipclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.visualize.VisualizationManager;
import org.bukkit.entity.Player;

@CommandAlias("showclaims")
public class ShowClaimsCommand extends BaseCommand {
    private VisualizationManager visualizationManager;
    private ClaimDataManager claimDataManager;

    public ShowClaimsCommand(VisualizationManager visualizationManager, ClaimDataManager claimDataManager){
        this.visualizationManager = visualizationManager;
        this.claimDataManager = claimDataManager;
    }

    @Default
    public void showClaims(Player player){
        visualizationManager.visualizeSuroudningClaims(player, claimDataManager);
    }
}
