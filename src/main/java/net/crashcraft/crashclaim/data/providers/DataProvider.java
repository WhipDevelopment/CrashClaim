package net.crashcraft.crashclaim.data.providers;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import org.bukkit.event.Listener;

import java.util.Set;
import java.util.UUID;

public interface DataProvider extends Listener {
    void init(CrashClaim plugin, ClaimDataManager manager);

    boolean preInitialSave(Claim claim);

    /**
     * Saves and updates a claims data
     */
    void saveClaim(Claim claim);

    /**
     * Removes a claim and all data
     * To remove a subClaim, remove the claim in memory and then save
     */
    void removeClaim(Claim claim);

    /**
     * Removes a subclaim and all data
     **/
    void removeSubClaim(SubClaim claim);

    /**
     *  Loads a claim into memory from the datasource
     * @return A loaded claim object
     */
    Claim loadClaim(Integer id);

    /**
     * @return a list of permitted claims
     */
    Set<Integer> getPermittedClaims(UUID uuid);

    /**
     * @return a list of owned claims
     */
    Set<Integer> getOwnedParentClaims(UUID uuid);
}
