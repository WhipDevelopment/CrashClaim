package net.crashcraft.crashclaim.commands.claiming.modes;

import dev.whip.crashutils.menusystem.defaultmenus.ConfirmationMenu;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.commands.claiming.ClaimCommand;
import net.crashcraft.crashclaim.commands.claiming.ClaimMode;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.data.ClaimResponse;
import net.crashcraft.crashclaim.data.ContributionManager;
import net.crashcraft.crashclaim.data.StaticClaimLogic;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.visualize.VisualizationManager;
import net.crashcraft.crashclaim.visualize.api.BaseVisual;
import net.crashcraft.crashclaim.visualize.api.VisualColor;
import net.crashcraft.crashclaim.visualize.api.VisualGroup;
import net.crashcraft.crashclaim.visualize.api.VisualType;
import net.crashcraft.crashpayment.payment.TransactionType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NewClaimMode implements ClaimMode {
    private final ClaimCommand commandManager;
    private final Player player;
    private final Location firstLocation;
    private final VisualizationManager visualizationManager;
    private final ClaimDataManager manager;

    public NewClaimMode(ClaimCommand commandManager, Player player, Location firstLocation){
        this.commandManager = commandManager;
        this.player = player;
        this.firstLocation = firstLocation;
        this.visualizationManager = commandManager.getVisualizationManager();
        this.manager = commandManager.getDataManager();

        player.sendMessage(Localization.NEW_CLAIM__CLICK_CORNER.getMessage());

        VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
        group.removeAllVisualsOfType(VisualType.MARKER);

        visualizationManager.getProvider().spawnMarkerVisual(VisualColor.YELLOW, group, firstLocation).spawn();
    }

    private boolean checkCanCreate(Location min, Location max){
        if ((max.getBlockX() - min.getBlockX()) < 4 || (max.getBlockZ() - min.getBlockZ()) < 4) {
            player.sendMessage(Localization.NEW_CLAIM__MIN_SIZE.getMessage());
            return false;
        }

        if (manager.checkOverLapSurroudningClaims(-1, max.getBlockX(), max.getBlockZ(), min.getBlockX(), min.getBlockZ(), min.getWorld().getUID())){
            player.sendMessage(Localization.NEW_CLAIM__OVERLAPPING.getMessage());
            return false;
        }

        if (!CrashClaim.getPlugin().getPluginSupport().canClaim(min, max)){
            player.sendMessage(Localization.NEW_CLAIM__OTHER_ERROR.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public void click(Player player, Location secondLocation) {
        Location min = StaticClaimLogic.calculateMinCorner(firstLocation, secondLocation);
        Location max = StaticClaimLogic.calculateMaxCorner(firstLocation, secondLocation);

        if (!checkCanCreate(min, max)){
            return;
        }

        int area = ContributionManager.getArea(min.getBlockX(), min.getBlockZ(), max.getBlockX(), max.getBlockZ());

        int price = (GlobalConfig.bypassModeBypassesMoney && PermissionHelper.getPermissionHelper().getBypassManager().isBypass(player.getUniqueId())) ?
                0 : (int) Math.ceil(area * GlobalConfig.money_per_block);
        String priceString = Integer.toString(price);

        if (price > 0){
            new ConfirmationMenu(player,
                    Localization.NEW_CLAIM__CREATE_MENU__TITLE.getMessage(),
                    Localization.NEW_CLAIM__CREATE_MENU__MESSAGE.getItem("price", priceString),
                    Localization.NEW_CLAIM__CREATE_MENU__ACCEPT.getItem("price", priceString),
                    Localization.NEW_CLAIM__CREATE_MENU__DENY.getItem("price", priceString),
                    (p, aBoolean) -> {
                        if (aBoolean){
                            if (!checkCanCreate(min, max)){
                                return "";
                            }

                            CrashClaim.getPlugin().getPayment().makeTransaction(player.getUniqueId(), TransactionType.WITHDRAW, "Claim Purchase", price, (res) -> {
                                if (!res.transactionSuccess()){
                                    player.sendMessage(Localization.NEW_CLAIM__NOT_ENOUGH_BALANCE.getMessage("price", priceString));
                                    cleanup(player.getUniqueId(), true);
                                    return;
                                }

                                Bukkit.getScheduler().runTask(CrashClaim.getPlugin(), () -> afterTransaction(min, max, area));
                            });
                        }
                        return "";
                    },
                    p -> {
                        cleanup(player.getUniqueId(), true);
                        return "";
                    }).open();
        } else {
            afterTransaction(min, max, 0); // set area to 0 to not add any money into economy
        }
    }

    private void afterTransaction(Location min, Location max, int area){
        ClaimResponse response = manager.createClaim(max, min, player.getUniqueId());

        if (response.isStatus()) {
            ((Claim) response.getClaim()).addContribution(player.getUniqueId(), area); //Contribution tracking

            player.sendMessage(Localization.NEW_CLAIM__SUCCESS.getMessage());

            VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
            group.removeAllVisuals();

            BaseVisual visual = visualizationManager.getProvider().spawnClaimVisual(VisualColor.GREEN, group, response.getClaim(), player.getLocation().getBlockY() - 1);
            visual.spawn();

            visualizationManager.deSpawnAfter(visual, 5);

            cleanup(player.getUniqueId(), false);
        } else {
            player.sendMessage(Localization.NEW_CLAIM__ERROR.getMessage());
            cleanup(player.getUniqueId(), true);
        }
    }

    @Override
    public void cleanup(UUID player, boolean visuals) {
        commandManager.forceCleanup(player, visuals);
    }
}
