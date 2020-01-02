package net.crashcraft.whipclaim.visualize.api;

import net.crashcraft.whipclaim.visualize.VisualizationManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class VisualGroup {
    private int uniqueID = 21346787;

    private ArrayList<BaseVisual> activeVisuals;
    private Player player;
    private VisualizationManager manager;

    public VisualGroup(Player player, VisualizationManager manager) {
        this.player = player;
        this.manager = manager;
        activeVisuals = new ArrayList<>();
    }

    public void addVisual(BaseVisual visual){
        activeVisuals.add(visual);
    }

    public void removeVisual(BaseVisual visual){
        visual.remove();
        activeVisuals.remove(visual);
    }

    public void removeAllVisuals(){
        for (Iterator<BaseVisual> it = activeVisuals.iterator(); it.hasNext();){
            BaseVisual visual = it.next();
            visual.remove();
            it.remove();
        }
    }

    public void removeAllVisualsOfType(VisualType type){
        if (activeVisuals == null)
            return;

        for (Iterator<BaseVisual> it = activeVisuals.iterator(); it.hasNext();){
            BaseVisual visual = it.next();
            if (visual.getType().equals(type)) {
                visual.remove();
                it.remove();
            }
        }
    }

    public int generateUiniqueID(){
        return uniqueID++;
    }

    public UUID generateUiniqueUUID(){
        UUID uuid = UUID.randomUUID();
        if (Bukkit.getEntity(uuid) == null){
            return uuid;
        }
        return generateUiniqueUUID();
    }

    public ArrayList<BaseVisual> getActiveVisuals() {
        return activeVisuals;
    }

    public Player getPlayer() {
        return player;
    }

    public VisualizationManager getManager() {
        return manager;
    }
}
