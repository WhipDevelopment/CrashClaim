package net.crashcraft.crashclaim.config;

public class GroupSettings {
    private final int maxClaims;

    public GroupSettings(int maxClaims) {
        this.maxClaims = maxClaims;
    }

    public int getMaxClaims() {
        return maxClaims;
    }
}
