package net.crashcraft.crashclaim.crashutils;

import io.papermc.lib.PaperLib;
import net.crashcraft.crashclaim.crashutils.caches.TextureCache;
import net.crashcraft.crashclaim.crashutils.menusystem.CrashMenuController;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

//https://github.com/CrashCraftNetwork/CrashUtils/blob/master/src/main/java/dev/whip/crashutils/CrashUtils.java
public class CrashUtils implements Listener {
    private static JavaPlugin plugin;
    private static TextureCache textureCache;

    public CrashUtils(JavaPlugin javaPlugin){
        plugin = javaPlugin;

        PaperLib.suggestPaper(javaPlugin);
    }

    public void setupMenuSubSystem(){
        Bukkit.getServer().getPluginManager().registerEvents(new CrashMenuController(plugin), plugin);     //Gui controller
    }

    public void setupTextureCache(){
        if (PaperLib.isPaper()) {
            if (textureCache == null) {
                textureCache = new TextureCache();
                Bukkit.getServer().getPluginManager().registerEvents(textureCache, plugin);
            }
        } else {
            plugin.getLogger().severe("Your server is not running Paper or a Paper derivative, texture caching has not been enabled");
        }
    }
}