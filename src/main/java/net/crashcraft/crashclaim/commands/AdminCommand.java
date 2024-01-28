package net.crashcraft.crashclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.localization.LocalizationUtils;
import net.crashcraft.crashclaim.migration.MigrationAdapter;
import net.crashcraft.crashclaim.migration.MigrationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

@CommandAlias("crashclaim")
public class AdminCommand extends BaseCommand {
    private final CrashClaim crashClaim;
    private final MigrationManager manager;

    public AdminCommand(CrashClaim crashClaim, MigrationManager manager){
        this.crashClaim = crashClaim;
        this.manager = manager;
    }

    @Subcommand("reload")
    @CommandPermission("crashclaim.admin.reload")
    public void reload(CommandSender sender){
        sender.spigot().sendMessage(Localization.RELOAD__RELOADING.getMessage(null));

        crashClaim.loadConfigs(); // Reload configs and their values.
        Localization.rebuildCachedMessages(); // Reload localization config, will load with new language value if changed in config.

        sender.spigot().sendMessage(Localization.RELOAD__RELOADED.getMessage(null));
    }

    @Subcommand("version")
    @CommandPermission("crashclaim.admin.version")
    public void version(CommandSender sender){
        // This message should not be configurable by end user as it is used for debug

        sender.sendMessage(ChatColor.GREEN + crashClaim.getDescription().getName() + ": " + ChatColor.YELLOW + crashClaim.getDescription().getVersion()
                + ChatColor.GREEN + "\nMinecraft Version: " + ChatColor.YELLOW + Bukkit.getMinecraftVersion()
                + ChatColor.GREEN + "\nServer Version: " + ChatColor.YELLOW + Bukkit.getVersion()
                + ChatColor.GOLD + "\nHard Dependencies:");

        for (String name : crashClaim.getDescription().getDepend()){
            Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
            String version = plugin == null ? ChatColor.RED + "Not Available" : plugin.getDescription().getVersion();
            sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.GREEN + name + ": " + ChatColor.YELLOW + version);
        }

        sender.sendMessage(ChatColor.GOLD + "Soft Dependencies:");

        for (String name : crashClaim.getDescription().getSoftDepend()){
            Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
            String version = plugin == null ? "Not Available" : plugin.getDescription().getVersion();
            sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.GREEN + name + ": " + ChatColor.YELLOW + version);
        }
    }

    @Subcommand("migratedata")
    @CommandPermission("crashclaim.sysadmin.migrate")
    public class MigrationCommand extends BaseCommand {
        private MigrationAdapter selectedAdaptor;

        @Default
        @CommandCompletion("@migrators")
        public void onMigrate(CommandSender sender, String adaptor){
            if (Bukkit.getServer().getOnlinePlayers().size() > 0){
                sender.spigot().sendMessage(Localization.MIGRATE__WARNING_PLAYERS_ONLINE.getMessage(null));
            }
            sender.spigot().sendMessage(Localization.MIGRATE__WARNING_BACKUP.getMessage(null));

            selectedAdaptor = manager.getMigrationAdaptor(adaptor);

            if (selectedAdaptor == null){
                sender.spigot().sendMessage(Localization.MIGRATE__NO_ADAPTER.getMessage(null));
                return;
            }

            sender.spigot().sendMessage(Localization.MIGRATE__SELECTED.getMessage(null,
                    "adapter", selectedAdaptor.getIdentifier()));
        }

        @Subcommand("list")
        public void onList(CommandSender sender){
            sender.spigot().sendMessage(Localization.MIGRATE__LIST__TITLE.getMessage(null));
            for (MigrationAdapter adapter : manager.getAdapters()){
                String error = adapter.checkRequirements(manager);

                sender.spigot().sendMessage(Localization.MIGRATE__LIST__MESSAGE.getMessage(
                        null,
                        "identifier", adapter.getIdentifier(),
                        "status", error == null ? Localization.MIGRATE__LIST__AVAILABLE.getRawMessage() : Localization.MIGRATE__LIST__DISABLED.getRawMessage(),
                        "error", error
                ));
            }
        }

        @Subcommand("confirm")
        public void onConfirmation(CommandSender sender){
            if (selectedAdaptor == null){
                LocalizationUtils.sendMessageList(sender, Localization.MIGRATE__CONFIRM__NOT_SELECTED.getMessageList(null));
                return;
            }

            String error = selectedAdaptor.checkRequirements(manager);
            if (error != null){
                LocalizationUtils.sendMessageList(sender, Localization.MIGRATE__CONFIRM__FAILED_REQUIREMENTS.getMessageList(null,
                        "error", error));
                return;
            }

            LocalizationUtils.sendMessageList(sender, Localization.MIGRATE__CONFIRM__START_MESSAGE.getMessageList(null,
                    "identifier", selectedAdaptor.getIdentifier()));

            manager.migrate(selectedAdaptor).thenAccept((migrateError) -> {
                if (migrateError != null){
                    LocalizationUtils.sendMessageList(sender, Localization.MIGRATE__CONFIRM__ERROR.getMessageList(null,
                            "error", migrateError));
                    return;
                }

                LocalizationUtils.sendMessageList(sender, Localization.MIGRATE__CONFIRM__SUCCESS.getMessageList(null,
                        "identifier", selectedAdaptor.getIdentifier()));
            });
        }

        @Subcommand("cancel")
        public void onCancel(CommandSender sender){
            sender.spigot().sendMessage(Localization.MIGRATE__CANCEL.getMessage(null));
            selectedAdaptor = null;
        }
    }
}
