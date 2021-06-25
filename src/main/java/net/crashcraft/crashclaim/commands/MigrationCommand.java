package net.crashcraft.crashclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.migration.MigrationAdapter;
import net.crashcraft.crashclaim.migration.MigrationManager;
import org.bukkit.Bukkit;
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
            sender.sendMessage(Localization.MIGRATE__WARNING_PLAYERS_ONLINE.getMessage());
        }
        sender.sendMessage(Localization.MIGRATE__WARNING_BACKUP.getMessage());

        selectedAdaptor = manager.getMigrationAdaptor(adaptor);

        if (selectedAdaptor == null){
            sender.sendMessage(Localization.MIGRATE__NO_ADAPTER.getMessage());
            return;
        }

        sender.sendMessage(Localization.MIGRATE__SELECTED.getMessage("adapter", selectedAdaptor.getIdentifier()));
    }

    @Subcommand("list")
    public void onList(CommandSender sender){
        sender.sendMessage(Localization.MIGRATE__LIST__TITLE.getMessage());
        for (MigrationAdapter adapter : manager.getAdapters()){
            String error = adapter.checkRequirements(manager);

            sender.sendMessage(Localization.MIGRATE__LIST__MESSAGE.getMessage(
                    "identifier", adapter.getIdentifier(),
                    "status", error == null ? Localization.MIGRATE__LIST__AVAILABLE.getRawMessage() : Localization.MIGRATE__LIST__DISABLED.getRawMessage(),
                    "error", error
            ));
        }
    }

    @Subcommand("confirm")
    public void onConfirmation(CommandSender sender){
        if (selectedAdaptor == null){
            sender.sendMessage(Localization.MIGRATE__CONFIRM__NOT_SELECTED.getMessage());
            return;
        }

        String error = selectedAdaptor.checkRequirements(manager);
        if (error != null){
            sender.sendMessage(Localization.MIGRATE__CONFIRM__FAILED_REQUIREMENTS.getMessage("error", error));
            return;
        }

        sender.sendMessage(Localization.MIGRATE__CONFIRM__START_MESSAGE.getMessage("identifier", selectedAdaptor.getIdentifier()));

        String migrateError = manager.migrate(selectedAdaptor);

        if (migrateError != null){
            sender.sendMessage(Localization.MIGRATE__CONFIRM__ERROR.getMessage("error", migrateError));
            return;
        }

        sender.sendMessage(Localization.MIGRATE__CONFIRM__SUCCESS.getMessage("identifier", selectedAdaptor.getIdentifier()));
    }

    @Subcommand("cancel")
    public void onCancel(CommandSender sender){
        sender.sendMessage(Localization.MIGRATE__CANCEL.getMessage());
        selectedAdaptor = null;
    }
}
