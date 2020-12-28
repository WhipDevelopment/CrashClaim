package net.crashcraft.crashclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.crashcraft.crashclaim.migration.MigrationAdapter;
import net.crashcraft.crashclaim.migration.MigrationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandAlias("migratedata")
@CommandPermission("crashclaim.sysadmin.migrate")
public class MigrationCommand extends BaseCommand {
    private final MigrationManager manager;

    private MigrationAdapter selectedAdaptor;

    public MigrationCommand(MigrationManager manager){
        this.manager = manager;
    }

    @Default
    @CommandCompletion("@migrators")
    public void onMigrate(CommandSender sender,  String adaptor){
        if (Bukkit.getServer().getOnlinePlayers().size() > 0){
            sender.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "It is not recommended to initiate a migration when there are players online.");
        }
        sender.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "Backup both the plugin you are migrating data from, and existing CrashClaim data before continuing.\n");

        selectedAdaptor = manager.getMigrationAdaptor(adaptor);

        if (selectedAdaptor == null){
            sender.sendMessage(ChatColor.RED + "There is no migration adapter with that identifier. Try '/migratedata list' for more details on available adaptors.");
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "Selected " + ChatColor.YELLOW + selectedAdaptor.getIdentifier() + ChatColor.GREEN + " for migration");
    }

    @Subcommand("list")
    public void onList(CommandSender sender){
        sender.sendMessage(ChatColor.YELLOW + "Migration Adaptors");
        for (MigrationAdapter adapter : manager.getAdapters()){
            String error = adapter.checkRequirements(manager);
            sender.sendMessage(ChatColor.GREEN + adapter.getIdentifier() + ChatColor.WHITE + ": " +
                    (error == null ? ChatColor.GREEN + "Available" : ChatColor.RED + "Disabled - " + error));
        }
    }

    @Subcommand("confirm")
    public void onConfirmation(CommandSender sender){
        if (selectedAdaptor == null){
            sender.sendMessage(ChatColor.RED + "There is no currently selected migration adaptor.\nUse /migratedata [adaptor] to select a valid adaptor.");
            return;
        }

        String error = selectedAdaptor.checkRequirements(manager);
        if (error != null){
            sender.sendMessage(ChatColor.RED + "There was an error while checking the requirements of the selected migration adaptor.\n" + error);
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + "Starting migration of data using [" + selectedAdaptor.getIdentifier() + "]");
        sender.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "Do not shut down the server until completion.");

        String migrateError = manager.migrate(selectedAdaptor);

        if (migrateError != null){
            sender.sendMessage(ChatColor.RED + "The migration has ended in an error, no data has been saved or modified.\n" + migrateError);
            sender.sendMessage(ChatColor.DARK_RED + "----- WARNING -----");
            sender.sendMessage(ChatColor.RED + "Data saving has been turned off due to the migrator possibly putting the data manager in an unsafe state. \n Restart the server to enable saving of data.");
            sender.sendMessage(ChatColor.DARK_RED + "----- WARNING -----");
            return;
        }

        sender.sendMessage("" + ChatColor.BOLD + ChatColor.GREEN + "Migration of data using [" + selectedAdaptor.getIdentifier() + "] has completed without error");
        sender.sendMessage(ChatColor.GREEN + "It is recommended to restart the server and remove the data that has been migrated from the migrated plugin");

        //TODO print total of claims in memory
    }

    @Subcommand("cancel")
    public void onCancel(CommandSender sender){
        sender.sendMessage(ChatColor.GREEN + "Unselected migration adapter.");
        selectedAdaptor = null;
    }
}
