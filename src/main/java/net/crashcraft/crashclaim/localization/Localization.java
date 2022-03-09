package net.crashcraft.crashclaim.localization;

import io.papermc.lib.PaperLib;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.config.BaseConfig;
import net.crashcraft.crashclaim.config.ConfigManager;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.craftbukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public enum Localization {
    //PlaceholderAPI
    PLACEHOLDERAPI__VISUAL_STATUS_SHOWN("shown"),
    PLACEHOLDERAPI__VISUAL_STATUS_HIDDEN("hidden"),

    //Generic Localizations
    PVP_DISABLED_INSIDE_CLAIM("<red>PvP is disabled inside of claims!"),
    DISABLED_WORLD("<red>Claiming is disabled inside this world."),
    UPDATE_AVAILABLE("<yellow>An updated <bold>CrashClaim <version></bold> is now available to download on SpigotMC!\n<gold>Download now at https://whips.dev/crashclaim"),
    MAX_CLAIMS_REACHED("<red>You have claimed the maximum amount of claims possible."),

    BYPASS__ENABLED("<green>Enabled claim bypass. You are now bypassing permissions"),
    BYPASS__DISABLED("<red>Disabled claim bypass."),

    EJECT__NO_PERMISSION("<red>You do not have the modify permission ability inside this claim."),
    EJECT__NOT_SAME_CLAIM("<red>That player is not standing in the same claim as you."),
    EJECT__HAS_PERMISSION("<red>That player has the modify permissions ability inside this claim and cannot be ejected."),
    EJECT__BEEN_EJECTED("<red>You have been ejected from the claim you were standing in by another player."),
    EJECT__SUCCESS("<green>You have successfully ejected that player to the edge of the claim."),
    EJECT__NO_CLAIM("<red>There is no claim where you are standing."),

    HIDE_CLAIMS__SUCCESS("<green>Claim visuals have been hidden."),
    SHOW_CLAIMS__SUCCESS("<green>Claim visuals have been shown."),

    CLAIM_SETTINGS__NO_CLAIM("<red>There is no claim where you are standing."),

    UN_CLAIM_ALL__MENU__CONFIRMATION__TITLE("Confirm Delete All Claims"),
    UN_CLAIM_ALL__MENU__CONFIRMATION__MESSAGE(Material.BOOK, 1,
            "<dark_red>Permanently Delete all claims?",
            "<red>Claim Blocks will be restored to ",
            "<red>the contributing parties"),
    UN_CLAIM_ALL__MENU__CONFIRMATION__ACCEPT(Material.GREEN_CONCRETE, 1, "<gold>Accept"),
    UN_CLAIM_ALL__MENU__CONFIRMATION__DENY(Material.RED_CONCRETE, 1, "<gold>Cancel"),
    UN_CLAIM_ALL__NO_PERMISSION("<red>You do not have permission to the claim named <gold><name></gold>."),
    UN_CLAIM_ALL__NO_PERMISSION_IN_ALL("<red>You do not have permission to modify the claim named <gold><name></gold> and all of its sub-claims"),
    UN_CLAIM_ALL__NO_CLAIM("<red>You do not own any to un-claim."),

    UN_CLAIM__MENU__CONFIRMATION__TITLE("Confirm Delete Claim"),
    UN_CLAIM__MENU__CONFIRMATION__MESSAGE(null, 1,
            "<dark_red>Permanently Delete this claim?",
            "<red>Claim Blocks will be restored to ",
            "<red>the contributing parties"),
    UN_CLAIM__MENU__CONFIRMATION__ACCEPT(Material.GREEN_CONCRETE, 1, "<gold>Accept"),
    UN_CLAIM__MENU__CONFIRMATION__DENY(Material.RED_CONCRETE, 1, "<gold>Cancel"),
    UN_CLAIM__NO_PERMISSION("<red>You do not have permission to modify this claim."),
    UN_CLAIM__NO_PERMISSION_IN_ALL("<red>You do not have permission to modify this claim and all of its sub-claims"),
    UN_CLAIM__NO_CLAIM("<red>There is no claim where you are standing."),

    UN_SUBCLAIM__MENU__CONFIRMATION__TITLE("Confirm Delete Sub-Claim"),
    UN_SUBCLAIM__MENU__CONFIRMATION__MESSAGE(null, 1,
            "<dark_red>Permanently Delete this claim?",
            "<red>Claim Blocks will be restored to ",
            "<red>the contributing parties"),
    UN_SUBCLAIM__MENU__CONFIRMATION__ACCEPT(Material.GREEN_CONCRETE, 1, "<gold>Accept"),
    UN_SUBCLAIM__MENU__CONFIRMATION__DENY(Material.RED_CONCRETE, 1, "<gold>Cancel"),
    UN_SUBCLAIM__MENU__NO_PERMISSION("<red>You do not have permission to modify this sub-claim."),
    UN_SUBCLAIM__MENU__NO_CLAIM("<red>There is no sub-claim where you are standing."),

    RELOAD__RELOADING("<yellow>Starting Config Reload..."),
    RELOAD__RELOADED("<green>Finished Reloading Configs."),

    MIGRATE__WARNING_PLAYERS_ONLINE("<red><bold>It is not recommended to initiate a migration when there are players online."),
    MIGRATE__WARNING_BACKUP("<red><bold>Backup both the plugin you are migrating data from, and existing CrashClaim data before continuing."),
    MIGRATE__NO_ADAPTER("<red>There is no migration adapter with that identifier. Try '/migratedata list' for more details on available adaptors."),
    MIGRATE__SELECTED("<green>Selected <yellow><adapter> <green>for migration"),
    MIGRATE__LIST__TITLE("<yellow>Migration Adaptors"),
    MIGRATE__LIST__AVAILABLE("<green>Available"),
    MIGRATE__LIST__DISABLED("<red>Disabled - <error>"),
    MIGRATE__LIST__MESSAGE("<green><identifier><white>: <status>"),
    MIGRATE__CONFIRM__NOT_SELECTED(
            "<red>There is no currently selected migration adaptor.",
            "Use /migratedata [adaptor] to select a valid adaptor."
    ),
    MIGRATE__CONFIRM__FAILED_REQUIREMENTS(
            "<red>There was an error while checking the requirements of the selected migration adaptor.",
            "<error>"
    ),
    MIGRATE__CONFIRM__START_MESSAGE(
            "<yellow>Starting migration of data using [<identifier>]",
            "<yellow><bold>Do not shut down the server until completion."
    ),
    MIGRATE__CONFIRM__ERROR(
            "<red>The migration has ended in an error, no data has been saved or modified.",
            "<error>",
            "<dark_red>----- WARNING -----",
            "<red>Data saving has been turned off due to the migrator possibly putting the data manager in an unsafe state.",
            "Restart the server to enable saving of data.",
            "<dark_red>----- WARNING -----"
    ),
    MIGRATE__CONFIRM__SUCCESS(
            "<bold><green>Migration of data using [<identifier>] has completed without error",
            "<green>It is recommended to restart the server and remove the data that has been migrated from the migrated plugin"
    ),
    MIGRATE__CANCEL("<green>Unselected migration adapter."),

    SHOW__SUBCLAIM__NO_PERMISSION("<red>You need permission to view sub-claims."),
    SHOW__SUBCLAIM__NO_SUBCLAIMS("<green>There are no sub-claims to visualize"),
    SHOW__SUBCLAIM__STAND_INSIDE("<red>You need to stand in a claim to visualize its sub-claims."),

    CLAIM_INFO__STATUS_ENABLED("<dark_green>Enabled"),
    CLAIM_INFO__STATUS_DISABLED("<red>Disabled"),
    CLAIM_INFO__MESSAGE(
            "<gold>Claim Info | <yellow>[<min_x>, <min_z>] - [<max_x>, <max_z>]",
            "<green>Owner: <white><owner>",
            "<gold>Global Permissions",
            "<green>Build <yellow>: <build_status>",
            "<green>Entities <yellow>: <entities_status>",
            "<green>Interactions <yellow>: <interactions_status>",
            "<green>View Sub-Claims <yellow>: <view_sub_claims_status>",
            "<green>Teleportation <yellow>: <teleportation_status>",
            "<green>Explosions <yellow>: <explosions_status>",
            "<green>Fluids <yellow>: <fluids_status>",
            "<green>Pistons <yellow>: <pistons_status>",
            "<gold>Global Container Permissions"
    ),
    CLAIM_INFO__CONTAINER_MESSAGE("<green><name> <yellow>: <status>"),
    CLAIM_INFO__NO_CLAIM("<red>There is no claim where you are standing."),

    NEW_CLAIM__INFO("<yellow>Click two corners to form a new claim. To resize an existing claim click a corner or side to resize in that direction."),
    NEW_CLAIM__CLICK_CORNER("<green>Click an opposite corner to form a new claim."),
    NEW_CLAIM__MIN_SIZE("<red>A claim has to be at least a 5x5."),
    NEW_CLAIM__OVERLAPPING("<red>You cannot claim over an existing claim."),
    NEW_CLAIM__OTHER_ERROR("<red>You cannot claim because of another region or plugin wont allow it."),
    NEW_CLAIM__NOT_ENOUGH_BALANCE("<red>You need <price> coins to claim that area."),
    NEW_CLAIM__SUCCESS("<green>Claim has been successfully created."),
    NEW_CLAIM__ERROR("<red>Error creating claim."),

    NEW_CLAIM__CREATE_MENU__TITLE("Confirm Claim Creation"),
    NEW_CLAIM__CREATE_MENU__MESSAGE(Material.EMERALD, 1,
            "<green>The claim creation will cost: <yellow><price>",
            "<gold>Confirm or deny the creation."),
    NEW_CLAIM__CREATE_MENU__ACCEPT(Material.GREEN_CONCRETE, 1, "<gold>Accept"),
    NEW_CLAIM__CREATE_MENU__DENY(Material.RED_CONCRETE, 1, "<gold>Cancel"),

    NEW_SUBCLAIM__INFO("<yellow>Click two corners to form a new sub-claim. To resize an existing sub-claim click a corner or side to resize in that direction."),
    NEW_SUBCLAIM__CLICK_CORNER("<green>Click an opposite corner to form a sub-claim"),
    NEW_SUBCLAIM__NOT_INSIDE_PARENT("<red>Sub-Claims can only be formed inside of a parent claim."),
    NEW_SUBCLAIM__SUCCESS("<green>Successfully created sub-claim."),
    NEW_SUBCLAIM__MIN_AREA("<red>A sub-claim needs to be at least a 5x5 area."),
    NEW_SUBCLAIM__NEED_PARENT("<red>You cannot form a sub-claim outside of a parent claim."),
    NEW_SUBCLAIM__NO_OVERLAP("<red>You cannot overlap an existing sub-claim."),
    NEW_SUBCLAIM__ERROR("<red>There was an error creating the sub-claim."),

    CLAIM__ENABLED("<green>Claim mode <bold>enabled!"),
    CLAIM__DISABLED("<red>Claim mode <bold>disabled!"),

    SUBCLAIM__ENABLED("<green>Sub-Claiming mode <bold>enabled!"),
    SUBCLAIM__DISABLED("<red>Sub-Claiming mode <bold>disabled!"),
    SUBCLAIM__NO_CLAIM("<red>You need to be standing in a claim to enable sub-claiming mode."),
    SUBCLAIM__NO_PERMISSION("<red>You need permission to modify this sub-claims."),
    SUBCLAIM__ALREADY_RESIZING("<red>The claim your are attempting to resize is already being resized."),

    RESIZE__CLICK_ANOTHER_LOCATION("<green>Click another location to resize the claim."),
    RESIZE__NO_PERMISSION("<red>You do not have permission to modify this claim."),
    RESIZE__INSTRUCTIONS("<red>You need to click the border of the claim to resize it. Grabbing an edge will move it in that direction, grabbing a corner will move it in both directions relative to the corner."),
    RESIZE__SUCCESS("<green>Claim successfully resized"),
    RESIZE__NO_OVERLAP("<red>You cannot overlap other claims."),
    RESIZE__MIN_SIZE("<red>A claim has to be at least a 5x5"),
    RESIZE__CANNOT_FLIP("<red>Claims cannot be flipped, please retry and grab the other edge to expand in this direction"),
    RESIZE__OVERLAP_EXISTING("<red>Sub-Claims cannot overlap over existing sub-claims."),
    RESIZE__ERROR_OTHER("<red>Another region or plugin is preventing the claim from being resized."),
    RESIZE__TRANSACTION_ERROR("<red><error>"),
    RESIZE__NO_LONGER_PERMISSION("<red>You no longer have permission to resize the claim."),

    RESIZE_SUBCLAIM__INSTRUCTIONS("<red>You need to click the border of the sub-claim to resize it. Grabbing an edge will move it in that direction, grabbing a corner will move it in both directions relative to the corner."),
    RESIZE_SUBCLAIM__CLICK_ANOTHER_LOCATION("<green>Click another location to resize the claims."),
    RESIZE_SUBCLAIM__INSIDE_PARENT("<red>Sub-Claims can only be formed inside of a parent claim."),
    RESIZE_SUBCLAIM__NO_OVERLAP("<red>You cannot overlap other sub-claims."),
    RESIZE_SUBCLAIM__MIN_SIZE("<red>A claim has to be at least a 5x5."),
    RESIZE_SUBCLAIM__CANNOT_FLIP("Claims cannot be flipped, please retry and grab the other edge to expand in this direction."),
    RESIZE_SUBCLAIM__SUCCESS("<green>Claim has been successfully resized."),

    RESIZE__MENU__CONFIRMATION__TITLE("Confirm Claim Resize"),
    RESIZE__MENU__CONFIRMATION__MESSAGE(Material.EMERALD, 1,
            "<green>The claim resize will cost: <yellow><price>",
            "<gold>Confirm or deny the resize."),
    RESIZE__MENU__CONFIRMATION__ACCEPT(Material.GREEN_CONCRETE, 1,
            "<gold>Accept"),
    RESIZE__MENU__CONFIRMATION__DENY(Material.RED_CONCRETE, 1,
            "<gold>Cancel"),

    CONTRIBUTION_REFUND("<green>You have received <gold><amount> <green>for a refunded contribution to a claim."),

    MENU__SIMPLE_PERMISSIONS__TITLE("Simple Permissions"),
    MENU__SUB_CLAIM_SIMPLE_PERMISSIONS__TITLE("Simple Sub-Claim Permissions"),
    MENU__ADVANCED_PERMISSIONS__TITLE("Advanced Permissions"),
    MENU__SUB_CLAIM_ADVANCED_PERMISSIONS__TITLE("Advanced Sub-Claim Permissions"),
    MENU__SIMPLE_PERMISSIONS__NO_PERMISSION("<red>You no longer have sufficient permissions to continue"),
    MENU__ADVANCED_PERMISSIONS__NO_PERMISSION("<red>You no longer have sufficient permissions to continue"),

    ALERT__NO_PERMISSIONS__BUILD("<red>You do not have permission to build in this claim"),
    ALERT__NO_PERMISSIONS__INTERACTION("<red>You do not have permission to interact in this claim."),
    ALERT__NO_PERMISSIONS__CONTAINERS("<red>You do not have permission to open containers in this claim."),
    ALERT__NO_PERMISSIONS__ENTITIES("<red>You do not have permission to interact with entities in this claim"),
    ALERT__NO_PERMISSIONS__TELEPORT("<red>You do not have permission to teleport to that claim."),
    ALERT__NO_PERMISSIONS__TELEPORT_RELOCATE("<red>You do not have permission to teleport to that claim. You have been relocated outside of it."),

    // General Visuals

    MENU__GENERAL__INSUFFICIENT_PERMISSION("<red>You no longer have sufficient permissions to continue"),

    MENU__GENERAL__CLAIM_ITEM_NO_OWNER(null, 1,
            "<name>",
            "<green>Coordinates: <yellow><min_x>, <min_z><gold> | <yellow><max_x>, <max_z>",
            "<green>World: <yellow><world>"),

    MENU__GENERAL__CLAIM_ITEM(null, 1,
            "<name>",
            "<green>Coordinates: <yellow><min_x>, <min_z><gold> | <yellow><max_x>, <max_z>",
            "<green>World: <yellow><world>",
            "<green>Owner: <yellow><owner>"),

    MENU__GENERAL__CLAIM_ITEM_PLAYER(null, 1,
            "<gold><name>",
            "<green>Claim Coordinates: <yellow><min_x>, <min_z><gold> | <yellow><max_x>, <max_z>",
            "<green>Claim World: <yellow><world>"),

    MENU__GENERAL__PAGE_DISPLAY(Material.ARROW, 1, "<gold>Page <page> / <page_total>"),
    MENU__GENERAL__NEXT_BUTTON(Material.OAK_BUTTON, 1, "<yellow>Next"),
    MENU__GENERAL__PREVIOUS_BUTTON(Material.OAK_BUTTON, 1, "<yellow>Previous"),
    MENU__GENERAL__BACK_BUTTON(Material.ARROW, 1, "<gold>Back"),

    MENU__LIST_PLAYERS__TITLE("Select Player"),

    // Claim
    MENU__CLAIM__TITLE("Claim Settings"),
    MENU__CLAIM_LIST__TITLE("Claims"),
    MENU__SUB_CLAIM__TITLE("Sub-Claim Settings"),
    MENU__SUB_CLAIM_LIST__TITLE("Sub-Claims"),

    MENU__CLAIM__RENAME__MESSAGE(Material.PAPER, 1, "Enter new claim name"),
    MENU__CLAIM__RENAME__CONFIRMATION("<green>Change claim name to <gold><name>"),

    MENU__CLAIM__ENTRY_MESSAGE__MESSAGE(Material.PAPER, 1, "Enter new claim entry message"),
    MENU__CLAIM__ENTRY_MESSAGE__CONFIRMATION("<green>Change claim entry message to <gold><entry_message>"),

    MENU__CLAIM__EXIT_MESSAGE__MESSAGE(Material.PAPER, 1, "Enter new claim exit message"),
    MENU__CLAIM__EXIT_MESSAGE__CONFIRMATION("<green>Change claim exit message to <gold><exit_message>"),

    // Permissions
    MENU__PERMISSIONS__BUTTONS__PER_PLAYER(Material.PLAYER_HEAD, 1,
            "<gold>Per Player Settings",
            "<green>Edit claim permissions on a per player basis"),
    MENU__PERMISSIONS__BUTTONS__PER_PLAYER_DISABLED(Material.PLAYER_HEAD, 1,
            "<gray>Per Player Settings",
            "<dark_gray>Edit claim permissions on a per player basis"),

    MENU__PERMISSIONS__BUTTONS__GLOBAL(Material.COMPASS, 1,
            "<gold>Global Claim Settings",
            "<green>Set global permissions for your claim"),
    MENU__PERMISSIONS__BUTTONS__GLOBAL_DISABLED(Material.COMPASS, 1,
            "<gray>Global Claim Settings",
            "<dark_gray>Set global permissions for your claim"),

    MENU__PERMISSIONS__BUTTONS__RENAME(Material.ANVIL, 1,
            "<gold>Rename Claim",
            "<green>Rename your claim to easily identify it"),
    MENU__PERMISSIONS__BUTTONS__RENAME_DISABLED(Material.ANVIL, 1,
            "<gray>Rename Claim",
            "<dark_gray>Rename your claim to easily identify it"),

    MENU__PERMISSIONS__BUTTONS__EDIT_ENTRY(Material.ANVIL, 1,
            "<gold>Edit Entry Message",
            "<green>Edit the entry message of your claim"),
    MENU__PERMISSIONS__BUTTONS__EDIT_ENTRY_DISABLED(Material.ANVIL, 1,
            "<gray>Edit Entry Message",
            "<dark_gray>Edit the entry message of your claim"),

    MENU__PERMISSIONS__BUTTONS__EDIT_EXIT(Material.ANVIL, 1,
            "<gold>Edit Exit Message",
            "<green>Edit the exit message of your claim"),
    MENU__PERMISSIONS__BUTTONS__EDIT_EXIT_DISABLED(Material.ANVIL, 1,
            "<gray>Edit Exit Message",
            "<dark_gray>Edit the exit message of your claim"),

    MENU__PERMISSIONS__BUTTONS__DELETE(Material.RED_CONCRETE, 1,
            "<gold>Delete Claim",
            "<green>Delete your claim permanently"),
    MENU__PERMISSIONS__BUTTONS__DELETE_DISABLED(Material.GRAY_CONCRETE, 1,
            "<gray>Delete Claim",
            "<dark_gray>Delete your claim permanently"),

    MENU__PERMISSIONS__BUTTONS_SUBCLAIMS(Material.WRITABLE_BOOK, 1,
            "<gold>Sub-Claims",
            "<green>View the a list of the sub-claims for this claim"),
    MENU__PERMISSIONS__BUTTONS_NO_SUBCLAIMS(Material.WRITABLE_BOOK, 1,
            "<gray>No Sub-Claims",
            "<dark_gray>There are no sub-claims you have permission to list."),

    MENU__PERMISSIONS__CONTAINERS__DISPLAY(null, 1, "<gold><name>"),

    // Toggles

    MENU__PERMISSIONS_TOGGLES__ENABLE__OFF(Material.GREEN_STAINED_GLASS, 1, "<dark_green>Enable"),
    MENU__PERMISSIONS_TOGGLES__ENABLE__DISABLED(Material.GRAY_STAINED_GLASS_PANE, 1, "<gray>Enable"),
    MENU__PERMISSIONS_TOGGLES__ENABLE__SELECTED(Material.GREEN_CONCRETE, 1, "<green>Enabled"),
    MENU__PERMISSIONS_TOGGLES__ENABLE__SELECTED_DISABLED(Material.GRAY_CONCRETE, 1, "<gray>Enabled"),
    MENU__PERMISSIONS_TOGGLES__ENABLE__OVERRODE(Material.YELLOW_STAINED_GLASS_PANE, 1, "<yellow>Enabled"),

    MENU__PERMISSIONS_TOGGLES__NEUTRAL__OFF(Material.GRAY_STAINED_GLASS, 1, "<dark_gray>Neutral"),
    MENU__PERMISSIONS_TOGGLES__NEUTRAL__DISABLED(Material.GRAY_STAINED_GLASS_PANE, 1, "<gray>Neutral"),
    MENU__PERMISSIONS_TOGGLES__NEUTRAL__SELECTED(Material.GRAY_CONCRETE, 1, "<gray>Neutral"),
    MENU__PERMISSIONS_TOGGLES__NEUTRAL__SELECTED_DISABLED(Material.GRAY_CONCRETE, 1, "<gray>Neutral"),
    MENU__PERMISSIONS_TOGGLES__NEUTRAL__OVERRODE(Material.YELLOW_STAINED_GLASS_PANE, 1, "<yellow>Neutral"),

    MENU__PERMISSIONS_TOGGLES__DISABLE__OFF(Material.RED_STAINED_GLASS, 1, "<dark_red>Disable"),
    MENU__PERMISSIONS_TOGGLES__DISABLE__DISABLED(Material.GRAY_STAINED_GLASS_PANE, 1, "<gray>Disable"),
    MENU__PERMISSIONS_TOGGLES__DISABLE__SELECTED(Material.RED_CONCRETE, 1, "<red>Disabled"),
    MENU__PERMISSIONS_TOGGLES__DISABLE__SELECTED_DISABLED(Material.GRAY_CONCRETE, 1, "<gray>Disabled"),
    MENU__PERMISSIONS_TOGGLES__DISABLE__OVERRODE(Material.YELLOW_STAINED_GLASS_PANE, 1, "<yellow>Disabled"),

    // Routes

    MENU__PERMISSIONS__PISTONS(Material.PISTON, 1,
            "<gold>Allow Pistons",
            "<green>Allows pistons to push and pull across the claim border"),
    MENU__PERMISSIONS__FLUIDS(Material.WATER_BUCKET, 1,
            "<gold>Allow Fluids",
            "<green>Allows fluids to flow across the claim border"),
    MENU__PERMISSIONS__VIEW_SUB_CLAIMS(Material.SEA_LANTERN, 1,
            "<gold>View Sub-Claims",
            "<green>Allows players to view the sub-claims"),
    MENU__PERMISSIONS__BUILD(Material.GRASS_BLOCK, 1,
            "<gold>Build",
            "<green>Allow breaking and placing of blocks"),
    MENU__PERMISSIONS__CONTAINERS(Material.BARREL, 1,
            "<gold>Containers",
            "<green>Allow access to containers"),
    MENU__PERMISSIONS__ENTITIES(Material.ZOMBIE_HEAD, 1,
            "<gold>Entities",
            "<green>Allow interacting with entities"),
    MENU__PERMISSIONS__INTERACTIONS(Material.OAK_FENCE_GATE, 1,
            "<gold>Interactions",
            "<green>Allow interacting with blocks such as",
            "<green>doors, levers, and more"),
    MENU__PERMISSIONS__EXPLOSIONS(Material.TNT, 1,
            "<gold>Explosions",
            "<green>Allow explosions from tnt, creepers,",
            "<green>and more"),
    MENU__PERMISSIONS__TELEPORTATION(Material.ENDER_PEARL, 1,
            "<gold>Teleportation",
            "<green>Allow players to teleport inside",
            "<green>of the claim."),
    MENU__PERMISSIONS__MODIFY_PERMISSIONS(Material.CRAFTING_TABLE, 1,
            "<gold>Modify Permissions",
            "<green>Allow user to modify claim permissions,",
            "<green>players can only grant permissions they have",
            "<green>to others"),
    MENU__PERMISSIONS__MODIFY_CLAIM(Material.OAK_FENCE, 1,
            "<gold>Modify Claim",
            "<green>Allow user to modify the claim,",
            "<green>this includes resizing, deleting, and modifying",
            "<green>all claim traits"),
    MENU__PERMISSIONS__ADMIN(Material.BEACON, 1,
            "<gold>Admin",
            "<green>Grants the user the ability to modify and grant",
            "<green>permissions they are granted and modify every",
            "<green>aspect of the claim, this includes deleting",
            "<green>the claim."),
    MENU__PERMISSIONS__MISC(Material.SEA_LANTERN, 1,
            "<gold>Misc",
            "<green>Allows mob griefing and allows pistons and fluids to flow",
            "<green>across the claim border"),
    MENU__PERMISSIONS__SUBCLAIM_ADMIN(Material.BEACON, 1,
            "<gold>Sub-Claim Admin",
            "<green>Grants the user the ability to modify and grant",
            "<green>permissions they are granted and modify every",
            "<green>aspect of the claim besides resizing, this includes",
            "<green>deleting the claim."),
    MENU__PERMISSIONS__ENTITY_GRIEF(Material.CREEPER_HEAD, 1,
            "<gold>Mob Griefing",
            "<green>Allows creepers, enderman and other mobs to interact",
            "<green>with blocks inside of the claim"),

    // Menu buttons

    MENU__PERMISSION_OPTION__SIMPLE(Material.CRAFTING_TABLE, 1,
            "<gold>Simple Configuration",
            "<green>A simple look and feel to modify claim",
            "<green>permissions"),
    MENU__PERMISSION_OPTION__SIMPLE_GLOWING(){
        @Override
        void postLoad(){
            this.setItem(new ItemStackTemplate(Utils.addItemShine(MENU__PERMISSION_OPTION__SIMPLE.getItem(null))));
        }
    },

    MENU__PERMISSION_OPTION__ADVANCED(Material.SMITHING_TABLE, 1,
            "<yellow>Advanced Configuration",
            "<green>An advanced menu to deep dive into",
            "<green>permission configuration"),
    MENU__PERMISSION_OPTION__ADVANCED_GLOWING(){
        @Override
        void postLoad(){
            this.setItem(new ItemStackTemplate(Utils.addItemShine(MENU__PERMISSION_OPTION__ADVANCED.getItem(null))));
        }
    },

    MENU__PERMISSION_OPTION__GENERAL(Material.SMITHING_TABLE, 1,
            "<gold>General"),
    MENU__PERMISSION_OPTION__GENERAL_GLOWING(){
        @Override
        void postLoad(){
            this.setItem(new ItemStackTemplate(Utils.addItemShine(MENU__PERMISSION_OPTION__GENERAL.getItem(null))));
        }
    },

    MENU__PERMISSION_OPTION__CONTAINERS(Material.BARREL, 1,
            "<gold>Containers"),
    MENU__PERMISSION_OPTION__CONTAINERS_GLOWING(){
        @Override
        void postLoad(){
            this.setItem(new ItemStackTemplate(Utils.addItemShine(MENU__PERMISSION_OPTION__CONTAINERS.getItem(null))));
        }
    },

    MENU__PERMISSION_OPTION__ADMIN(Material.BEACON, 1,
            "<gold>Admin"),
    MENU__PERMISSION_OPTION__ADMIN_GLOWING(){
        @Override
        void postLoad(){
            this.setItem(new ItemStackTemplate(Utils.addItemShine(MENU__PERMISSION_OPTION__ADMIN.getItem(null))));
        }
    },

    MENU__PERMISSION_OPTION__UNUSED(Material.GRAY_STAINED_GLASS, 1,
            "<grey>Unused",
            "<dark_grey>No permissions fall under this category."),

    MENU__PERMISSION_OPTION__MISC(Material.CRAFTING_TABLE, 1,
            "<gold>Misc"),
    MENU__PERMISSION_OPTION__MISC_GLOWING(){
        @Override
        void postLoad(){
            this.setItem(new ItemStackTemplate(Utils.addItemShine(MENU__PERMISSION_OPTION__MISC.getItem(null))));
        }
    }

    ;

    private static class Utils {
        static ItemStack addItemShine(ItemStack itemStack){
            ItemStack item = itemStack.clone();

            item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
            ItemMeta meta = item.getItemMeta(); // Stupid spigot api
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            item.setItemMeta(meta);
            return item;
        }
    }

    public static BaseComponent[] parseRawUserInput(String s){
        return BungeeComponentSerializer.get().serialize(LocalizationLoader.userParser.deserialize(s));
    }

    public static void rebuildCachedMessages(){
        Logger logger = CrashClaim.getPlugin().getLogger();

        File languagesFolder = new File(CrashClaim.getPlugin().getDataFolder(), "languages");
        languagesFolder.mkdirs();

        // Load Additional Bundled Language Files
        String langSpanishFile = "languages/es_ES.yml";
        if (!new File(CrashClaim.getPlugin().getDataFolder(), langSpanishFile).exists()) {
            CrashClaim.getPlugin().saveResource(langSpanishFile, false);
        }

        // Fetch configured file, default is en_US.yml
        File languageFile = new File(languagesFolder, GlobalConfig.locale + ".yml");

        if (!languageFile.exists() && !GlobalConfig.locale.equals("en_US")){
            logger.warning("Language file does not exist, /languages/" + GlobalConfig.locale + ".yml, reverting to en_US");
            languageFile = new File(languagesFolder, "en_US.yml");
        }

        try {
            ConfigManager.initConfig(languageFile, Config.class);
        } catch (Exception ex){
            logger.severe("Language config failed to load properly. Continuing on.");
            ex.printStackTrace();
        }
    }

    void postLoad(){

    }

    private static class Config extends BaseConfig {
        private static void load(){
            for (Localization localization : Localization.values()){
                switch (localization.type){
                    case MESSAGE:
                        localization.setDefault(getString(parseToKey(localization.name()), localization.def));
                        localization.hasPlaceholders = LocalizationLoader.placeholderManager.hasPlaceholders(localization.def);

                        if (!localization.hasPlaceholders) {
                            localization.message = localization.getMessage(null, new String[0]);
                        }
                        break;
                    case MESSAGE_LIST:
                        localization.setDefaultList(getStringList(parseToKey(localization.name()), Arrays.asList(localization.defList)).toArray(new String[0]));
                        localization.hasPlaceholders = LocalizationLoader.placeholderManager.hasPlaceholders(localization.defList);

                        if (!localization.hasPlaceholders) {
                            localization.messageList = localization.getMessageList(null, new String[0]);
                        }
                        break;
                    case ITEM:
                        localization.item = createItemStack(parseToKey(localization.name()), localization.getItemTemplate());
                        break;
                    case CODE_ONLY:
                        break;
                }

                localization.postLoad();
            }
        }

        private static void removeKey(String key){
            config.set(key, null);
        }

        private static String parseToKey(String key){
            return key.replaceAll("__", ".").replaceAll("_", "-").toLowerCase();
        }

        private static ItemStackTemplate createItemStack(String key, ItemStackTemplate template){
            String title = getString(key + ".title", template.getTitle());
            List<String> lore = getStringList(key + ".lore", template.getLore());
            int model = config.getInt(key + ".model");

            return new ItemStackTemplate(
                    template.getMaterial() != null ? getMaterial(key + ".type", template.getMaterial()) : Material.PAPER, // Usually gets replaced
                    getInt(key + ".count", template.getStackSize()),
                    title,
                    lore,
                    model == 0 ? null : model,
                    itemHasPlaceholders(title, lore)
            );
        }
    }

    private static boolean itemHasPlaceholders(String title, List<String> lore){
        return LocalizationLoader.placeholderManager.hasPlaceholders(title)
                || LocalizationLoader.placeholderManager.hasPlaceholders(lore.toArray(new String[0]));
    }

    private static TagResolver generateTagResolver(String... replace){
        TagResolver.Builder builder = TagResolver.builder();
        for (int x = 0; x < replace.length - 1; x+=2){
            builder.resolver(Placeholder.parsed(replace[x], replace[x + 1]));
        }
        return builder.build();
    }

    private enum localizationType {
        MESSAGE,
        MESSAGE_LIST,
        ITEM,
        CODE_ONLY
    }

    private String def;
    private String[] defList;

    private final localizationType type;

    private boolean hasPlaceholders = false;

    Localization(){
        this.def = "";
        this.defList = null;
        this.type = localizationType.CODE_ONLY;
    }

    Localization(String def){
        this.def = def;
        this.defList = null;
        this.type = localizationType.MESSAGE;
    }

    private BaseComponent[] message;
    private List<BaseComponent[]> messageList;

    Localization(String... defList){
        this.def = null;
        this.defList = defList;
        this.type = localizationType.MESSAGE_LIST;
    }

    private ItemStackTemplate item;

    Localization(Material material, Integer stackSize, String title, String... loreDef){
        this.def = null;
        this.defList = null;
        this.type = localizationType.ITEM;

        this.item = new ItemStackTemplate(material, stackSize, title, Arrays.asList(loreDef), null, false);
    }

    public BaseComponent[] getMessage(OfflinePlayer player) {
        if (hasPlaceholders){
            return getMessage(player, new String[0]);
        }
        return message;
    }

    public BaseComponent[] getMessage(OfflinePlayer player, String... replace){
        if (hasPlaceholders){
            return BungeeComponentSerializer.get().serialize(LocalizationLoader.parser.deserialize(LocalizationLoader.placeholderManager.usePlaceholders(player, def), generateTagResolver(replace)));
        }
        return BungeeComponentSerializer.get().serialize(LocalizationLoader.parser.deserialize(def, generateTagResolver(replace)));
    }

    public List<BaseComponent[]> getMessageList(OfflinePlayer player) {
        if (hasPlaceholders){
            return getMessageList(player, new String[0]);
        }
        return messageList;
    }

    public List<BaseComponent[]> getMessageList(OfflinePlayer player, String... replace){
        ArrayList<BaseComponent[]> arr = new ArrayList<>(defList.length);

        if (hasPlaceholders){
            for (String line : defList) {
                Collections.addAll(arr, BungeeComponentSerializer.get().serialize(LocalizationLoader.parser.deserialize(
                        LocalizationLoader.placeholderManager.usePlaceholders(player, line), generateTagResolver(replace))));
            }
        } else {
            for (String line : defList) {
                Collections.addAll(arr, BungeeComponentSerializer.get().serialize(LocalizationLoader.parser.deserialize(line, generateTagResolver(replace))));
            }
        }

        return arr;
    }

    public ItemStack getItem(Player player){
        return item.build(player);
    }

    public ItemStack getItem(Player player, String... replace){
        return item.build(player, replace);
    }

    public String getRawMessage() {
        return def;
    }

    public String[] getRawList() {
        return defList;
    }

    public void setItem(ItemStackTemplate item) {
        this.item = item;
    }

    public static class ItemStackTemplate {
        private final Material material;
        private final int stackSize;
        private final String title;
        private final List<String> lore;
        private final boolean hasPlaceholders;
        private final Integer model;

        private final ItemStack staticItemStack;

        public ItemStackTemplate(Material material, int stackSize, String title, List<String> lore, Integer model, boolean hasPlaceholders) {
            this.material = material;
            this.stackSize = stackSize;
            this.title = title;
            this.lore = lore;
            this.model = model;
            this.hasPlaceholders = hasPlaceholders;

            this.staticItemStack = build(null, new String[0]);
        }

        public ItemStackTemplate(ItemStack itemStack) {
            this.material = null;
            this.stackSize = 0;
            this.title = null;
            this.lore = null;
            this.model = null;
            this.hasPlaceholders = false; // Cant have placeholders for these items.

            this.staticItemStack = itemStack;
        }

        public ItemStack build(Player player){
            if (hasPlaceholders){
                return build(player, new String[0]);
            }

            return staticItemStack;
        }

        public ItemStack build(Player player, String... replace){
            if (material == null){
                return staticItemStack;
            }

            ItemStack item = new ItemStack(material, stackSize);
            ItemMeta iMeta = item.getItemMeta();

            String newTitle = hasPlaceholders ? LocalizationLoader.placeholderManager.usePlaceholders(player, title) : title;
            iMeta.setDisplayName(BukkitComponentSerializer.legacy().serialize(Component.empty().decoration(TextDecoration.ITALIC, false).append(LocalizationLoader.parser.deserialize(newTitle, generateTagResolver(replace)))));

            if (PaperLib.isPaper()){
                List<Component> components = new ArrayList<>(lore.size());
                for (String line : lore) {
                    components.add(
                            Component.empty().decoration(TextDecoration.ITALIC, false).append(LocalizationLoader.parser.deserialize(
                                    hasPlaceholders ? LocalizationLoader.placeholderManager.usePlaceholders(player, line) : line, generateTagResolver(replace)))
                    );
                }

                iMeta.lore(components);
            } else {
                List<String> components = new ArrayList<>(lore.size());

                LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().build();
                for (String line : lore) {
                    components.add(
                            serializer.serialize(
                                    Component.empty().decoration(TextDecoration.ITALIC, false).append(LocalizationLoader.parser.deserialize(
                                            hasPlaceholders ? LocalizationLoader.placeholderManager.usePlaceholders(player, line) : line, generateTagResolver(replace))))
                    );
                }

                iMeta.setLore(components);
            }

            if (model != null){
                iMeta.setCustomModelData(model); // Set custom model data.
            }

            item.setItemMeta(iMeta);

            return item;
        }

        public Material getMaterial() {
            return material;
        }

        public int getStackSize() {
            return stackSize;
        }

        public String getTitle() {
            return title;
        }

        public List<String> getLore() {
            return lore;
        }
    }

    private void setDefault(String def) {
        this.def = def;
    }

    private void setDefaultList(String[] defList) {
        this.defList = defList;
    }

    private void setMessage(BaseComponent[] message) {
        this.message = message;
    }

    private void setMessageList(List<BaseComponent[]> messageList) {
        this.messageList = messageList;
    }

    public ItemStackTemplate getItemTemplate() {
        return item;
    }

    private localizationType getType() {
        return type;
    }
}

