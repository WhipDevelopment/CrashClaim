package net.crashcraft.crashclaim.visualize;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
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
import net.crashcraft.crashclaim.visualize.api.visuals.BaseGlowVisual;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class VisualizationManager {
    private final ProtocolManager protocolManager;
    private final HashMap<UUID, VisualGroup> visualHashMap;

    private final HashMap<BaseVisual, Long> timeMap;

    private final VisualProvider provider;

    public VisualizationManager(CrashClaim crashClaim, ProtocolManager protocolManager){
        this.protocolManager = protocolManager;

        this.visualHashMap = new HashMap<>();
        this.timeMap = new HashMap<>();

        if (GlobalConfig.visual_type.equals("glow")){
            provider = new GlowVisualProvider();
        } else {
            provider = new BlockVisualProvider();
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

        BaseGlowVisual.setProtocolManager(protocolManager);

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
        },0,20L);

        crashClaim.saveConfig();
    }

    public void sendAlert(Player player, BaseComponent[] message){
        if (message == null || message.length < 1){
            return;
        }

        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.TITLE);

        packet.getTitleActions().write(0, GlobalConfig.visual_alert_type);
        packet.getChatComponents().write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(message)));
        packet.getIntegers().write(0, GlobalConfig.visual_alert_fade_in);
        packet.getIntegers().write(1, GlobalConfig.visual_alert_duration);
        packet.getIntegers().write(2, GlobalConfig.visual_alert_fade_out);

        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
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

    public void visualizeSuroudningClaims(Player player, ClaimDataManager claimDataManager){
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

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
