package net.crashcraft.whipclaim.visualize;

import org.bukkit.entity.Player;

import javax.xml.stream.Location;
import java.util.*;

public class VisualGroup {
    /**
     * Fix this because we catn identiy the entities andn where they came from. Makes depsawning them uselsess
     * Maybe use this class as a holder and keep the maps seperate in the visual objects
     * This can be the controller for the sub visual objecs
     */

    private HashMap<Integer, UUID> fakeEntities; // id - uuid
    private HashMap<Integer, Location> entityLocations;

    private ArrayList<Visual> activeVisuals;
    private Player player;

    public VisualGroup(Player player) {
        this.player = player;
        activeVisuals = new ArrayList<>();
    }

    public void signalRemoval(Visual visual){
        activeVisuals.remove(visual);

        for(Iterator<Map.Entry<Integer, UUID>> it = fakeEntities.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, UUID> entry = it.next();

        }
    }

    public ArrayList<Visual> getActiveVisuals() {
        return activeVisuals;
    }

    public Player getPlayer() {
        return player;
    }


}
