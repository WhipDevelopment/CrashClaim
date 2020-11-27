package net.crashcraft.crashclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import net.crashcraft.crashclaim.CrashClaim;

public class CommandManager {
    private PaperCommandManager commandManager;

    public CommandManager(CrashClaim claim) {
        commandManager = new PaperCommandManager(claim);
    }

    public void registerCommand(BaseCommand command){
        commandManager.registerCommand(command);
    }
}
