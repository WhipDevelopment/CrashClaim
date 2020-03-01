package net.crashcraft.whipclaim.menus.helpers;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.whipclaim.permissions.PermissionRoute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class StaticItemLookup {
    private static HashMap<PermissionRoute, ItemStack> items = new HashMap<PermissionRoute, ItemStack>(){
        {
            put(PermissionRoute.PISTONS , GUI.createGuiItem(ChatColor.GOLD + "Allow Pistons",
                    new ArrayList<>(Arrays.asList(ChatColor.GREEN + "Allows pistons to cross into and out of claim")), Material.CRAFTING_TABLE));
            put(PermissionRoute.FLUIDS, GUI.createGuiItem(ChatColor.GOLD + "Allow Fluids",
                    new ArrayList<>(Arrays.asList(ChatColor.GREEN + "Allows fluids to cross into and out of claim")), Material.OAK_FENCE_GATE));
            put(PermissionRoute.VIEW_SUB_CLAIMS, GUI.createGuiItem(ChatColor.GOLD + "View Sub Claims",
                    new ArrayList<>(Arrays.asList(ChatColor.GREEN + "Allows players to view the sub claims")), Material.SEA_LANTERN));
            put(PermissionRoute.BUILD, GUI.createGuiItem(ChatColor.GOLD + "Build", Material.GRASS_BLOCK));
            put(PermissionRoute.ENTITIES, GUI.createGuiItem(ChatColor.GOLD + "Entities", Material.CREEPER_HEAD));
            put(PermissionRoute.INTERACTIONS, GUI.createGuiItem(ChatColor.GOLD + "Interactions", Material.OAK_FENCE_GATE));
            put(PermissionRoute.EXPLOSIONS, GUI.createGuiItem(ChatColor.GOLD + "Explosions", Material.TNT));
            put(PermissionRoute.TELEPORTATION, GUI.createGuiItem(ChatColor.GOLD + "Teleportation", Material.ENDER_PEARL));
            put(PermissionRoute.MODIFY_PERMISSIONS, GUI.createGuiItem(ChatColor.GOLD + "Modify Permissions", Material.CRAFTING_TABLE));
            put(PermissionRoute.MODIFY_CLAIM, GUI.createGuiItem(ChatColor.GOLD + "Modify Claim", Material.OAK_FENCE_GATE));
        }
    };

    public static ItemStack getItem(PermissionRoute route){
        return items.get(route);
    }
}
