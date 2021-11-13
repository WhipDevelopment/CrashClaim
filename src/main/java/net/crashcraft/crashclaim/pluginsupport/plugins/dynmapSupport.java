package net.crashcraft.crashclaim.pluginsupport.plugins;

import net.crashcraft.crashclaim.pluginsupport.PluginSupport;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

public class dynmapSupport implements PluginSupport {
    private DynmapAPI dynmapAPI;
    private MarkerSet markerSet;

    @Override
    public boolean isUnSupportedVersion(String version) {
        return true; // Idk the api is wack
    }

    @Override
    public void init(Plugin plugin) {
        dynmapAPI = (DynmapAPI) plugin;

        markerSet = dynmapAPI.getMarkerAPI().createMarkerSet("net.crashcraft.crashclaim.claims", "Claims", dynmapAPI.getMarkerAPI().getMarkerIcons(), false);

/*        AreaMarker areaMarker = markerSet.createAreaMarker("id", "label", true, "world", x, y, false);
        areaMarker.setFillStyle(0.5, hex);*/
    }

    @Override
    public void disable() {

    }

    @Override
    public boolean canClaim(Location minLoc, Location maxLoc) {
        return true;
    }
}
