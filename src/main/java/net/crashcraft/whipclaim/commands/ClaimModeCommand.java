package net.crashcraft.whipclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import net.crashcraft.whipclaim.data.ClaimResponse;
import net.crashcraft.whipclaim.data.StaticClaimLogic;
import net.crashcraft.whipclaim.visualize.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@CommandAlias("claim")
@CommandPermission("whipclaim.user.claim")
public class ClaimModeCommand extends BaseCommand implements Listener {
    private ClaimDataManager manager;
    private VisualizationManager visualizationManager;

    private ArrayList<UUID> enabledMode;
    private HashMap<UUID, Location> clickMap;

    public ClaimModeCommand(WhipClaim whipClaim){
        manager = whipClaim.getDataManager();
        visualizationManager = whipClaim.getVisualizationManager();

        enabledMode = new ArrayList<>();
        clickMap = new HashMap<>();
    }

    @Subcommand("debug")
    public void debug(Player player){
        Location location = player.getLocation();
        ArrayList<Integer> claims = manager.temporaryTestGetChunkMap().get(player.getWorld().getUID()).get(StaticClaimLogic.getChunkHashFromLocation(location.getBlockX(), location.getBlockZ()));
        ArrayList<Claim> claimList = new ArrayList<>();
        for (Integer integer : claims){
            claimList.add(manager.temporaryTestGetClaimMap().get(integer));
        }

        for (Claim claim : claimList){
            System.out.println(claim.getId());
            System.out.println("|--Ux  " + claim.getUpperCornerX());
            System.out.println("|--Uz  " + claim.getUpperCornerZ());
            System.out.println("|--Lx  " + claim.getLowerCornerX());
            System.out.println("|--Lz  " + claim.getLowerCornerZ());
        }
    }

    @Default
    public void onClaim(Player player){
        UUID uuid = player.getUniqueId();
        if (enabledMode.contains(uuid)){
            player.sendMessage(ChatColor.RED + "Claim mode disabled");
            enabledMode.remove(uuid);
            clickMap.remove(uuid);
        } else {
            enabledMode.add(uuid);

            player.sendMessage(ChatColor.GREEN + "Claim mode enabled, click 2 corners to claim.");
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e){
        if (e.getHand() != null && e.getHand().equals(EquipmentSlot.HAND) && e.getClickedBlock() != null){
            click(e.getPlayer(), e.getClickedBlock().getLocation());
        }
    }

    public void click(Player player, Location loc1){
        World world = loc1.getWorld();

        if (world == null) {
            cleanup(player.getUniqueId());
            throw new RuntimeException("World was null on claim mode manager click");
        }

        Claim claim = manager.getClaim(loc1.getBlockX(), loc1.getBlockZ(), loc1.getWorld().getUID());

        if (claim != null){
            clickedExistingClaim(player, loc1);
            return;
        }

        clickClaim(player, loc1);
    }

    public void clickClaim(Player player, Location location){
        //TODO check for disabled worlds and disabled locations - bypass mode should bypass this

        UUID uuid = player.getUniqueId();
        UUID world = player.getWorld().getUID();

        if (!clickMap.containsKey(uuid)){
            clickMap.put(uuid, location);
            player.sendMessage(ChatColor.GREEN + "Click the an opposite corner to form a new claim.");
            return;
        }

        Location loc2 = clickMap.get(uuid);

        Location upperCorner = StaticClaimLogic.calculateUpperCorner(loc2, location);
        Location lowerCorner = StaticClaimLogic.calculateLowerCorner(loc2, location);

        if ((lowerCorner.getBlockX() - upperCorner.getBlockX()) < 4 || (lowerCorner.getBlockZ() - upperCorner.getBlockZ()) < 4) {
            player.sendMessage(ChatColor.RED + "A claim has to be at least a 5x5");
            cleanup(player.getUniqueId());
            return;
        }

        if (manager.checkOverLapSurroudningClaims(upperCorner.getBlockX(), upperCorner.getBlockZ(), lowerCorner.getBlockX(), lowerCorner.getBlockZ(), world)){
            player.sendMessage(ChatColor.RED + "You cannot claim over an existing claim.");
            cleanup(player.getUniqueId());
            return;
        }

        ClaimResponse response = manager.createClaim(upperCorner, lowerCorner, uuid);

        if (response.isStatus()) {
            player.sendMessage(ChatColor.GREEN + "Claim has been successfully created.");

            VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
            ClaimVisual claimVisual = new ClaimVisual(response.getClaim(), player.getLocation().getBlockY() - 1);
            group.addVisual(claimVisual);

            claimVisual.spawn();
            claimVisual.color(TeamColor.GREEN);
        } else {
            player.sendMessage(ChatColor.RED + "Error creating claim");
        }

        cleanup(player.getUniqueId());
    }

    public void clickedExistingClaim(Player player, Location location){

    }

    private void cleanup(UUID uuid){
        clickMap.remove(uuid);
        enabledMode.remove(uuid);
    }

    private static boolean isClaimBorder(int NWCorner_x, int SECorner_x, int NWCorner_z, int SECorner_z, int Start_x, int Start_z) {
        return Start_x == NWCorner_x || Start_x == SECorner_x || Start_z == NWCorner_z || Start_z == SECorner_z;
    }

    private static int[] calculateResize(int NWCorner_x, int SECorner_x, int NWCorner_z, int SECorner_z, int Start_x, int Start_z, int End_x, int End_z) {
        /*
        Works for any size changes: corners or sides.
        Note:
        NWCorner_x defines the West Line
        SECorner_x defines the East Line

        NWCorner_z defines the North Line
        SECorner_z defines the South Line
         */

        int newNWCorner_x = NWCorner_x;
        int newSECorner_x = SECorner_x;
        int newNWCorner_z = NWCorner_z;
        int newSECorner_z = SECorner_z;

        int change = 0;

        if (Start_x == NWCorner_x) {
            /*Start is West*/
            newNWCorner_x = End_x;
        }

        if (Start_x == SECorner_x) {
            /*Start is East*/
            newSECorner_x = End_x;
        }

        if (Start_z == NWCorner_z) {
            /*Start is North*/
            newNWCorner_z = End_z;
        }

        if (Start_z == SECorner_z) {
            /*Start is South*/
            newSECorner_z = End_z;
        }

        if (newSECorner_x > NWCorner_x && newNWCorner_x < SECorner_x && newSECorner_z > NWCorner_z && newNWCorner_z < SECorner_z) {
            NWCorner_x = newNWCorner_x;
            SECorner_x = newSECorner_x;
            NWCorner_z = newNWCorner_z;
            SECorner_z = newSECorner_z;
            change = 1;
        }

        int[] arr = new int[5];

        arr[0] = NWCorner_x;
        arr[1] = SECorner_x;
        arr[2] = NWCorner_z;
        arr[3] = SECorner_z;
        arr[4] = change;

        return arr;
    }
}
