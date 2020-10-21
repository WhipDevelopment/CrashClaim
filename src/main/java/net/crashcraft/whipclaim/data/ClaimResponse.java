package net.crashcraft.whipclaim.data;

import net.crashcraft.whipclaim.claimobjects.BaseClaim;

public class ClaimResponse {
    private boolean status;
    private ErrorType error;
    private BaseClaim claim;

    public ClaimResponse(boolean status, ErrorType error) {
        this.status = status;
        this.error = error;
    }

    public ClaimResponse(boolean status, BaseClaim claim) {
        this.status = status;
        this.error = ErrorType.NONE;
        this.claim = claim;
    }

    public boolean isStatus() {
        return status;
    }

    public ErrorType getError() {
        return error;
    }

    public BaseClaim getClaim() {
        return claim;
    }
}
