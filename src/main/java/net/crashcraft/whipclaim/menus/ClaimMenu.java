package net.crashcraft.whipclaim.menus;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.menu.defaultmenus.ConfirmationMenu;
import net.crashcraft.menu.defaultmenus.PlayerListMenu;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class ClaimMenu extends GUI {
    private Claim claim;
    private Material material;

    public ClaimMenu(Player player, Claim claim) {
        super(player, "Claim Menu", 54);
        this.claim = claim;
        setupGUI();
    }

    @Override
    public void initialize() {
        material = Material.OAK_FENCE; //TODO  make this dynamic from config
    }

    @Override
    public void loadItems() {
        inv.setItem(13, createGuiItem(ChatColor.GOLD + claim.getName(),
                new ArrayList<>(Arrays.asList(
                        ChatColor.GREEN + "NW Corner: " + ChatColor.YELLOW + claim.getUpperCornerX() +
                                ", " + claim.getUpperCornerZ(),
                        ChatColor.GREEN + "SE Corner: " + ChatColor.YELLOW + claim.getLowerCornerX() +
                                ", " + claim.getLowerCornerZ())),
                material));

        inv.setItem(28, createGuiItem(ChatColor.GOLD + "Per Player settings",
                new ArrayList<>(Collections.singleton(ChatColor.GREEN + "Edit claim permissions on a per player basis")), Material.PLAYER_HEAD));

        inv.setItem(29, createGuiItem(ChatColor.GOLD + "Global Claim settings",
                new ArrayList<>(Collections.singleton(ChatColor.GREEN + "Set global permissions for your claim")), Material.COMPASS));

        inv.setItem(30, createGuiItem(ChatColor.GOLD + "Sub Claims",
                new ArrayList<>(Collections.singleton(ChatColor.GREEN + "View the a list of the sub claims for this claim")), Material.WRITABLE_BOOK));

        inv.setItem(32, createGuiItem(ChatColor.GOLD + "Rename Claim",
                new ArrayList<>(Collections.singleton(ChatColor.GREEN + "Rename your claim to easily identify it")), Material.ANVIL));

        inv.setItem(33, createGuiItem(ChatColor.GOLD + "Edit Entry Message",
                new ArrayList<>(Collections.singleton(ChatColor.GREEN + "Edit the entry message of your claim")), Material.ANVIL));

        inv.setItem(34, createGuiItem(ChatColor.GOLD + "Edit Exit Message",
                new ArrayList<>(Collections.singleton(ChatColor.GREEN + "Edit the exit message of your claim")), Material.ANVIL));

        inv.setItem(49, createGuiItem(ChatColor.GOLD + "Delete Claim",
                new ArrayList<>(Collections.singleton(ChatColor.GREEN + "Delete your claim permanently")), Material.RED_CONCRETE));

        inv.setItem(45, createGuiItem(ChatColor.GOLD + "Back", Material.ARROW));
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        switch (rawItemName){
            case "per player settings":
                ArrayList<UUID> uuids = new ArrayList<>(claim.getPerms().getPlayerPermissions().keySet());

                for (Player player : Bukkit.getOnlinePlayers()){
                    if (!uuids.contains(player.getUniqueId()))
                        uuids.add(player.getUniqueId());
                }

                uuids.remove(getPlayer().getUniqueId());    //Cant modify perms of yourself
                uuids.remove(claim.getOwner());    //Owners permissions are off limits.

                new PlayerListMenu(getPlayer(), this, uuids, (player, uuid) -> {
                    new PlayerPermissionMenu(player, claim.getPerms(), uuid).open();
                    return "";
                }).open();
                break;
            case "global claim settings":
                new GlobalPermissionMenu(player, claim, this).open();
                break;
            case "rename claim":
                new AnvilGUI(WhipClaim.getPlugin(), getPlayer(), "Enter new claim name", (player, reply) -> {
                    claim.setName(reply);
                    player.sendMessage(ChatColor.GREEN + "Change claim name to " + ChatColor.GOLD + reply);

                    //TODO Make sure they cant set duplicate names maybe?

                    return null;
                });
                break;
            case "edit entry message":
                new AnvilGUI(WhipClaim.getPlugin(), getPlayer(), "Enter new claim entry message", (player, reply) -> {
                    claim.setExitMessage(reply);
                    player.sendMessage(ChatColor.GREEN + "Change claim entry message to " + ChatColor.GOLD + reply);

                    return null;
                });
                break;
            case "edit exit message":
                new AnvilGUI(WhipClaim.getPlugin(), getPlayer(), "Enter new claim exit message", (player, reply) -> {
                    claim.setEntryMessage(reply);
                    player.sendMessage(ChatColor.GREEN + "Change claim exit message to " + ChatColor.GOLD + reply);

                    return null;
                });
                break;
            case "delete claim":
                new ConfirmationMenu(getPlayer(),"Confirm Delete Claim",
                        ChatColor.DARK_RED + "Permanently Delete this claim?",
                        new ArrayList<>(Arrays.asList(ChatColor.RED + "Claim Blocks will be restored to ",
                                ChatColor.RED + "the contributing parties")),
                        material,
                        (player, aBoolean) -> {
                            if (aBoolean.equals(true)) {
                                //ClaimManager.getClaimManager().removeClaim(UserCache.getUser(player), claimObject);
                                //TODO add remove claim here
                            }
                            return "";
                        }, player -> "").open();
                break;
            case "back":

                break;
        }
    }
}
