package net.crashcraft.crashclaim.api;

import co.aikar.taskchain.TaskChain;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.data.StaticClaimLogic;
import net.crashcraft.crashclaim.menus.list.ClaimListMenu;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CrashClaimAPI {
    private final CrashClaim crashClaim;

    public CrashClaimAPI(CrashClaim crashClaim){
        this.crashClaim = crashClaim;
    }

    public void openClaimListMenu(Player player){
        new ClaimListMenu(player, null).open();
    }

    public CompletableFuture<Claim> getClaim(Location location){
        if (location == null){
            return null;
        }

        CompletableFuture<Claim> completableFuture = new CompletableFuture<>();

        TaskChain<?> chain = CrashClaim.newChain();
        chain.asyncFirst(() -> crashClaim.getDataManager().getClaim(location))
                .syncLast(completableFuture::complete)
                .setErrorHandler(((e, task) -> completableFuture.completeExceptionally(e)));
        chain.execute();

        return completableFuture;
    }

    public CompletableFuture<Claim> getClaim(int claimId){
        CompletableFuture<Claim> completableFuture = new CompletableFuture<>();

        TaskChain<?> chain = CrashClaim.newChain();
        chain.asyncFirst(() -> crashClaim.getDataManager().getClaim(claimId))
                .syncLast(completableFuture::complete)
                .setErrorHandler(((e, task) -> completableFuture.completeExceptionally(e)));
        chain.execute();

        return completableFuture;
    }

    public CompletableFuture<ArrayList<Claim>> getClaims(Player player){
        CompletableFuture<ArrayList<Claim>> completableFuture = new CompletableFuture<>();

        TaskChain<?> chain = CrashClaim.newChain();
        chain.asyncFirst(() -> crashClaim.getDataManager().getOwnedClaims(player.getUniqueId()))
                .syncLast(completableFuture::complete)
                .setErrorHandler(((e, task) -> completableFuture.completeExceptionally(e)));
        chain.execute();

        return completableFuture;
    }

    public CompletableFuture<ArrayList<Claim>> getClaims(long chunkX, long chunkZ, UUID uuid){
        if (uuid == null){
            return null;
        }

        CompletableFuture<ArrayList<Claim>> completableFuture = new CompletableFuture<>();

        TaskChain<?> chain = CrashClaim.newChain();
        chain.asyncFirst(() ->  crashClaim.getDataManager().getClaims(chunkX, chunkZ, uuid))
                .syncLast(completableFuture::complete)
                .setErrorHandler(((e, task) -> completableFuture.completeExceptionally(e)));
        chain.execute();

        return completableFuture;
    }

    public PermissionHelper getPermissionHelper(){
        return PermissionHelper.getPermissionHelper();
    }
}
