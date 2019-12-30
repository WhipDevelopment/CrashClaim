package net.crashcraft.whipclaim.visualize;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import net.crashcraft.whipclaim.config.ValueConfig;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.data.StaticClaimLogic;
import net.crashcraft.whipclaim.visualize.api.*;
import net.crashcraft.whipclaim.visualize.api.providers.BlockVisualProvider;
import net.crashcraft.whipclaim.visualize.api.providers.GlowVisualProvider;
import net.crashcraft.whipclaim.visualize.api.visuals.BaseGlowVisual;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class VisualizationManager {
    private ProtocolManager protocolManager;
    private HashMap<UUID, VisualGroup> visualHashMap;

    private HashMap<BaseVisual, Long> timeMap;

    private VisualProvider provider;

    public VisualizationManager(WhipClaim whipClaim, ProtocolManager protocolManager){
        this.protocolManager = protocolManager;

        visualHashMap = new HashMap<>();
        timeMap = new HashMap<>();

        if (ValueConfig.VISUALIZE_VISUAL_TYPE.equals("glow")){
            provider = new GlowVisualProvider();
        } else {
            provider = new BlockVisualProvider();
        }

        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();

        if (scoreboardManager == null)
            throw new RuntimeException("Scoreboard manager was null.");

        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();

        for (VisualColor color : VisualColor.values()){
            if (scoreboard.getTeam(color.name()) == null) {
                Team team = scoreboard.registerNewTeam(color.name());
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
                team.setColor(ChatColor.valueOf(color.name()));
            }
        }

        BaseGlowVisual.setProtocolManager(protocolManager);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(whipClaim, () -> {
            if (timeMap.size() != 0) {
                long time = System.currentTimeMillis();

                for (Iterator<Map.Entry<BaseVisual, Long>> it = timeMap.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<BaseVisual, Long> entry = it.next();
                    if (entry.getValue() <= time) {
                        entry.getKey().getParent().removeVisual(entry.getKey());
                        it.remove();
                    }
                }
            }
        },0,20L);

        String key = "visualization.visual-colors.";
        FileConfiguration configuration = whipClaim.getConfig();
        for (VisualColor color : VisualColor.values()){
            String material = configuration.getString(key + color.name());
            if (material == null){
                configuration.set(key + color.name(), Material.ORANGE_CONCRETE.name());
                color.setMaterial(Material.ORANGE_CONCRETE);
                continue;
            }
            Material mat = Material.getMaterial(material);
            if (mat == null){
                configuration.set(key + color.name(), Material.ORANGE_CONCRETE.name());
                whipClaim.getLogger().warning(key + color.name() + ", is not a valid material, defaulting to ORANGE_CONCRETE");
                color.setMaterial(Material.ORANGE_CONCRETE);
                continue;
            }
            color.setMaterial(mat);
        }

        whipClaim.saveConfig();
    }

    public VisualGroup fetchExistingGroup(UUID uuid){
        return visualHashMap.get(uuid);
    }

    public VisualGroup fetchVisualGroup(Player player, boolean create){
        if (visualHashMap.containsKey(player.getUniqueId())){
            return visualHashMap.get(player.getUniqueId());
        } else if (create){
            VisualGroup group = new VisualGroup(player, this);
            visualHashMap.put(player.getUniqueId(), group);
            return group;
        }
        return null;
    }

    public void despawnAfter(BaseVisual visual, int seconds){
        timeMap.put(visual, System.currentTimeMillis() + (seconds * 1000));
    }

    public void colorEntities(Player player, VisualColor color, ArrayList<String> uuids){
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);

        packet.getStrings()
                .write(0, color.toString());
        packet.getIntegers().write(0, 3);

        packet.getSpecificModifier(Collection.class)
                .write(0, uuids);

        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }



    public void visualizeSuroudningClaims(Player player, ClaimDataManager claimDataManager){
        long chunkx = player.getLocation().getChunk().getX();
        long chunkz = player.getLocation().getChunk().getZ();

        Long2ObjectOpenHashMap<ArrayList<Integer>> chunks = claimDataManager.getClaimChunkMap(player.getWorld().getUID());

        ArrayList<Integer> tempClaims = new ArrayList<>();

        for (long x = chunkx - 6; x <= chunkx + 6; x++){
            for (long z = chunkz + 6; z >= chunkz - 6; z--){
                ArrayList<Integer> chu = chunks.get(StaticClaimLogic.getChunkHash(x, z));

                if (chu == null)
                    continue;

                for (Integer integer : chu){
                    if (!tempClaims.contains(integer))
                        tempClaims.add(integer);
                }
            }
        }

        ArrayList<Claim> claims = new ArrayList<>();
        for (Integer integer : tempClaims){
            claims.add(claimDataManager.getClaim(integer));
        }

        VisualGroup group = fetchVisualGroup(player, true);
        group.removeAllVisuals();

        int y = player.getLocation().getBlockY() - 1;

        for (Claim claim : claims){
            BaseVisual visual = provider.spawnClaimVisual(null, group, claim, y);
            visual.spawn();
        }
    }

    public void visualizeSuroudningSubClaims(Claim claim, Player player){
        ArrayList<SubClaim> subClaims = claim.getSubClaims();
        VisualGroup group = fetchVisualGroup(player, true);
        group.removeAllVisuals();

        int y = player.getLocation().getBlockY();

        provider.spawnClaimVisual(VisualColor.WHITE, group, claim, y - 1).spawn();

        for (SubClaim subClaim : subClaims){
            provider.spawnClaimVisual(null, group, subClaim, y).spawn();
        }
    }
/*
    public void visualizeSuroudningSubClaims(Player player, int y, ArrayList<SubClaim> claims){
        VisualGroup group = fetchVisualGroup(player, true);
        group.removeAllVisuals();

        for (SubClaim subClaim : claims){
            provider.spawnClaimVisual(null, )

            SubClaimVisual subClaimVisual = new SubClaimVisual(subClaim, y);

            group.addVisual(subClaimVisual);

            subClaimVisual.spawn();
            subClaimVisual.color(null);
        }
    }

 */

    public VisualProvider getProvider(){
        return provider;
    }
}
