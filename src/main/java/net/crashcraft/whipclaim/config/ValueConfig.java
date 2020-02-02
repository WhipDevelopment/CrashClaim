package net.crashcraft.whipclaim.config;

import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
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

    public static EnumWrappers.TitleAction VISUALIZE_ALERT_TYPE = EnumWrappers.TitleAction.ACTIONBAR;
    private static String VISUALIZE_ALERT_TYPE_KEY = "visualization.alert.type";

    public static int VISUALIZE_ALERT_FADE_IN = 10;
    private static String VISUALIZE_ALERT_FADE_IN_KEY = "visualization.alert.fade-in";

    public static int VISUALIZE_ALERT_DURATION = 1;
    private static String VISUALIZE_ALERT_DURATION_KEY = "visualization.alert.duration";

    public static int VISUALIZE_ALERT_FADE_OUT = 10;
    private static String VISUALIZE_ALERT_FADE_OUT_KEY = "visualization.alert.fade-out";

    public static HashMap<PlayerTeleportEvent.TeleportCause, Integer> EVENTS_TELEPORT;
    private static String EVENTS_TELEPORT_KEY = "events.teleport";

    public static double MONEY_PER_BLOCK = 0.01;
    private static String MONEY_PER_BLOCK_KEY = "money-per-block";

    public static void writeDefault(FileConfiguration configuration, Plugin plugin){
        YamlConfiguration config = new YamlConfiguration();

        config.set(VISUALIZE_VISUAL_TYPE_KEY, VISUALIZE_VISUAL_TYPE);
        config.set(VISUALIZE_VISUAL_BLOCK_KEY, VISUALIZE_VISUAL_BLOCK.name());
        config.set(MENU_VISUAL_CLAIM_ITEMS_KET, MENU_VISUAL_CLAIM_ITEMS);
        config.set(VISUALIZE_VISUAL_USE_HIGHEST_BLOCK_KEY, VISUALIZE_VISUAL_USE_HIGHEST_BLOCK);
        config.set(VISUALIZE_ALERT_TYPE_KEY, VISUALIZE_ALERT_TYPE.name());
        config.set(VISUALIZE_ALERT_FADE_IN_KEY, VISUALIZE_ALERT_FADE_IN);
        config.set(VISUALIZE_ALERT_DURATION_KEY, VISUALIZE_ALERT_DURATION);
        config.set(VISUALIZE_ALERT_FADE_OUT_KEY, VISUALIZE_ALERT_FADE_OUT);
        config.set(MONEY_PER_BLOCK_KEY, MONEY_PER_BLOCK);

        for (PlayerTeleportEvent.TeleportCause cause : PlayerTeleportEvent.TeleportCause.values()){
            config.set(EVENTS_TELEPORT_KEY + "." + cause.name(), "block");
        }

        configuration.options().copyDefaults(true);
        configuration.setDefaults(config);
    }

    public static void loadConfig(FileConfiguration configuration, Plugin plugin) {
        Material material = null;
        logger = plugin.getLogger();

        VISUALIZE_VISUAL_TYPE = configuration.getString(VISUALIZE_VISUAL_TYPE_KEY);

        material = Material.getMaterial(configuration.getString(VISUALIZE_VISUAL_BLOCK_KEY));
        if (material == null) {
            logger.warning("[Config] " + VISUALIZE_VISUAL_BLOCK_KEY + " is not a valid material. Defaulting values.");
        } else {
            VISUALIZE_VISUAL_BLOCK = material;
        }

        ArrayList<Material> materials = new ArrayList<>();
        for (String worldname : configuration.getStringList(MENU_VISUAL_CLAIM_ITEMS_KET)) {
            material = Material.valueOf(configuration.getString(MENU_VISUAL_CLAIM_ITEMS_KET + "." + worldname));
            World world = Bukkit.getWorld(worldname);
            if (material == null) {
                logger.warning("[Config] " + MENU_VISUAL_CLAIM_ITEMS_KET + "." + worldname + " is not a valid material. Defaulting values.");
            } else if (world == null) {
                logger.warning("[Config] " + MENU_VISUAL_CLAIM_ITEMS_KET + "." + worldname + " is not a valid world name. Defaulting values.");
            } else {
                MENU_VISUAL_CLAIM_ITEMS.put(world.getUID(), material);
            }
        }

        VISUALIZE_VISUAL_USE_HIGHEST_BLOCK = configuration.getBoolean(VISUALIZE_VISUAL_USE_HIGHEST_BLOCK_KEY, VISUALIZE_VISUAL_USE_HIGHEST_BLOCK);

        try {
            VISUALIZE_ALERT_TYPE = EnumWrappers.TitleAction.valueOf(configuration.getString(VISUALIZE_ALERT_TYPE_KEY));
        } catch (EnumConstantNotPresentException e) {
            logger.warning("Invalid " + VISUALIZE_ALERT_TYPE_KEY + ", defaulting to " + VISUALIZE_ALERT_TYPE.name());
        }

        VISUALIZE_ALERT_FADE_IN = configuration.getInt(VISUALIZE_ALERT_FADE_IN_KEY, VISUALIZE_ALERT_FADE_IN);
        VISUALIZE_ALERT_DURATION = configuration.getInt(VISUALIZE_ALERT_DURATION_KEY, VISUALIZE_ALERT_DURATION);
        VISUALIZE_ALERT_FADE_OUT = configuration.getInt(VISUALIZE_ALERT_FADE_OUT_KEY, VISUALIZE_ALERT_FADE_OUT);

        EVENTS_TELEPORT = new HashMap<>();

        ConfigurationSection section = configuration.getConfigurationSection(EVENTS_TELEPORT_KEY);
        if (section == null){
            section = configuration.createSection(EVENTS_TELEPORT_KEY);
        }

        // 0 | NONE  - diable, 1 | BLOCK - enable check with blocking, 2 | RELOCATE - enable check with relocating

        for (PlayerTeleportEvent.TeleportCause cause : PlayerTeleportEvent.TeleportCause.values()){
            String value = section.getString(cause.name());

            switch (value.toLowerCase()){
                case "none":
                    EVENTS_TELEPORT.put(cause, 0);
                    continue;
                case "block":
                    EVENTS_TELEPORT.put(cause, 1);
                    continue;
                case "relocate":
                    EVENTS_TELEPORT.put(cause, 2);
                    continue;
                default:
                    //Bad value default to good one
                    logger.warning("Invalid value for " + section.getCurrentPath() + "." + cause.name() + "\nUsing `block`");
                    EVENTS_TELEPORT.put(cause, 1);
                    continue;
            }
        }

        MONEY_PER_BLOCK = configuration.getDouble(MONEY_PER_BLOCK_KEY);
    }
}
