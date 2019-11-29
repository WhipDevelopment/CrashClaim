package net.crashcraft.whipclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import net.crashcraft.whipclaim.WhipClaim;

public class CommandManager {
    private PaperCommandManager commandManager;

    public CommandManager(WhipClaim claim) {
        commandManager = new PaperCommandManager(claim);
    }

    public void registerCommand(BaseCommand command){
        commandManager.registerCommand(command);
    }
}
