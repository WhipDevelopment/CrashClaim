package net.crashcraft.crashclaim.api;

import co.aikar.taskchain.TaskChain;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.menus.list.ClaimListMenu;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.pluginsupport.PluginSupport;
import net.crashcraft.crashclaim.pluginsupport.PluginSupportLoader;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CrashClaimAPI {
    private final CrashClaim crashClaim;

    public CrashClaimAPI(CrashClaim crashClaim){
        this.crashClaim = crashClaim;
    }

    /**
     * Opens the claim list menu for a player
     *
     * @param player to open gui on
     */
    public void openClaimListMenu(Player player){
        new ClaimListMenu(player, null).open();
    }

    /**
     * Returns the a future of a claim at a specific location, completes null if not claim is present
     *
     * @param location of claim
     * @return the CompletableFuture of a claim
     */
    public CompletableFuture<Claim> getClaimAsync(Location location){
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

    /**
     * Returns the claim at a specific location, null if not claim is present
     *
     * @param location of claim
     * @return the claim
     */
    public Claim getClaim(Location location){
        return crashClaim.getDataManager().getClaim(location);
    }

    /**
     * Returns a future of the claim with the corresponding id, completes null if not claim is present
     *
     * @param claimId of the claim
     * @return the CompletableFuture of a claim
     */
    public CompletableFuture<Claim> getClaimAsync(int claimId){
        CompletableFuture<Claim> completableFuture = new CompletableFuture<>();

        TaskChain<?> chain = CrashClaim.newChain();
        chain.asyncFirst(() -> crashClaim.getDataManager().getClaim(claimId))
                .syncLast(completableFuture::complete)
                .setErrorHandler(((e, task) -> completableFuture.completeExceptionally(e)));
        chain.execute();

        return completableFuture;
    }

    /**
     * Returns the claim with the corresponding id, null if not claim is present
     *
     * @param claimId of the claim
     * @return the claim
     */
    public Claim getClaim(int claimId){
        return crashClaim.getDataManager().getClaim(claimId);
    }

    /**
     * Returns a future of an ArrayList containing claims owned by the player, empty if no claims available.
     *
     * @param player of the claim
     * @return the CompletableFuture of an ArrayList of claims
     */
    public CompletableFuture<ArrayList<Claim>> getClaimsAsync(Player player){
        CompletableFuture<ArrayList<Claim>> completableFuture = new CompletableFuture<>();

        TaskChain<?> chain = CrashClaim.newChain();
        chain.asyncFirst(() -> crashClaim.getDataManager().getOwnedClaims(player.getUniqueId()))
                .syncLast(completableFuture::complete)
                .setErrorHandler(((e, task) -> completableFuture.completeExceptionally(e)));
        chain.execute();

        return completableFuture;
    }

    /**
     * Returns an ArrayList containing claims owned by the player, empty if no claims available.
     *
     * @param player of the claim
     * @return the ArrayList of claims
     */
    public ArrayList<Claim> getClaims(Player player){
        return crashClaim.getDataManager().getOwnedClaims(player.getUniqueId());
    }

    /**
     * Returns a future of an ArrayList containing claims contained in the chunk, empty if no claims available.
     *
     * @param chunkX chunk coordinate X
     * @param chunkZ chunk coordinate Z
     * @param uuid of the world
     * @return the CompletableFuture of am ArrayList of claims
     */
    public CompletableFuture<ArrayList<Claim>> getClaimsAsync(long chunkX, long chunkZ, UUID uuid){
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

    /**
     * Returns an ArrayList containing claims contained in the chunk, empty if no claims available.
     *
     * @param chunkX chunk coordinate X
     * @param chunkZ chunk coordinate Z
     * @param uuid of the world
     * @return the ArrayList of claims
     */
    public ArrayList<Claim> getClaims(long chunkX, long chunkZ, UUID uuid){
        if (uuid == null){
            return null;
        }

        return crashClaim.getDataManager().getClaims(chunkX, chunkZ, uuid);
    }

    /**
     * Get the PermissionHelper instance
     *
     * @return a PermissionHelper instance
     */
    public PermissionHelper getPermissionHelper(){
        return PermissionHelper.getPermissionHelper();
    }

    public void registerHook(PluginSupportLoader pluginSupportLoader){
        crashClaim.getPluginSupportManager().register(pluginSupportLoader);
    }

    public void registerHook(PluginSupport pluginSupport){
        crashClaim.getPluginSupportManager().register(pluginSupport);
    }
}
