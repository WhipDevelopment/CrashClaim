package net.crashcraft.whipclaim.data.providers;

import net.crashcraft.whipclaim.WhipClaim;
import net.crashcraft.whipclaim.claimobjects.Claim;
import net.crashcraft.whipclaim.data.ClaimDataManager;
import org.bukkit.event.Listener;

public interface DataProvider extends Listener {
    void init(WhipClaim plugin, ClaimDataManager manager);

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
     *  Loads a claim into memory from the datasource
     * @return A loaded claim object
     */
    Claim loadClaim(Integer id);
}
