package net.crashcraft.crashclaim.pluginsupport;

import io.papermc.paper.plugin.PermissionManager;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.crashutils.ServiceUtil;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class PluginSupportManager implements Listener {

    private final CrashClaim crashClaim;
    private final Logger logger;
    private final Set<PluginSupport> enabledSupport;
    private final List<PluginSupport> allSupport;
    private final PluginSupportDistributor supportDistributor;

    public PluginSupportManager(CrashClaim crashClaim) {
        this.crashClaim = crashClaim;
        this.logger = crashClaim.getLogger();

        allSupport = new ArrayList<>();
        enabledSupport = new HashSet<>();

        supportDistributor = new PluginSupportDistributor(this, crashClaim);
    }

    public void onLoad() {
        List<PluginSupportLoader> services = ServiceUtil.getServices(PluginSupportLoader.class, crashClaim.getClass().getClassLoader());

        logger.info("Found " + services.size() + " plugin support services.");
        for (PluginSupportLoader service : services) {
            register(service);
        }
    }

    public void onEnable(){
        for (PluginSupport pluginSupport : enabledSupport){
            pluginSupport.enable(crashClaim);
        }
    }

    public void onDisable(){
        for (PluginSupport pluginSupport : enabledSupport){
            pluginSupport.disable();
        }
        enabledSupport.clear();
    }

    public void register(PluginSupportLoader pluginSupportLoader){
        if (!pluginSupportLoader.canLoad()) {
            logger.warning("Plugin support for " + pluginSupportLoader.getPluginName() + " is not enabled.");
            return;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginSupportLoader.getPluginName());
        String version = plugin.getDescription().getVersion();

        if (pluginSupportLoader.isUnsupportedVersion(version)) {
            logger.warning("Plugin [" + pluginSupportLoader.getPluginName() + "] was found but version " + version + ", is not supported.");
            return;
        }

        pluginSupportLoader.load(plugin);
    }

    public void register(PluginSupport pluginSupport){
        if (allSupport.contains(pluginSupport)) {
            logger.warning("Plugin support for " + pluginSupport.getPluginName() + " is already registered.");
            return;
        }

        allSupport.add(pluginSupport);

        if (!pluginSupport.canLoad()) {
            logger.warning("Not enabling plugin support for " + pluginSupport.getPluginName() + ", plugin is not enabled.");
            return;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginSupport.getPluginName());
        String version = plugin.getDescription().getVersion();

        if (pluginSupport.isUnsupportedVersion(version)) {
            logger.warning("Plugin [" + pluginSupport.getPluginName() + "] was found but version " + version + ", is not supported.");
            return;
        }

        pluginSupport.load(plugin);
        enabledSupport.add(pluginSupport);
        logger.info("Enabling plugin support for " + pluginSupport.getPluginName() + ", enabling additional checks and features");
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin plugin = event.getPlugin();
        String pluginName = plugin.getName();

        for (PluginSupport service : allSupport) {
            if (!service.getPluginName().equals(pluginName)) {
                continue;
            }

            if (!service.canLoad()) {
                continue;
            }

            if (enabledSupport.contains(service)) {
                continue;
            }

            String version = plugin.getDescription().getVersion();

            if (service.isUnsupportedVersion(version)) {
                logger.warning("Plugin [" + service.getPluginName() + "] was found but version " + version + ", is not supported.");
                continue;
            }

            logger.info("Enabling plugin support for " + service.getPluginName() + ", enabling additional checks and features");
            enabledSupport.add(service);
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        Plugin plugin = event.getPlugin();
        String pluginName = plugin.getName();

        for (PluginSupport support : enabledSupport) {
            if (!support.getPluginName().equals(pluginName)) {
                continue;
            }

            logger.info("Disabling plugin support for " + support.getPluginName() + ", disabling additional checks and features");
            enabledSupport.remove(support);
        }
    }

    public Set<PluginSupport> getEnabledSupport() {
        return enabledSupport;
    }

    public PluginSupportDistributor getSupportDistributor() {
        return supportDistributor;
    }
}