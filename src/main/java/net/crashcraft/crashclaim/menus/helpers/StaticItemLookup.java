package net.crashcraft.crashclaim.menus.helpers;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class StaticItemLookup {
    private static final HashMap<PermissionRoute, ItemStack> items;

    public static ItemStack SIMPLE_MENU_ITEM;
    public static ItemStack SIMPLE_MENU_ITEM_GLOWING;

    public static ItemStack ADVANCED_MENU_ITEM;
    public static ItemStack ADVANCED_MENU_ITEM_GLOWING;

    public static ItemStack ADVANCED_SUBMENU_GENERAL;
    public static ItemStack ADVANCED_SUBMENU_GENERAL_GLOWING;
    public static ItemStack ADVANCED_SUBMENU_CONTAINERS;
    public static ItemStack ADVANCED_SUBMENU_CONTAINERS_GLOWING;
    public static ItemStack ADVANCED_SUBMENU_ADMIN;
    public static ItemStack ADVANCED_SUBMENU_ADMIN_GLOWING;
    public static ItemStack ADVANCED_SUBMENU_MISC;
    public static ItemStack ADVANCED_SUBMENU_MISC_GLOWING;

    public static ItemStack BACK_ARROW;

    public static ItemStack NEXT_BUTTON;
    public static ItemStack BACK_BUTTON;

    static {
        items = new HashMap<PermissionRoute, ItemStack>(){
            {
                put(PermissionRoute.PISTONS , GUI.createGuiItem(ChatColor.GOLD + "Allow Pistons",
                        new ArrayList<>(Arrays.asList(ChatColor.GREEN + "Allows pistons to cross into and out of claim")), Material.PISTON));
                put(PermissionRoute.FLUIDS, GUI.createGuiItem(ChatColor.GOLD + "Allow Fluids",
                        new ArrayList<>(Arrays.asList(ChatColor.GREEN + "Allows fluids to cross into and out of claim")), Material.WATER_BUCKET));
                put(PermissionRoute.VIEW_SUB_CLAIMS, GUI.createGuiItem(ChatColor.GOLD + "View Sub Claims",
                        new ArrayList<>(Arrays.asList(ChatColor.GREEN + "Allows players to view the sub claims")), Material.SEA_LANTERN));
                put(PermissionRoute.BUILD, GUI.createGuiItem(ChatColor.GOLD + "Build", Material.GRASS_BLOCK));
                put(PermissionRoute.CONTAINERS, GUI.createGuiItem(ChatColor.GOLD + "Containers", Material.BARREL));
                put(PermissionRoute.ENTITIES, GUI.createGuiItem(ChatColor.GOLD + "Entities", Material.CREEPER_HEAD));
                put(PermissionRoute.INTERACTIONS, GUI.createGuiItem(ChatColor.GOLD + "Interactions", Material.OAK_FENCE_GATE));
                put(PermissionRoute.EXPLOSIONS, GUI.createGuiItem(ChatColor.GOLD + "Explosions", Material.TNT));
                put(PermissionRoute.TELEPORTATION, GUI.createGuiItem(ChatColor.GOLD + "Teleportation", Material.ENDER_PEARL));
                put(PermissionRoute.MODIFY_PERMISSIONS, GUI.createGuiItem(ChatColor.GOLD + "Modify Permissions", Material.CRAFTING_TABLE));
                put(PermissionRoute.MODIFY_CLAIM, GUI.createGuiItem(ChatColor.GOLD + "Modify Claim", Material.OAK_FENCE_GATE));
                put(PermissionRoute.ADMIN, GUI.createGuiItem(ChatColor.GOLD + "Admin", Material.BEACON));
                put(PermissionRoute.MISC, GUI.createGuiItem(ChatColor.GOLD + "Misc", Material.SEA_LANTERN));
            }
        };

        SIMPLE_MENU_ITEM = GUI.createGuiItem(ChatColor.GREEN + "Simple Configuration", Material.CRAFTING_TABLE);
        SIMPLE_MENU_ITEM_GLOWING = SIMPLE_MENU_ITEM.clone();
        SIMPLE_MENU_ITEM_GLOWING.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        SIMPLE_MENU_ITEM_GLOWING.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        ADVANCED_MENU_ITEM = GUI.createGuiItem(ChatColor.YELLOW + "Advanced Configuration", Material.SMITHING_TABLE);
        ADVANCED_MENU_ITEM_GLOWING = ADVANCED_MENU_ITEM.clone();
        ADVANCED_MENU_ITEM_GLOWING.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        ADVANCED_MENU_ITEM_GLOWING.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        //SubMenu

        ADVANCED_SUBMENU_GENERAL = GUI.createGuiItem(ChatColor.GREEN + "General", Material.SMITHING_TABLE);
        ADVANCED_SUBMENU_GENERAL_GLOWING = ADVANCED_SUBMENU_GENERAL.clone();
        ADVANCED_SUBMENU_GENERAL_GLOWING.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        ADVANCED_SUBMENU_GENERAL_GLOWING.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        ADVANCED_SUBMENU_CONTAINERS = GUI.createGuiItem(ChatColor.GREEN + "Containers", Material.BARREL);
        ADVANCED_SUBMENU_CONTAINERS_GLOWING = ADVANCED_SUBMENU_CONTAINERS.clone();
        ADVANCED_SUBMENU_CONTAINERS_GLOWING.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        ADVANCED_SUBMENU_CONTAINERS_GLOWING.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        ADVANCED_SUBMENU_ADMIN = GUI.createGuiItem(ChatColor.GREEN + "Admin", Material.BEACON);
        ADVANCED_SUBMENU_ADMIN_GLOWING = ADVANCED_SUBMENU_ADMIN.clone();
        ADVANCED_SUBMENU_ADMIN_GLOWING.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        ADVANCED_SUBMENU_ADMIN_GLOWING.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        ADVANCED_SUBMENU_MISC = GUI.createGuiItem(ChatColor.GREEN + "Misc", Material.CRAFTING_TABLE);
        ADVANCED_SUBMENU_MISC_GLOWING = ADVANCED_SUBMENU_MISC.clone();
        ADVANCED_SUBMENU_MISC_GLOWING.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        ADVANCED_SUBMENU_MISC_GLOWING.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        //Items

        BACK_ARROW = GUI.createGuiItem(ChatColor.GOLD + "Back", Material.ARROW);

        NEXT_BUTTON = GUI.createGuiItem(ChatColor.WHITE + "Next", Material.OAK_BUTTON);
        BACK_BUTTON = GUI.createGuiItem(ChatColor.WHITE + "Previous", Material.OAK_BUTTON);
    }

    public static ItemStack getItem(PermissionRoute route){
        return items.get(route);
    }
}
