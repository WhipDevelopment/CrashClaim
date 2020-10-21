package net.crashcraft.whipclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.crashcraft.whipclaim.WhipClaim;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@CommandAlias("ctest")
@CommandPermission("whipclaim.test.containertoconsole")
public class ContainerizerCommand extends BaseCommand {
    @Default
    public void on(Player player){
        Block block = player.getTargetBlock(5);

        if (block == null){
            return;
        }

        if (block.getType().equals(Material.CHEST)){
            Chest chest = (Chest) block.getState();

            Inventory inventory = chest.getInventory();

            StringBuilder builder = new StringBuilder("\n");

            for (int x = 0; x < inventory.getSize(); x++){
                ItemStack itemStack = inventory.getItem(x);

                if (itemStack == null || itemStack.getType().equals(Material.AIR)){
                    continue;
                }

                builder.append("inv.setItem(");
                builder.append(x);
                builder.append(", createGuiItem(ChatColor.GRAY + \"General Permissions\", Material.");
                builder.append(itemStack.getType().name());
                builder.append("));");

                builder.append("\n");
            }

            WhipClaim.getPlugin().getLogger().info(builder.toString());
        }
    }
}
