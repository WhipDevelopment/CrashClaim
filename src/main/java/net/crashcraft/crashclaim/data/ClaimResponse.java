package net.crashcraft.crashclaim.data;

import net.crashcraft.crashclaim.claimobjects.BaseClaim;

public class ClaimResponse {
    private final boolean status;
    private final ErrorType error;
    private final BaseClaim claim;

    public ClaimResponse(boolean status, ErrorType error) {
        this.status = status;
        this.error = error;
        this.claim = null;
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
