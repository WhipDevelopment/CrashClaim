package net.crashcraft.whipclaim.config;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public abstract class BaseConfig {
    public static YamlConfiguration config;

    public static void setConfig(YamlConfiguration config) {
        BaseConfig.config = config;
    }

    protected static boolean fatalError = false;

    protected static void logError(String s) {
        Bukkit.getLogger().severe(config.getName() + ": " + s);
    }

    protected static void log(String s) {
        Bukkit.getLogger().info(config.getName() + ": " + s);
    }

    protected static void fatal(String s) {
        fatalError = true;
        throw new RuntimeException("Fatal " + config.getName() + ".yml config error: " + s);
    }

    protected static void set(String path, Object val) {
        config.set(path, val);
    }

    protected static boolean getBoolean(String path, boolean def) {
        config.addDefault(path, def);
        return config.getBoolean(path, config.getBoolean(path));
    }

    protected static double getDouble(String path, double def) {
        config.addDefault(path, def);
        return config.getDouble(path, config.getDouble(path));
    }

    protected static int getInt(String path, int def) {
        config.addDefault(path, def);
        return config.getInt(path, config.getInt(path));
    }

    protected static <T> List getList(String path, T def) {
        config.addDefault(path, def);
        return config.getList(path, config.getList(path));
    }

    protected static List<String> getStringList(String path, List<String> def) {
        config.addDefault(path, def);
        return config.getStringList(path);
    }

    protected static String getString(String path, String def) {
        config.addDefault(path, def);
        return config.getString(path, config.getString(path));
    }

    protected static int[] getRangedValue(String path, int expected, String def) {
        config.addDefault(path, def);
        String[] value = config.getString(path).split(":", expected);
        int[] arr = new int[expected];
        try {
            for (int x = 0; x < arr.length; x++) {
                arr[x] = Integer.valueOf(value[x]);
            }
        } catch (NumberFormatException e) {
            logError("Invalid number format for, " + path);
        }
        return arr;
    }

    protected static Material getMaterial(String path, Material def) {
        config.addDefault(path, def.name());
        return Material.getMaterial(config.getString(path, config.getString(path)));
    }
}
