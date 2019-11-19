package net.crashcraft.whipclaim.visualize;

import java.util.HashMap;
import java.util.UUID;

public class VisualizationManager {
    private HashMap<UUID, ClaimVisual> visualHashMap;

    public VisualizationManager(){
        visualHashMap = new HashMap<>();
    }
}
