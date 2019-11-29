package net.crashcraft.whipclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.data.StaticClaimLogic;
import net.crashcraft.whipclaim.visualize.VisualGroup;
import net.crashcraft.whipclaim.visualize.VisualizationManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@CommandAlias("showclaims")
public class ShowClaimsCommand extends BaseCommand {
    private VisualizationManager visualizationManager;
    private ClaimDataManager claimDataManager;

    @Default
    public void showClaims(Player player){
        long chunkx = player.getLocation().getChunk().getX();
        long chunkz = player.getLocation().getChunk().getZ();

        Long2ObjectOpenHashMap<ArrayList<Integer>> chunks = claimDataManager.getClaimChunkMap(player.getWorld().getUID());

        ArrayList<Integer> tempClaims = new ArrayList<>();

        for (long x = chunkx - 6; x <= chunkx + 6; x++){
            for (long z = chunkz + 6; z >= chunkz - 6; z--){
                for (Integer integer : chunks.get(StaticClaimLogic.getChunkHash(x, z))){
                    if (!tempClaims.contains(integer))
                        tempClaims.add(integer);
                }
            }
        }

        ArrayList<Claim> claims = new ArrayList<>();
        for (Integer integer : tempClaims){
            claims.add(claimDataManager.getClaim(integer));
        }

        VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
        group.removeAllVisuals();

        for (Claim claim : claims){
            group.
        }
    }
}
