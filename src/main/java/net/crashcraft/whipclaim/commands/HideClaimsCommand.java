package net.crashcraft.whipclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.crashcraft.whipclaim.visualize.VisualizationManager;
import net.crashcraft.whipclaim.visualize.api.VisualGroup;
import org.bukkit.entity.Player;

@CommandAlias("hideclaims")
@CommandPermission("crashclaim.user.hideclaims")
public class HideClaimsCommand extends BaseCommand {
    private VisualizationManager visualizationManager;

    public HideClaimsCommand(VisualizationManager visualizationManager){
        this.visualizationManager = visualizationManager;
    }

    @Default
    public void onHide(Player player){
        VisualGroup group = visualizationManager.fetchVisualGroup(player, false);

        if (group != null){
            group.removeAllVisuals();
        }
    }
}
