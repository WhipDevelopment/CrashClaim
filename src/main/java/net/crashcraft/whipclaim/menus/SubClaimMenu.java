package net.crashcraft.whipclaim.menus;

import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.menu.defaultmenus.ConfirmationMenu;
import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.claimobjects.SubClaim;
import net.crashcraft.whipclaim.menus.player.PlayerPermListMenu;
import net.crashcraft.whipclaim.menus.sub.GlobalSubClaimPermissionsMenu;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class SubClaimMenu extends GUI {
    private SubClaim claim;
    private Material material;

    public SubClaimMenu(Player player, SubClaim claim) {
        super(player, "Claim Menu", 54);
        this.claim = claim;
        setupGUI();
    }

    @Override
    public void initialize() {
        material = Material.PAPER; //TODO  make this dynamic from config
    }

    @Override
    public void loadItems() {
        inv.setItem(13, createGuiItem(ChatColor.GOLD + claim.getName(),
                new ArrayList<>(Arrays.asList(
                        ChatColor.GREEN + "NW Corner: " + ChatColor.YELLOW + claim.getMinX() +
                                ", " + claim.getMinZ(),
                        ChatColor.GREEN + "SE Corner: " + ChatColor.YELLOW + claim.getMaxX() +
                                ", " + claim.getMaxZ())),
                material));

        inv.setItem(28, createGuiItem(ChatColor.GOLD + "Per Player settings",
                new ArrayList<>(Collections.singleton(ChatColor.GREEN + "Edit claim permissions on a per player basis")), Material.PLAYER_HEAD));

        inv.setItem(29, createGuiItem(ChatColor.GOLD + "Global Claim settings",
                new ArrayList<>(Collections.singleton(ChatColor.GREEN + "Set global permissions for your claim")), Material.COMPASS));

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
        switch (rawItemName) {
            case "per player settings":
                new PlayerPermListMenu(claim, getPlayer(), this);
                break;
            case "global claim settings":
                new GlobalSubClaimPermissionsMenu(getPlayer(), claim.getPerms()).open();
                break;
            case "rename claim":
                new AnvilGUI(WhipClaim.getPlugin(), getPlayer(), "Enter new claim name", (player, reply) -> {
                    claim.setName(reply);
                    player.sendMessage(ChatColor.GREEN + "Change claim name to " + ChatColor.GOLD + reply);

                    //TODO Make sure they cant set duplicate names maybe? might not matter because 2 claims can be named the same by 2 diffferent people then shared

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
                new ConfirmationMenu(getPlayer(), "Confirm Delete Claim",
                        ChatColor.DARK_RED + "Permanently Delete this sub claim?",
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
                Claim main = claim.getParent();
                new RealClaimListMenu(getPlayer(), new ClaimMenu(getPlayer(), main),  "Sub Claims", main.getSubClaims(), Material.PAPER, (p, c) -> {
                    if (c instanceof SubClaim) {
                        SubClaim claim = (SubClaim) c;
                        new SubClaimMenu(getPlayer(), claim).open();
                    }
                    return null;
                }).open();
                break;
        }
    }
}
