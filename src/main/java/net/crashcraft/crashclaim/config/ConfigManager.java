package net.crashcraft.crashclaim.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigManager {
    public static YamlConfiguration initConfig(File configFile, Class<? extends BaseConfig> clazz) throws Exception{
        return initConfig(configFile, clazz, null);
    }

    public static YamlConfiguration initConfig(File configFile, Class<? extends BaseConfig> clazz, Object instance) throws Exception{
        if (!configFile.exists()){
            configFile.createNewFile();
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (InvalidConfigurationException ex) {
            throw new Exception("Invalid syntax in config file, " + configFile);
        }
        config.options().copyDefaults(true);

        BaseConfig.setConfig(config); //Set for each load instance should be fine unless threaded - not thread safe

        return readConfig(clazz, instance, config, configFile);
    }

    private static YamlConfiguration readConfig(Class<?> clazz, Object instance, YamlConfiguration config, File file) throws Exception{ //Stole from paper their config system is so sexy
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())) {
                if (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE) {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new Exception("Failed to instantiate config file: " + file + ", method: " + method);
                    }
                }
            }
        }

        try {
            config.save(file);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save " + file, ex); //Not completely fatal will not shut server down
        }

        return config;
    }
}
