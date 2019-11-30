package net.crashcraft.whipclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import net.crashcraft.whipclaim.visualize.VisualGroup;
import net.crashcraft.whipclaim.visualize.VisualizationManager;
import org.bukkit.entity.Player;

@CommandAlias("hideclaims")
public class HideClaimsCommand extends BaseCommand {
    private VisualizationManager visualizationManager;

    public HideClaimsCommand(VisualizationManager visualizationManager){
        this.visualizationManager = visualizationManager;
    }

    public void onHide(Player player){
        VisualGroup group = visualizationManager.fetchVisualGroup(player, false);

        if (group != null){
            group.removeAllVisuals();
        }
    }
}
