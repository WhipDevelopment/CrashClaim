package net.crashcraft.whipclaim.data;

public class SaveResponse {
    private final String error;
    private final String provider;

    public SaveResponse(String error, String provider) {
        this.error = error;
        this.provider = provider;
    }

    public String getError() {
        return error;
    }

    public String getProvider() {
        return provider;
    }

    public boolean isSuccsessful(){
        return error == null;
    }
}
