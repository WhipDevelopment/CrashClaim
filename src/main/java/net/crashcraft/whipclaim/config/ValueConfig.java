package net.crashcraft.whipclaim.config;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

@SuppressWarnings("all")
public class ValueConfig {
    private static Logger logger;

    public static String VISUALIZE_VISUAL_TYPE = "glow";
    private static String VISUALIZE_VISUAL_TYPE_KEY = "visualization.visual-type";

    public static Material VISUALIZE_VISUAL_BLOCK = Material.GREEN_CONCRETE;
    private static String VISUALIZE_VISUAL_BLOCK_KEY = "visualization.visual-block-type";

    public static HashMap<UUID, Material> MENU_VISUAL_CLAIM_ITEMS = new HashMap<>();
    private static String MENU_VISUAL_CLAIM_ITEMS_KET = "visualize.visualize-claim-items";

    public static boolean VISUALIZE_VISUAL_USE_HIGHEST_BLOCK = false;
    private static String VISUALIZE_VISUAL_USE_HIGHEST_BLOCK_KEY = "visualization.visual-use-highest-block";

    public static void writeDefault(FileConfiguration configuration, Plugin plugin){
        YamlConfiguration config = new YamlConfiguration();

        config.set(VISUALIZE_VISUAL_TYPE_KEY, VISUALIZE_VISUAL_TYPE);
        config.set(VISUALIZE_VISUAL_BLOCK_KEY, VISUALIZE_VISUAL_BLOCK.name());
        config.set(MENU_VISUAL_CLAIM_ITEMS_KET, MENU_VISUAL_CLAIM_ITEMS);
        config.set(VISUALIZE_VISUAL_USE_HIGHEST_BLOCK_KEY, VISUALIZE_VISUAL_USE_HIGHEST_BLOCK);

        configuration.options().copyDefaults(true);
        configuration.setDefaults(config);

        try {
            System.out.println(configuration.getCurrentPath());
            configuration.save(configuration.getCurrentPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadConfig(FileConfiguration configuration, Plugin plugin){
        Material material = null;
        logger = plugin.getLogger();

        VISUALIZE_VISUAL_TYPE = configuration.getString(VISUALIZE_VISUAL_TYPE_KEY);

        material = Material.getMaterial(configuration.getString(VISUALIZE_VISUAL_BLOCK_KEY));
        if (material == null){
            logger.warning("[Config] " + VISUALIZE_VISUAL_BLOCK_KEY + " is not a valid material. Defaulting values.");
        } else {
            VISUALIZE_VISUAL_BLOCK = material;
        }

        ArrayList<Material> materials = new ArrayList<>();
        for (String worldname : configuration.getStringList(MENU_VISUAL_CLAIM_ITEMS_KET)){
            material = Material.valueOf(configuration.getString(MENU_VISUAL_CLAIM_ITEMS_KET + "." + worldname));
            World world = Bukkit.getWorld(worldname);
            if (material == null){
                logger.warning("[Config] " + MENU_VISUAL_CLAIM_ITEMS_KET + "." + worldname + " is not a valid material. Defaulting values.");
            } else if (world == null){
                logger.warning("[Config] " + MENU_VISUAL_CLAIM_ITEMS_KET + "." + worldname + " is not a valid world name. Defaulting values.");
            } else {
                MENU_VISUAL_CLAIM_ITEMS.put(world.getUID(), material);
            }
        }

        VISUALIZE_VISUAL_USE_HIGHEST_BLOCK = configuration.getBoolean(VISUALIZE_VISUAL_USE_HIGHEST_BLOCK_KEY, VISUALIZE_VISUAL_USE_HIGHEST_BLOCK);
    }
}
