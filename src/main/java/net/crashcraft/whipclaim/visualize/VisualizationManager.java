package net.crashcraft.whipclaim.visualize;

import net.crashcraft.whipclaim.WhipClaim;
import org.bukkit.Bukkit;

import java.util.*;

public class VisualizationManager {
    private HashMap<UUID, VisualGroup> visualHashMap;

    private HashMap<Visual, Long> timeMap;

    public VisualizationManager(WhipClaim claim){
        visualHashMap = new HashMap<>();
        timeMap = new HashMap<>();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(claim, () -> {
            if (timeMap.size() == 0)
                return;

            long time = System.currentTimeMillis();

            for(Iterator<Map.Entry<Visual, Long>> it = timeMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Visual, Long> entry = it.next();
                if (entry.getValue() <= time){
                    entry.getKey().remove();
                    it.remove();
                }
            }
        },0,20);
    }

    public void despawnAfter(Visual visual, int seconds){
        timeMap.put(visual, System.currentTimeMillis() + (seconds * 1000));
    }
}
