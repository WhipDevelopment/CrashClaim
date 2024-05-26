package net.crashcraft.crashclaim.visualize.api;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class VisualGroup {
    private final ArrayList<BaseVisual> activeVisuals;
    private final Player player;
    private final VisualizationManager manager;

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
        for (Iterator<BaseVisual> it = activeVisuals.iterator(); it.hasNext();){
            BaseVisual visual = it.next();
            if (visual.getType().equals(type)) {
                visual.remove();
                it.remove();
            }
        }
    }

    public int generateUniqueID(){
        return Bukkit.getServer().getUnsafe().nextEntityId();
    }

    public UUID generateUniqueUUID(){
        return UUID.randomUUID();
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
