package net.crashcraft.crashclaim.config;

import net.crashcraft.crashclaim.visualize.api.VisualColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class GlobalConfig extends BaseConfig{
    public static String locale;
    public static String paymentProvider;
    public static boolean checkUpdates;

    private static void loadGeneral(){
        locale = getString("language", "en_US");
        paymentProvider = getString("payment-provider", "default");
        checkUpdates = getBoolean("check-updates", true);
    }

    public static String visual_type;
    public static boolean visual_use_highest_block;
    public static HashMap<UUID, Material> visual_menu_items;

    public static int visual_alert_fade_in;
    public static int visual_alert_duration;
    public static int visual_alert_fade_out;

    private static void loadVisual(){
        visual_type = getString("visualization.visual-type", "glow");

        visual_menu_items = new HashMap<>();

        for (World world : Bukkit.getWorlds()){
            visual_menu_items.put(world.getUID(), Material.getMaterial(getString("visualization.claim-items." + world.getName(), Material.OAK_FENCE.name())));
        }

        visual_use_highest_block = getBoolean("visualization.visual-use-highest-block", false);
        visual_alert_fade_in = getInt("visualization.alert.fade-in", 10);
        visual_alert_duration = getInt("visualization.alert.duration", 1);
        visual_alert_fade_out = getInt("visualization.alert.fade-out", 10);

        setVisualBlockColor(VisualColor.GOLD, Material.ORANGE_CONCRETE);
        setVisualBlockColor(VisualColor.RED, Material.RED_CONCRETE);
        setVisualBlockColor(VisualColor.GREEN, Material.LIME_CONCRETE);
        setVisualBlockColor(VisualColor.YELLOW, Material.GREEN_CONCRETE);
        setVisualBlockColor(VisualColor.WHITE, Material.WHITE_CONCRETE);
    }

    private static void setVisualBlockColor(VisualColor color, Material defaultMaterial){
        String key = "visualization.visual-colors." + color.name();
        Material material = Material.getMaterial(getString(key, defaultMaterial.name()));

        if (material != null) {
            color.setMaterial(material);
            return;
        }

        log("Invalid material for " + key + ", loading default value");
        color.setMaterial(defaultMaterial);
    }

    public static boolean useCommandInsteadOfEdgeEject;
    public static String claimEjectCommand;

    private static void loadEject(){
        useCommandInsteadOfEdgeEject = getBoolean("eject.useCommandInstead", false);
        claimEjectCommand = getString("eject.command", "home");
    }

    public static HashMap<PlayerTeleportEvent.TeleportCause, Integer> teleportCause;

    private static void loadTeleport(){
        // 0 | NONE  - diable, 1 | BLOCK - enable check with blocking, 2 | RELOCATE - enable check with relocating
        teleportCause = new HashMap<>();
        for (PlayerTeleportEvent.TeleportCause cause : PlayerTeleportEvent.TeleportCause.values()){
            if (cause.equals(PlayerTeleportEvent.TeleportCause.UNKNOWN)){
                continue;
            }

            String value = getString("events.teleport." + cause.name(), "block");

            switch (value.toLowerCase()){
                case "none":
                    teleportCause.put(cause, 0);
                case "block":
                    teleportCause.put(cause, 1);
                case "relocate":
                    teleportCause.put(cause, 2);
                default:
                    //Bad value default to good one
                    teleportCause.put(cause, 1);
            }
        }
    }

    public static double money_per_block;
    public static ArrayList<UUID> disabled_worlds;
    public static String forcedVersionString;
    public static boolean blockPvPInsideClaims;
    public static boolean checkEntryExitWhileFlying;

    private static void miscValues(){
        money_per_block = getDouble("money-per-block", 0.01);
        disabled_worlds = new ArrayList<>();
        for (String s : getStringList("disabled-worlds", Collections.emptyList())){
            World world = Bukkit.getWorld(s);
            if (world == null){
                logError("World name was invalid or the world was not loaded into memory");
                continue;
            }

            disabled_worlds.add(world.getUID());
        }

        forcedVersionString = config.getString("use-this-version-instead");
        blockPvPInsideClaims = getBoolean("block-pvp-inside-claims", false);
        checkEntryExitWhileFlying = config.getBoolean("check-entry-and-exit-while-flying", false);
    }

    public static boolean bypassModeBypassesMoney;

    private static void onBypass(){
        bypassModeBypassesMoney = getBoolean("bypass-mode-bypasses-payment", false);
    }

    public static boolean useStatistics;

    private static void onStats(){
        useStatistics = getBoolean("statistics", true);
    }
}
