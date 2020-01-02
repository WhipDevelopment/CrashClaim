package net.crashcraft.whipclaim.menus;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.menu.defaultmenus.ComplexItemListMenu;
import net.crashcraft.menu.defaultmenus.ItemListMenu;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.cache2k.CacheOperationCompletionListener;

import java.util.*;

public class ClaimListMenu {
    private HashMap<String, Claim> lookupMap = new HashMap<>();

    public ClaimListMenu(Player player, GUI menuInstance){
        HashMap<String, Material> claimHashMap = new HashMap<>();

        ClaimDataManager manager = WhipClaim.getPlugin().getDataManager();

        ArrayList<Integer> ownedClaims = manager.getOwnedClaims().get(player.getUniqueId());

        manager.getClaimCache().prefetchAll(
                ownedClaims,
                new CacheOperationCompletionListener(){
                    @Override
                    public void onCompleted() {
                        ArrayList<Claim> claims = new ArrayList<>(manager.getClaimCache().getAll(ownedClaims).values());

                        for (Claim claim : claims){
                            String name = claim.getName();

                            if (lookupMap.containsKey(name)){
                                name += "(" + claim.getId() + ")";
                            }

                            lookupMap.put(name, claim);

                            claimHashMap.put(name, manager.getMaterialLookup().get(claim.getWorld()));
                        }

                        Bukkit.getScheduler().scheduleSyncDelayedTask(WhipClaim.getPlugin(), () -> {
                            final ComplexItemListMenu menu = new ComplexItemListMenu(player, menuInstance, "Claim List",
                                    sortByValue(claimHashMap),
                                    ChatColor.WHITE,
                                    false,
                                    ((p, itemMeta) -> {
                                        String name = ChatColor.stripColor(itemMeta.getDisplayName());
                                        Claim claim = lookupMap.get(name);
                                        if (claim == null){
                                            name += "(" + claim.getId() + ")";



                                            player.sendMessage(ChatColor.RED + "Unable to find claim.");
                                            return "";
                                        }

                                        new ClaimMenu(player, claim).open();
                                        return "";
                                    })
                            );

                            menu.open();
                        });
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        player.sendMessage(ChatColor.RED + "Error fetching claim list from cache.");
                    }
                }
        );
    }
 
    private static HashMap<String, Material> sortByValue(HashMap<String, Material> hm){
        // Create a list from elements of HashMap
        List<Map.Entry<String, Material> > list =
                new LinkedList<>(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Material> >() {
            public int compare(Map.Entry<String, Material> o1,
                               Map.Entry<String, Material> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Material> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Material> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
}
