package net.crashcraft.crashclaim.visualize;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.data.StaticClaimLogic;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.VisualProvider;
import net.crashcraft.crashclaim.visualize.api.providers.BlockVisualProvider;
import net.crashcraft.crashclaim.visualize.api.providers.GlowVisualProvider;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class VisualizationManager {
    private final HashMap<UUID, VisualGroup> visualHashMap;
    private final HashMap<BaseVisual, Long> timeMap;
    private final VisualProvider provider;
    private final BlockVisualProvider blockVisualProvider;

    public VisualizationManager(CrashClaim crashClaim){
        this.visualHashMap = new HashMap<>();
        this.timeMap = new HashMap<>();

        blockVisualProvider = new BlockVisualProvider();
        if (GlobalConfig.visual_type.equals("glow")){
            provider = new GlowVisualProvider();
        } else {
            provider = blockVisualProvider;
        }

        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();

        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();

        for (VisualColor color : VisualColor.values()){
            if (scoreboard.getTeam(color.name()) == null) {
                Team team = scoreboard.registerNewTeam(color.name());
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
                team.setColor(ChatColor.valueOf(color.name()));
            }
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(crashClaim, () -> {
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
        }, 0, 20L);

        crashClaim.saveConfig();
    }

    public VisualProvider getProvider(UUID target){
        if (target.getMostSignificantBits() == 0){
            return blockVisualProvider;
        } else {
            return provider;
        }
    }

    public void sendAlert(Player player, BaseComponent[] message){
        if (message == null || message.length < 1){
            return;
        }
        player.sendActionBar(message);
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

    public void deSpawnAfter(BaseVisual visual, int seconds){
        timeMap.put(visual, System.currentTimeMillis() + (seconds * 1000L));
    }

    public void visualizeSurroundingClaims(Player player, ClaimDataManager claimDataManager){
        long chunkx = player.getLocation().getChunk().getX();
        long chunkz = player.getLocation().getChunk().getZ();

        Long2ObjectOpenHashMap<ArrayList<Integer>> chunks = claimDataManager.getClaimChunkMap(player.getWorld().getUID());

        ArrayList<Integer> tempClaims = new ArrayList<>();

        for (long x = chunkx - 6; x <= chunkx + 6; x++){
            for (long z = chunkz + 6; z >= chunkz - 6; z--){
                ArrayList<Integer> chu = chunks.get(StaticClaimLogic.getChunkHash(x, z));

                if (chu == null) {
                    continue;
                }

                for (Integer integer : chu){
                    if (!tempClaims.contains(integer)) {
                        tempClaims.add(integer);
                    }
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
            BaseVisual visual = getProvider(player.getUniqueId()).spawnClaimVisual(null, group, claim, y);
            visual.spawn();
        }
    }

    public void visualizeSurroundingSubClaims(Claim claim, Player player){
        ArrayList<SubClaim> subClaims = claim.getSubClaims();
        VisualGroup group = fetchVisualGroup(player, true);
        group.removeAllVisuals();

        int y = player.getLocation().getBlockY();

        getProvider(player.getUniqueId()).spawnClaimVisual(VisualColor.WHITE, group, claim, y - 1).spawn();

        for (SubClaim subClaim : subClaims){
            getProvider(player.getUniqueId()).spawnClaimVisual(null, group, subClaim, y).spawn();
        }
    }

    public void cleanup(Player player) {
        VisualGroup group = fetchVisualGroup(player, false);
        if (group != null){
            for (BaseVisual visual : group.getActiveVisuals()){
                timeMap.remove(visual);
            }
            group.removeAllVisuals();
        }
        visualHashMap.remove(player.getUniqueId());
    }

    public VisualProvider getProvider(){
        return provider;
    }
}
