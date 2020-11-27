package net.crashcraft.crashclaim.data.providers.sqlite;

public class DataVersionIncrementException extends RuntimeException {
    public DataVersionIncrementException(int fromRevision, int toRevision) {
        super("Database schema conversion failed: [" + fromRevision + " -> " + toRevision + "]");
    }
}
