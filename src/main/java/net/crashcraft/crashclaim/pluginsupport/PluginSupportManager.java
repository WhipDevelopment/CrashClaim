package net.crashcraft.crashclaim.pluginsupport;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.pluginsupport.plugins.WorldGuardSupport;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class PluginSupportManager implements Listener {
    private static final List<Class<? extends PluginSupport>> pluginSupportWrappers = Collections.singletonList(
            WorldGuardSupport.class
    );

    private final Logger logger;
    private final List<PluginSupport> enabledSupport;
    private final PluginSupportDistributor supportDistributor;

    public PluginSupportManager(CrashClaim crashClaim) {
        this.logger = crashClaim.getLogger();

        enabledSupport = new ArrayList<>();

        for (Class<? extends PluginSupport> pluginSupport : pluginSupportWrappers){
            try {
                String pluginName = getPluginName(pluginSupport);

                Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
                if (plugin == null){
                    continue;
                }

                PluginSupport support = pluginSupport.getDeclaredConstructor().newInstance();
                String version = plugin.getDescription().getVersion();

                if (support.isUnSupportedVersion(version)){
                    logger.warning("Plugin [" + pluginName + "] was found but version " + version + ", is not supported.");
                    continue;
                }

                logger.info("Enabling plugin support for " + pluginName + ", enabling additional checks and features");

                enabledSupport.add(support);
                support.init(plugin);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex){
                // Unsupported then
            }
        }

        supportDistributor = new PluginSupportDistributor(this);

        Bukkit.getPluginManager().registerEvents(this, crashClaim);
    }

    private String getPluginName(Class<?> clazz){
        return clazz.getSimpleName().substring(0, clazz.getSimpleName().length() - 8); // Support - 1 for length
    }

    @EventHandler
    public void pluginEnable(PluginEnableEvent event){
        for (Class<? extends PluginSupport> pluginSupport : pluginSupportWrappers){
            try {
                String pluginName = getPluginName(pluginSupport);

                if (!event.getPlugin().getDescription().getName().equals(pluginName)){
                    continue;
                }

                PluginSupport support = pluginSupport.getDeclaredConstructor().newInstance();
                String version = event.getPlugin().getDescription().getVersion();

                for (PluginSupport otherSupport : enabledSupport){
                    if (getPluginName(otherSupport.getClass()).equals(pluginName)){
                        logger.warning("Plugin initialized twice without registering a disable, this might cause problems, reinfecting new instance.");
                        support.init(event.getPlugin());
                        return;
                    }
                }

                if (support.isUnSupportedVersion(version)){
                    logger.warning("Plugin [" + pluginName + "] was found but version " + version + ", is not supported.");
                    return;
                }

                logger.info("Enabling plugin support for " + pluginName + ", enabling additional checks and features");

                enabledSupport.add(support);
                support.init(event.getPlugin());
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex){
                // Unsupported then
            }
        }
    }

    @EventHandler
    public void pluginDisable(PluginDisableEvent event){
        Iterator<PluginSupport> iter = enabledSupport.iterator();
        while (iter.hasNext()){
            PluginSupport support = iter.next();

            if (event.getPlugin().getDescription().getName().equals(getPluginName(support.getClass()))){
                logger.info("Disabling plugin support for [" + event.getPlugin().getDescription().getName() + "] because plugin is disabling.");

                enabledSupport.remove(support);
                support.disable();
                iter.remove();
                break;
            }
        }
    }

    public List<PluginSupport> getEnabledSupport() {
        return enabledSupport;
    }

    public PluginSupportDistributor getSupportDistributor() {
        return supportDistributor;
    }
}
