package net.crashcraft.crashclaim.commands.claiming.modes;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.commands.claiming.ClaimCommand;
import net.crashcraft.crashclaim.commands.claiming.ClaimMode;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.payment.TransactionType;
import net.crashcraft.crashclaim.crashutils.menusystem.defaultmenus.ConfirmationMenu;
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

        player.spigot().sendMessage(Localization.NEW_CLAIM__CLICK_CORNER.getMessage(player));

        VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
        group.removeAllVisualsOfType(VisualType.MARKER);

        visualizationManager.getProvider(player.getUniqueId()).spawnMarkerVisual(VisualColor.YELLOW, group, firstLocation).spawn();
    }

    private boolean checkCanCreate(Location min, Location max){
        if ((max.getBlockX() - min.getBlockX()) < 4 || (max.getBlockZ() - min.getBlockZ()) < 4) {
            player.spigot().sendMessage(Localization.NEW_CLAIM__MIN_SIZE.getMessage(player));
            return false;
        }

        if (manager.checkOverLapSurroudningClaims(-1, max.getBlockX(), max.getBlockZ(), min.getBlockX(), min.getBlockZ(), min.getWorld().getUID())){
            player.spigot().sendMessage(Localization.NEW_CLAIM__OVERLAPPING.getMessage(player));
            return false;
        }

        if (!CrashClaim.getPlugin().getPluginSupport().canClaim(min, max)){
            player.spigot().sendMessage(Localization.NEW_CLAIM__OTHER_ERROR.getMessage(player));
            return false;
        }

        return true;
    }

    @Override
    public void click(Player player, Location secondLocation) {
        Location min = StaticClaimLogic.calculateMinCorner(firstLocation, secondLocation);
        Location max = StaticClaimLogic.calculateMaxCorner(firstLocation, secondLocation);

        if (!checkCanCreate(min, max)){
            cleanup(player.getUniqueId(), true);
            return;
        }

        int area = ContributionManager.getArea(min.getBlockX(), min.getBlockZ(), max.getBlockX(), max.getBlockZ());

        int price = (GlobalConfig.bypassModeBypassesMoney && PermissionHelper.getPermissionHelper().getBypassManager().isBypass(player.getUniqueId())) ?
                0 : (int) Math.ceil(area * GlobalConfig.money_per_block);
        String priceString = Integer.toString(price);

        if (price > 0){
            new ConfirmationMenu(player,
                    Localization.NEW_CLAIM__CREATE_MENU__TITLE.getMessage(player),
                    Localization.NEW_CLAIM__CREATE_MENU__MESSAGE.getItem(player,
                            "price", priceString),
                    Localization.NEW_CLAIM__CREATE_MENU__ACCEPT.getItem(player,
                            "price", priceString),
                    Localization.NEW_CLAIM__CREATE_MENU__DENY.getItem(player,
                            "price", priceString),
                    (p, aBoolean) -> {
                        if (aBoolean){
                            if (!checkCanCreate(min, max)){
                                cleanup(player.getUniqueId(), true);
                                return "";
                            }

                            CrashClaim.getPlugin().getPayment().makeTransaction(player.getUniqueId(), TransactionType.WITHDRAW, "Claim Purchase", price, (res) -> {
                                if (!res.transactionSuccess()){
                                    player.spigot().sendMessage(Localization.NEW_CLAIM__NOT_ENOUGH_BALANCE.getMessage(player,
                                            "price", priceString));
                                    cleanup(player.getUniqueId(), true);
                                    return;
                                }

                                Bukkit.getScheduler().runTask(CrashClaim.getPlugin(), () -> afterTransaction(min, max, area, player.getUniqueId()));
                            });
                        }
                        return "";
                    },
                    p -> {
                        cleanup(player.getUniqueId(), true);
                        return "";
                    }).open();
        } else {
            afterTransaction(min, max, 0, player.getUniqueId()); // set area to 0 to not add any money into economy
        }
    }

    private void afterTransaction(Location min, Location max, int area, UUID target){
        ClaimResponse response = manager.createClaim(max, min, player.getUniqueId());

        if (response.isStatus()) {
            ((Claim) response.getClaim()).addContribution(player.getUniqueId(), area); //Contribution tracking

            player.spigot().sendMessage(Localization.NEW_CLAIM__SUCCESS.getMessage(player));

            VisualGroup group = visualizationManager.fetchVisualGroup(player, true);
            group.removeAllVisuals();

            BaseVisual visual = visualizationManager.getProvider(target).spawnClaimVisual(VisualColor.GREEN, group, response.getClaim(), player.getLocation().getBlockY() - 1);
            visual.spawn();

            visualizationManager.deSpawnAfter(visual, 5);

            cleanup(player.getUniqueId(), false);
        } else {
            player.spigot().sendMessage(Localization.NEW_CLAIM__ERROR.getMessage(player));
            cleanup(player.getUniqueId(), true);
        }
    }

    @Override
    public void cleanup(UUID player, boolean visuals) {
        commandManager.forceCleanup(player, visuals);
    }
}
