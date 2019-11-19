package net.crashcraft.whipclaim.data;

import net.crashcraft.whipclaim.claimobjects.Claim;

public class ClaimResponse {
    private boolean status;
    private String error;
    private Claim claim;

    public ClaimResponse(boolean status, String error) {
        this.status = status;
        this.error = error;
    }

    public ClaimResponse(boolean status, Claim claim) {
        this.status = status;
        this.error = "";
        this.claim = claim;
    }

    public boolean isStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public Claim getClaim() {
        return claim;
    }
}
