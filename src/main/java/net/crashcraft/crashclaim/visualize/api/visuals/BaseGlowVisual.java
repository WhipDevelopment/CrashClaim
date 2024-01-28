package net.crashcraft.crashclaim.visualize.api.visuals;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.VisualType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public abstract class BaseGlowVisual extends BaseVisual {
    private final HashMap<Integer, String> fakeEntities; // id - uuid
    private final HashMap<Integer, Location> entityLocations; // check for hit collision on custom interact event

    public BaseGlowVisual(VisualType type, VisualColor color, VisualGroup parent, Player player, int y) {
        super(type, color, parent, player, y);
        this.fakeEntities = new HashMap<>();
        this.entityLocations = new HashMap<>();
    }

    public BaseGlowVisual(VisualType type, VisualColor color, VisualGroup parent, Player player, int y, BaseClaim claim) {
        super(type, color, parent, player, y, claim);

        this.fakeEntities = new HashMap<>();
        this.entityLocations = new HashMap<>();
    }

    public void spawnEntity(int x, int z, int y){
        spawnEntity(getPlayer(),
                x,
                z,
                y,
                getParent().generateUniqueID(),
                getParent().generateUniqueUUID());
    }

    public void removeAll(){
        CrashClaim.getPlugin().getHandler().removeEntity(getPlayer(), fakeEntities.keySet());
    }

    public void colorEntities(Player player, VisualColor color, ArrayList<String> uuids){
        CrashClaim.getPlugin().getHandler().setEntityTeam(player, color.toString(), uuids);
    }

    public void spawnEntity(Player player, int x, int z, int y, int id, UUID uuid){
        double dx = x + 0.5;
        double dz = z + 0.5;

        CrashClaim.getPlugin().getHandler().spawnGlowingInvisibleMagmaSlime(
                player,
                dx,
                dz,
                y,
                id,
                uuid,
                fakeEntities,
                entityLocations
        );
    }

    public ArrayList<Integer> getEntityIDs(){
        return new ArrayList<>(fakeEntities.keySet());
    }

    public ArrayList<String> getEntityUUIDs(){
        return new ArrayList<>(fakeEntities.values());
    }

    public Location getEntityLocation(int id){
        return entityLocations.get(id);
    }

    public boolean containsID(int id){
        return fakeEntities.containsKey(id);
    }

    public boolean containsUUID(String uuid){
        return fakeEntities.containsValue(uuid);
    }
}
