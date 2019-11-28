package net.crashcraft.whipclaim.permissions;

import net.crashcraft.whipclaim.WhipClaim;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

public class PermissionSetup {
    private ArrayList<Material> trackedContainers;
    private ArrayList<Material> untrackedBlocks;
    private ArrayList<Material> extraInteractables;

    public PermissionSetup(WhipClaim claim){
        Logger logger = claim.getLogger();

        trackedContainers = new ArrayList<>();
        untrackedBlocks = new ArrayList<>();
        extraInteractables = new ArrayList<>();

        for (Material material : Material.values()){
            ItemStack stack = new ItemStack(material);
            BlockStateMeta meta = (BlockStateMeta) stack.getItemMeta();

            if (meta == null)
                return;

            if (meta.getBlockState() instanceof Container){
                trackedContainers.add(material);
            }
        }

        FileConfiguration lookup = YamlConfiguration.loadConfiguration(new File(Paths.get(claim.getDataFolder().getAbsolutePath(), "lookup.yml").toUri()));

        for (String name : lookup.getStringList("untracked-blocks")){
            Material material = Material.getMaterial(name);

            if (material != null){
                untrackedBlocks.add(material);
                trackedContainers.remove(material);
            } else {
                logger.warning("Material was not found whole parsing lookup.yml -> untracked-blocks: " + name +
                        "\n Make sure to be using the Bukkit Material names.");
            }
        }

        for (String name : lookup.getStringList("additional-tracked-interactables")){
            Material material = Material.getMaterial(name);

            if (material != null){
                extraInteractables.add(material);
            } else {
                logger.warning("Material was not found whole parsing lookup.yml -> additional-tracked-interactables: " + name +
                        "\n Make sure to be using the Bukkit Material names.");
            }
        }
    }
}